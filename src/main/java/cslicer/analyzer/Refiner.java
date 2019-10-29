package cslicer.analyzer;

/*
 * #%L
 * CSlicer
 *    ______ _____  __ _                  
 *   / ____// ___/ / /(_)_____ ___   _____
 *  / /     \__ \ / // // ___// _ \ / ___/
 * / /___  ___/ // // // /__ /  __// /
 * \____/ /____//_//_/ \___/ \___//_/
 * %%
 * Copyright (C) 2014 - 2019 Department of Computer Science, University of Toronto
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Lists;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import cslicer.analyzer.AtomicChange.CHG_TYPE;
import cslicer.analyzer.SlicingResult.DEP_FLAG;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.UnitTestScope;
import cslicer.builder.plain.PlainBuilder;
import cslicer.callgraph.BcelStaticCallGraphBuilder;
import cslicer.callgraph.CGNode;
import cslicer.callgraph.ClassPathInvalidException;
import cslicer.callgraph.StaticCallGraph;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.coverage.TestFailureException;
import cslicer.daikon.ChangedInvVar;
import cslicer.daikon.DaikonLoader;
import cslicer.distiller.ChangeDistillerException;
import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CheckoutBranchFailedException;
import cslicer.jgit.CheckoutFileFailedException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.DeleteBranchFailedException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.soot.impact.LocalChangeImpactAnalysis;
import cslicer.utils.BytecodeUtils;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.StatsUtils;
import daikon.diff.InvMap;

public class Refiner extends HistoryAnalyzer {

	// class BundleComparator implements Comparator<CommitBundle> {
	// @Override
	// public int compare(CommitBundle arg0, CommitBundle arg1) {
	// return Double.compare(arg0.getSignificance(),
	// arg1.getSignificance());
	// }
	// }

	private static final double MIN_THRESHOLD = 0.001;
	protected static final String REFINE_BRANCH = "REFINE";
	private static final double UNIT_SIG = 10.0;
	private static final Double DECAY_FACTOR = 0.8;

	// dependency graph for each commit version
	// entity --> (version, dependencies)
	private Map<String, Map<RevCommit, Set<String>>> fEntityDependencyVersion;

	protected VersionTracker fTracker;
	private InvMap fLastPassInvMap;
	private String fDaikonConfigPath;
	private String fDaikonTracePath;
	private Set<String> fDaikonIncludes;
	private UnitTestScope fScope;

	private final boolean fEnableInvariant;
	private final boolean fEnableLearning;
	private final boolean fEnableInitRank;
	private final boolean fEnableCompCheck;
	private final boolean fEnableProbablistic;

	private final boolean fEnableIntersection;

	private Map<String, Double> fSignificance;
	private String fTestClassRootPath;

	// fixed dependencies
	private Set<String> fFixedDependencies;
	private Set<String> fChangedClasses;

	public Refiner(ProjectConfiguration config)
			throws RepositoryInvalidException, CommitNotFoundException,
			BuildScriptInvalidException, CoverageControlIOException,
			AmbiguousEndPointException, ProjectConfigInvalidException,
			BranchNotFoundException, CoverageDataMissingException,
			CheckoutBranchFailedException, ClassPathInvalidException,
			CheckoutFileFailedException, IOException,
			CompilationFailureException, TestFailureException {
		super(config);

		fEnableInvariant = config.getEnableInvariant();
		fEnableLearning = config.getEnableLearning();
		fEnableInitRank = config.getEnableInitRank();
		fEnableCompCheck = config.getEnableCompCheck();
		fEnableProbablistic = config.getEnableProbablistic();

		fEnableIntersection = config.getEnableIntersection();

		fClassRootPath = config.getClassRootPath();
		fTestClassRootPath = config.getTestClassRootPath();

		fEntityDependencyVersion = new HashMap<>();
		fFixedDependencies = new HashSet<>();
		fChangedClasses = new HashSet<>();
		fScope = config.getTestCases();

		// initializeCoverage(fConfig);
		List<RevCommit> A = new LinkedList<>(fHistory);
		fTracker = new VersionTracker(A, fComparator);
		preProcessHistory();

		initializeSignificance();

		// fDaikonIncludes = config.getDaikonIncludes();
		fDaikonIncludes = fChangedClasses;

		initializeCompiler(config);

		PrintUtils.print("Using options: ");
		PrintUtils.print("Enable Comp: " + fEnableCompCheck);
		PrintUtils.print("Enable Learning: " + fEnableLearning);
		PrintUtils.print("Enable Invariant: " + fEnableInvariant);
		PrintUtils
				.print("Enable Probablistic Inference: " + fEnableProbablistic);
		PrintUtils.print("Enable InitRank: " + fEnableInitRank);

		if (fEnableCompCheck) {
			buildChangeDependencyGraph();

			propogateDependencyVersin(fStart);
			for (RevCommit c : fHistory)
				propogateDependencyVersin(c);
		}

		// Check that the final version compiles (don't do this anymore, this is
		// an assumption).
		// fCompiler.restoreBuildFile();
		// fJGit.checkOutNewBranch(REFINE_BRANCH, fEnd);
		// fCompiler.writeBuildFile();
		// assert fCompiler.checkCompilation();
		// fCompiler.restoreBuildFile();
	}

	// this has to be called after pre-process history
	private void buildChangeDependencyGraph()
			throws CheckoutBranchFailedException, ClassPathInvalidException,
			CheckoutFileFailedException, CompilationFailureException,
			CommitNotFoundException, IOException {
		StatsUtils.resume("call.graphs");

		// ChangeExtractor extractor = new ChangeExtractor(fJGit,
		// fConfig.getProjectJDKVersion());
		Set<String> scope = fTracker.getChangedEntities();

		// ========= initial dependency analysis of whole program =============
		// Analyze base version
		// fCompiler.restoreBuildFile();
		// fJGit.checkOutNewBranch(REFINE_BRANCH, fStart);
		// fCompiler.writeBuildFile();
		// if (!fCompiler.checkCompilation())
		// throw new CompilationFailureException(commitSummary(fStart));
		// fCompiler.restoreBuildFile();

		// call graph for all entities
//		BcelStaticCallGraphBuilder callGraphBuilder = new BcelStaticCallGraphBuilder(
//				fClassRootPath);
//		// use a sparser call graph here to save time (TODO: test
//		// differences with experiments)
//		// callGraphBuilder.buildClassLevelCallGraph();
//		callGraphBuilder.buildCallGraph();
//		//
//		// // find fixed dependencies: changed entities which are depend by
//		// fixed
//		// // entities
//		StaticCallGraph depsBase = callGraphBuilder.getCallGraph();
//		// // PrintUtils.print(depsBase.toString());
//		depsBase.outputDOTFile("/tmp/base-deps.txt");

		// Compute test code call graph
		BcelStaticCallGraphBuilder testCGBuilder = new BcelStaticCallGraphBuilder(
				fTestClassRootPath);
		testCGBuilder.buildCallGraph();
		StaticCallGraph testCG = testCGBuilder.getCallGraph();
		testCG.outputDOTFile("/tmp/test-deps.txt");

//		for (String entity : scope) {
//			// do not consider class-field, class-method relations
//			// XXX this will include field declare with assignment (can be a
//			// problem!)
//			for (String n : depsBase.getHardDependers(entity)) {
//				if (!containsIdentifier(scope, n))
//					fFixedDependencies.add(entity);
//			}
//		}

		for (String testMethod : fScope.getTestMethodFullyQualifiedNames()) {
			Set<String> deps = testCG.getDependees(testMethod);
			deps.retainAll(scope);
			fFixedDependencies.addAll(deps);
		}

		// propogateDependencyVersion(fStart, depsBase);

		// =====================================================================

		// =============== Lazy analysis of the following versions =============
		// callGraphBuilder.setScope(scope);
		//
		// // analyze following versions
		// for (RevCommit c : fHistory) {
		//
		// PrintUtils.print("Analyzing version: " + commitSummary(c));
		//
		// Pair<Set<String>, Set<String>> pair = processCommit(c, extractor);
		// Set<String> insertedClasses = pair.getLeft();
		// Set<String> deletedClasses = pair.getRight();
		//
		// fJGit.checkOutVersion(c);
		//
		// fCompiler.writeBuildFile();
		// if (!fCompiler.checkCompilation())
		// throw new CompilationFailureException(commitSummary(c));
		// fCompiler.restoreBuildFile();
		//
		// // call graph for only changed entities
		// callGraphBuilder.buildPartialCallGraph(insertedClasses,
		// deletedClasses);
		//
		// // fDependencyGraphVersion.put(c, callGraphBuilder.getCallGraph());
		//
		// propogateDependencyVersion(c, callGraphBuilder.getCallGraph());
		// }

		StatsUtils.stop("call.graphs");
	}

	/**
	 * Return a String representation of change dependency versions.
	 * 
	 * @return String text representation of change dependencies.
	 */
	public String changeDependencyPrettyPrint() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n");

		for (String e : fEntityDependencyVersion.keySet()) {
			builder.append(e + " ");

			int i = 0;
			for (RevCommit h : fHistory) {
				builder.append(i++ + ":");
				Set<String> deps = fEntityDependencyVersion.get(e).get(h);
				builder.append(deps);
			}

			builder.append("\n");
		}

		return builder.toString();
	}

	private double commitSignificance(Collection<RevCommit> set) {
		int total = 0;
		for (RevCommit c : set)
			total += commitSignificance(c);
		return total;
	}

	private double commitSignificance(RevCommit c) {
		Set<AtomicChange> changes = fTracker.getChangesVersion(c);
		double total = 0.0;

		// assign an negative score for empty commit
		// otherwise it will be 0 initially
		if (fEnableInitRank && changes.isEmpty())
			total = -1 * UNIT_SIG;

		for (AtomicChange a : changes)
			total += fSignificance.get(a.getIdentifier());

		return total;
	}

	private boolean compilableRemoveChanges(Set<AtomicChange> toRemove) {

		// force sequential removal
		Set<AtomicChange> remove = new HashSet<>(toRemove);
		for (AtomicChange atom : toRemove) {
			remove.addAll(fTracker.outStandingChanges(atom.getPreImage(),
					atom.getIdentifier()));
		}
		if (!toRemove.containsAll(remove))
			return false;

		// check compilation dependencies
		// initial view has to be consistent
		ChangedEntityView view = getCurrentEntityView();

		view.refineFixedDeps();
		assert view.isConsistent();

		// PrintUtils.print(fFixedDependencies);

		// update view : XXX bug . add entity back when remove delete
		for (String e : fTracker.getChangedEntities()) {

			// search for latest deletion
			AtomicChange lAtom = getLatestChange(e, remove);
			if (lAtom != null) {
				if (lAtom.getChangeType() == CHG_TYPE.DEL) {
					// atom is the earliest change for e
					String aid = lAtom.getIdentifier();
					view.insertEntity(aid, fEntityDependencyVersion.get(aid)
							.get(lAtom.getPreImage()));
				}
			}

			// search for earliest insertion
			AtomicChange eAtom = getEarliestChange(e, remove);
			if (eAtom != null) {

				// atom is the earliest change for e
				String aid = eAtom.getIdentifier();
				view.setVersion(eAtom.getIdentifier(), fEntityDependencyVersion
						.get(aid).get(eAtom.getPreImage()),
						eAtom.getPreImage());

				// re-compute existence of entity
				if (eAtom.getChangeType() == CHG_TYPE.INS) {
					view.removeEntity(aid);
				}
			}
		}

		return view.isConsistent();
	}

	private boolean compilableRemoveCommits(Set<RevCommit> toRemove) {
		Set<AtomicChange> changes = new HashSet<>();
		for (RevCommit c : toRemove) {
			changes.addAll(fTracker.getChangesVersion(c));
		}
		return compilableRemoveChanges(changes);
	}

	private boolean containsIdentifier(Collection<String> set, String id) {
		for (String s : set) {
			if (BytecodeUtils.matchWithGenericType(s, id))
				return true;
		}
		return false;
	}

	private void decaySignificance() {
		for (String e : fTracker.getChangedEntities()) {
			if (Math.abs(fSignificance.get(e)) < MIN_THRESHOLD)
				fSignificance.put(e, 0.0);
			else
				fSignificance.put(e, fSignificance.get(e) * DECAY_FACTOR);
		}
	}

	/**
	 * Execute test suite on the sliced history.
	 * 
	 * @param drop
	 *            a set of changes to drop.
	 * 
	 * @return {@code true} if tests pass.
	 * @throws IOException
	 *             throws if invariant file cannot be found.
	 */
	public boolean execute(final Set<RevCommit> drop) throws IOException {

		StatsUtils.resume("test.run");
		StatsUtils.count("test");

		fCompiler.writeBuildFile();

		if (!fCompiler.checkCompilation()) {
			fCompiler.restoreBuildFile();

			StatsUtils.count("real.comp.fail");

			StatsUtils.stop("test.run");
			return false;
		}

		boolean isTestPass = fCompiler.runSingleTest(fScope);
		boolean singleChange = (drop.size() == 1);

		fCompiler.restoreBuildFile();

		StatsUtils.stop("test.run");

		if (!fEnableLearning)
			return isTestPass;

		// pass: decrease significance of entities in removed commits
		// fail: increase significance of entities in removed commits
		Set<AtomicChange> total = new HashSet<>();
		for (RevCommit d : drop) {
			Set<AtomicChange> atoms = fTracker.getChangesVersion(d);
			total.addAll(atoms);
		}

		// definite confidence for test pass
		// probabilistic confidence for test failure
		double sigDelta = 0.0;
		if (isTestPass)
			sigDelta = -UNIT_SIG;
		else if (!isTestPass && singleChange)
			sigDelta = UNIT_SIG;
		else if (!isTestPass && fEnableProbablistic)
			sigDelta = UNIT_SIG / (double) total.size();

		for (AtomicChange a : total) {
			// update significance
			fSignificance.put(a.getIdentifier(),
					fSignificance.get(a.getIdentifier()) + sigDelta);
		}

		// start processing daikon invariants
		if (!fEnableInvariant)
			return isTestPass;

		// learning significance from invariant delta

		if (singleChange || isTestPass) {
			DaikonLoader loader = new DaikonLoader();
			InvMap invMap = loader.loadInvMapfromTrace(fDaikonTracePath,
					fDaikonConfigPath);
			List<ChangedInvVar> varList = loader.getChangedVars(fLastPassInvMap,
					invMap);

			// PrintUtils.print("Invariant Diff: ");
			// PrintUtils.print(varList);

			StatsUtils.resume("soot.lia");
			// XXX the second classpath has to be set to the project classpath
			LocalChangeImpactAnalysis lcia = new LocalChangeImpactAnalysis(
					fClassRootPath, fClassRootPath, fChangedClasses);
			lcia.inferImpactingSources(varList);
			StatsUtils.stop("soot.lia");

			if (isTestPass) {
				// lower the rank of minus-set
				for (String e : lcia.fTargetEntities) {
					for (String entity : fSignificance.keySet()) {
						if (BytecodeUtils.matchWithGenericType(e, entity)) {
							fSignificance.put(entity,
									fSignificance.get(entity) - UNIT_SIG);
						}
					}
				}

				fLastPassInvMap = invMap;
				return true;
			} else {
				if (singleChange) {
					// higher the rank of minus-set
					for (String e : lcia.fTargetEntities) {
						for (String entity : fSignificance.keySet()) {
							if (BytecodeUtils.matchWithGenericType(e, entity)) {
								fSignificance.put(entity,
										fSignificance.get(entity) + UNIT_SIG);
							}
						}
					}
				}

				return false;
			}
		}

		return false;
	}

	private ChangedEntityView getCurrentEntityView() {
		ChangedEntityView view = new ChangedEntityView(fFixedDependencies);

		for (String e : fTracker.getPresentEntities()) {
			RevCommit version;
			if (fTracker.getHeadChangeFor(e) == null)
				version = fStart;
			else
				version = fTracker.getHeadChangeFor(e).getPostImage();

			view.setVersion(e, fEntityDependencyVersion.get(e).get(version),
					version);
		}

		return view;
	}

	private AtomicChange getEarliestChange(String entity,
			Collection<AtomicChange> set) {
		List<AtomicChange> list = new ArrayList<>();
		for (AtomicChange c : set) {
			if (c.getIdentifier().equals(entity))
				list.add(c);
		}

		if (list.isEmpty())
			return null;

		return Collections.min(list);
	}

	private AtomicChange getLatestChange(String entity,
			Collection<AtomicChange> set) {
		List<AtomicChange> list = new ArrayList<>();
		for (AtomicChange c : set) {
			if (c.getIdentifier().equals(entity))
				list.add(c);
		}

		if (list.isEmpty())
			return null;

		return Collections.max(list);
	}

	/*
	 * private void initializeCompiler(ProjectConfiguration config) throws
	 * BuildScriptInvalidException, IOException { // if
	 * (config.isClassRootPathSet() && config.isSourceRootPathSet()) { // //
	 * class root path is set by the user // fClassRootPath =
	 * config.getClassRootPath(); // } else { fTargetPath =
	 * FileUtils.createTempDir("gitref", "-target", null);
	 * 
	 * if (fEnableInvariant) { fCompiler = new
	 * MavenInvokerDaikon(fConfig.getBuildScriptPath(),
	 * fConfig.getDaikonJarPath(), fConfig.getChicoryJarPath(), fDaikonIncludes,
	 * fConfig.isBuilderOutputEnabled());
	 * fCompiler.initializeBuild(fTargetPath);
	 * 
	 * fDaikonConfigPath = config.getDaikonConfigPath(); fDaikonTracePath =
	 * ((MavenInvokerDaikon) fCompiler).getDaikonTrace() .getAbsolutePath(); }
	 * else { fCompiler = new MavenInvokerDefault(fConfig.getBuildScriptPath(),
	 * fConfig.isBuilderOutputEnabled());
	 * fCompiler.initializeBuild(fTargetPath); }
	 * 
	 * // class root path is set by Maven invoker fClassRootPath =
	 * fCompiler.getClassDirPath().getAbsolutePath(); }
	 */

	private void initializeCompiler(ProjectConfiguration config)
			throws BuildScriptInvalidException {
		fCompiler = new PlainBuilder(fConfig.getBuildScriptPath(),
				fConfig.getRepositoryPath());
	}

	/**
	 * Initial run of test on the latest version and collect Daikon invariants.
	 * 
	 * @return {@code true} if initialization fails.
	 * @throws IOException
	 *             throws if invariant file cannot be found.
	 * @throws BuildScriptInvalidException
	 *             throws if build script is invalid.
	 */
	public boolean initializeDaikon()
			throws IOException, BuildScriptInvalidException {
		fCompiler.compileTests();

		if (!fCompiler.runSingleTest(fScope))
			return false;

		if (fEnableInvariant) {
			DaikonLoader loader = new DaikonLoader();
			this.fLastPassInvMap = loader.loadInvMapfromTrace(fDaikonTracePath,
					fDaikonConfigPath);

		}

		return true;
	}

	private void initializeSignificance() {
		fSignificance = new HashMap<>();
		// initialize significance to 0
		for (String e : fTracker.getChangedEntities()) {
			fSignificance.put(e, 0.0);
		}
		// // test change to -100
		// for (RevCommit c : fTracker.getPresentCommits()) {
		// Set<AtomicChange> atoms = fTracker.getChangesVersion(c);
		// for (AtomicChange a : atoms) {
		// fSignificance.put(a.getIdentifier(), -UNIT_SIG / 100.0);
		// }
		// }
	}

	private boolean isNewer(RevCommit c1, RevCommit c2) {
		return fComparator.compare(c1, c2) > 0;
	}

	/**
	 * Return the minimal set of {@link AtomicChange} needed to be dropped
	 * together in order to compile.
	 * 
	 * @param h
	 *            target {@link AtomicChange} to drop.
	 * @return a set of collateral changes to drop.
	 */
	public Set<AtomicChange> minimalRemovableChangeSet(AtomicChange h) {
		return minimalRemovableChangeSet(Collections.singleton(h));
	}

	// compute a smallest set of atomic changes needed to be removed together
	protected Set<AtomicChange> minimalRemovableChangeSet(
			final Set<AtomicChange> toRemove) {
		Set<AtomicChange> res = new HashSet<>();

		// pre-process: collect all changes to remove (by order)
		Set<AtomicChange> remove = new HashSet<>(toRemove);
		for (AtomicChange atom : toRemove) {
			remove.addAll(fTracker.outStandingChanges(atom.getPreImage(),
					atom.getIdentifier()));
		}

		// initial view has to be consistent
		ChangedEntityView view = getCurrentEntityView();
		assert view.isConsistent();

		do {
			// update view
			for (String e : fTracker.getChangedEntities()) {
				AtomicChange atom = getEarliestChange(e, remove);
				if (atom == null)
					continue;

				// atom is the earliest change for e
				String aid = atom.getIdentifier();
				view.setVersion(atom.getIdentifier(), fEntityDependencyVersion
						.get(aid).get(atom.getPreImage()), atom.getPreImage());
				if (atom.getChangeType() == CHG_TYPE.INS) {
					view.removeEntity(aid);
				} else if (atom.getChangeType() == CHG_TYPE.DEL
						|| atom.getChangeType() == CHG_TYPE.UPD) {
				} else
					assert false;
			}

			// resolve missing entities
			Set<Pair<String, String>> miss = view.getMissingDependencies();

			for (Pair<String, String> m : miss) {
				String depender = m.getLeft();
				String dependee = m.getRight();

				PrintUtils.print(depender + " --> " + dependee);

				// revert newer entity to match with the older one
				if (view.getVersion(depender)
						.equals(view.getVersion(dependee))) {
					assert false;
				} else if (isNewer(view.getVersion(depender),
						view.getVersion(dependee))) {
					Map<RevCommit, Set<String>> dVersion = fEntityDependencyVersion
							.get(depender);
					Set<RevCommit> noDepVersion = new HashSet<>();

					// find the latest version where depender does not depend on
					// dependee
					for (RevCommit v : dVersion.keySet())
						if (!dVersion.get(v).contains(dependee)
								&& !isNewer(v, view.getVersion(depender)))
							noDepVersion.add(v);

					RevCommit latest = (RevCommit) Collections.max(noDepVersion,
							fComparator);
					remove.addAll(
							fTracker.outStandingChanges(latest, depender));
				} else {
					// find the latest version where dependee exists
					// in other words find the latest delete
					AtomicChange ac = fTracker.getHeadChangeFor(dependee);
					assert ac.getChangeType().equals(CHG_TYPE.DEL);
					remove.add(ac);
				}
			}

		} while (!view.isConsistent());

		res.addAll(remove);
		return res;
	}

	public enum SCHEME {
		NEGATIVE, NON_POSITIVE, LOWER_3, LOWER_4, COMBINED
	}

	/**
	 * Produce n different partitions.
	 * 
	 * @param n
	 *            partition granularity
	 * @param scheme
	 *            select partition scheme
	 * @return a list of commits to drop
	 */
	public List<CommitBundle> partition(int n, SCHEME scheme) {
		List<CommitBundle> res = new LinkedList<>();

		// remaining commits
		Set<RevCommit> universe = fTracker.getPresentCommits();

		n = Math.min(universe.size(), n);

		// use significance ranking for partition
		if (fEnableLearning || fEnableInvariant) {
			Set<RevCommit> positive = new HashSet<>();
			Set<RevCommit> negative = new HashSet<>();

			// ranking of commits
			PriorityQueue<Rank<RevCommit>> ranking = new PriorityQueue<>(11,
					new Comparator<Rank<RevCommit>>() {
						@Override
						public int compare(Rank<RevCommit> o1,
								Rank<RevCommit> o2) {
							return Double.compare(o1.getSignificance(),
									o2.getSignificance());
						}
					});

			for (RevCommit c : universe) {
				double sigc = commitSignificance(c);
				ranking.add(new CommitRank(c, sigc));

				if (sigc > 0.0)
					positive.add(c);
				else if (sigc < 0.0)
					negative.add(c);
			}

			// ----------- partition by significance ----------------

			if (scheme == SCHEME.COMBINED) {
				// scheme 2: drop negative
				if (!negative.isEmpty() && negative.size() < universe.size())
					res.add(new CommitBundle(negative, -100 * UNIT_SIG));

				// scheme 1: drop non-positive
				if (positive.size() > Math.max(universe.size() / 2, 2)
						&& positive.size() < universe.size())
					res.add(new CommitBundle(
							CollectionUtils.subtract(universe, positive),
							-99 * UNIT_SIG));

				// scheme 3: drop the lower 1/3
				if (!positive.isEmpty() || !negative.isEmpty()) {
					List<RevCommit> lower = new LinkedList<>();
					if (ranking.size() >= 6) {
						for (int i = 0; i < universe.size() / 3; i++) {
							lower.add(ranking.poll().getElement());
						}

						res.add(new CommitBundle(new HashSet<>(lower),
								-98 * UNIT_SIG));
					}
					// scheme 4: drop the lower 1/4
				}
			} else if (scheme == SCHEME.NEGATIVE) {
				// scheme 2: drop negative
				if (!negative.isEmpty() && negative.size() < universe.size())
					res.add(new CommitBundle(negative, -100 * UNIT_SIG));
			} else if (scheme == SCHEME.NON_POSITIVE) {
				// scheme 1: drop non-positive
				if (!positive.isEmpty() && positive.size() < universe.size())
					res.add(new CommitBundle(
							CollectionUtils.subtract(universe, positive),
							-100 * UNIT_SIG));
			} else if (scheme == SCHEME.LOWER_3) {
				// scheme 3: drop the lower 1/3
				if (!positive.isEmpty() || !negative.isEmpty()) {
					List<RevCommit> lower = new LinkedList<>();
					if (ranking.size() >= 6) {
						for (int i = 0; i < universe.size() / 3; i++) {
							lower.add(ranking.poll().getElement());
						}

						res.add(new CommitBundle(new HashSet<>(lower),
								-98 * UNIT_SIG));
					}
				}
			}
			// ----------- partition by significance ----------------
		}

		assert universe.size() / n > 0;

		// ---------- random partition into size n groups -----------
		for (List<RevCommit> l : Lists.partition(new LinkedList<>(universe),
				universe.size() / n)) {
			// size n set
			res.add(new CommitBundle(new HashSet<>(l), commitSignificance(l)));
			// compliment
			Set<RevCommit> compliment = new HashSet<>(
					CollectionUtils.subtract(universe, l));
			res.add(new CommitBundle(compliment,
					commitSignificance(compliment)));
		}
		// ---------- random partition into size n groups -----------

		return res;
	}

	private void preProcessHistory() throws CommitNotFoundException {
		ChangeExtractor extractor = new ChangeExtractor(fJGit,
				fConfig.getProjectJDKVersion());

		StatsUtils.resume("history.preprocess");

		int i = 0; // commit sequence number
		for (RevCommit c : fHistory) {
			Set<GitRefSourceCodeChange> changes;
			try {
				changes = extractor.extractChangesMerge(c);
			} catch (ChangeDistillerException e) {
				PrintUtils.print(
						"Exception occurs in change distilling! Result will be unreliable!",
						TAG.WARNING);
				e.printStackTrace();
				continue;
			}

			for (GitRefSourceCodeChange gitChange : changes) {
				// get change distiller change
				SourceCodeChange change = gitChange.getSourceCodeChange();
				// get file path to changed entity
				String filePath = gitChange.getChangedFilePath();
				// unique identifier of changed entity
				String uniqueName = null;
				// dependency type (reason for keeping)
				DEP_FLAG depType = DEP_FLAG.DROP;
				// change operation type
				CHG_TYPE chgType = null;

				fChangedClasses.add(gitChange.getEnclosingClassName());

				// parent entity is field/method/class which contains the
				// change
				if (change instanceof Delete) {
					Delete del = (Delete) change;
					uniqueName = del.getChangedEntity().getUniqueName();
					chgType = CHG_TYPE.DEL;
				} else if (change instanceof Insert) {
					Insert ins = (Insert) change;
					uniqueName = ins.getChangedEntity().getUniqueName();
					chgType = CHG_TYPE.INS;

				} else if (change instanceof Update) {
					Update upd = (Update) change;
					uniqueName = upd.getNewEntity().getUniqueName();
					// is signature updated?
					boolean signatureChange = !upd.getChangedEntity()
							.getUniqueName().equals(uniqueName);
					assert !signatureChange;
					chgType = CHG_TYPE.UPD;
				} else if (change instanceof Move) {
					// shouldn't detect move for structure nodes
					assert false;
				} else
					assert false;

				// track this atomic change
				fTracker.trackAtomicChangeAdd(new AtomicChange(uniqueName,
						filePath, gitChange.getPreImage(),
						gitChange.getPostImage(), i, depType, chgType));
			}

			i++;
		}

		// fTracker.collectChangedEntities();
		// PrintUtils.print(fTracker.changedEntityVersionPrettyPring());

		StatsUtils.stop("history.preprocess");
	}

	private Pair<Set<String>, Set<String>> processCommit(RevCommit c,
			ChangeExtractor extractor) throws CommitNotFoundException {
		Set<GitRefSourceCodeChange> changes;
		Set<String> changedClasses = new HashSet<>();
		Set<String> removedClasses = new HashSet<>();

		try {
			changes = extractor.extractChangesMerge(c);
		} catch (ChangeDistillerException e) {
			PrintUtils.print(
					"Exception occurs in change distilling! Result will be unreliable!",
					TAG.WARNING);
			e.printStackTrace();
			return Pair.of(changedClasses, removedClasses);
		}

		for (GitRefSourceCodeChange gitChange : changes) {
			// get change distiller change
			SourceCodeChange change = gitChange.getSourceCodeChange();
			changedClasses.add(gitChange.getEnclosingClassName());

			// parent entity is field/method/class which contains the
			// change
			if (change instanceof Delete) {
				Delete del = (Delete) change;
				if (del.getChangeType().equals(ChangeType.REMOVED_CLASS)) {
					removedClasses.add(gitChange.getEnclosingClassName());
				}
			}
		}

		changedClasses.removeAll(removedClasses);
		return Pair.of(changedClasses, removedClasses);
	}

	private void propogateDependencyVersin(RevCommit version) {
		for (String e : fTracker.getChangedEntities()) {
			Map<RevCommit, Set<String>> dependencyVersion;

			if (!fEntityDependencyVersion.containsKey(e)) {
				dependencyVersion = new HashMap<>();
				fEntityDependencyVersion.put(e, dependencyVersion);
			}
			dependencyVersion = fEntityDependencyVersion.get(e);
			if (!dependencyVersion.containsKey(version)) {
				dependencyVersion.put(version, new HashSet<String>());
			}
		}
	}

	private void propogateDependencyVersion(RevCommit version,
			StaticCallGraph dGraph) {

		for (String e : fTracker.getChangedEntities()) {
			Map<RevCommit, Set<String>> dependencyVersion;

			if (!fEntityDependencyVersion.containsKey(e)) {
				dependencyVersion = new HashMap<>();
				fEntityDependencyVersion.put(e, dependencyVersion);
			}
			dependencyVersion = fEntityDependencyVersion.get(e);

			Set<String> deps;

			if (!dependencyVersion.containsKey(version)) {
				deps = new HashSet<>();
				dependencyVersion.put(version, deps);
			}
			deps = dependencyVersion.get(version);

			for (CGNode n : dGraph.getOutgoingNodes(e)) {

				for (String e1 : fTracker.getChangedEntities()) {
					if (BytecodeUtils.matchWithGenericType(e1, n.getName()))
						deps.add(e1);
				}
				//
				// if (fTracker.getChangedEntities().contains(n.getName()))
				// deps.add(n.getName());
			}
		}
	}

	// return a random element from a bag
	// private <T> T randomChoice(Collection<T> bag) {
	// return (T) bag.toArray()[randomChoice(bag.size())];
	// }

	// return a random number from 0 to n-1
	// private int randomChoice(int n) {
	// Random rand = new Random();
	// return rand.nextInt(n);
	// }

	/**
	 * Main entrance of the delta refinement algorithm.
	 * 
	 * @param scheme
	 *            significance learning scheme.
	 * @return a list of {@link RevCommit}.
	 * @throws IOException
	 *             throws if IO exception happens.
	 * @throws BuildScriptInvalidException
	 *             throws if build script is invalid.
	 */
	public List<RevCommit> refineSlice(SCHEME scheme)
			throws IOException, BuildScriptInvalidException {

		PrintUtils.print("Start Refining...");

		// Empty tried set initially
		Set<List<RevCommit>> tried = new HashSet<>();
		computeOneMinimalHistory(scheme, tried);

		List<RevCommit> minimal = fTracker.getPresentHistory();
		for (RevCommit c : minimal) {
			PrintUtils.print("H*: " + commitSummary(c), TAG.OUTPUT);
		}

		PrintUtils.print("|H*| = " + fTracker.getPresentCommits().size(),
				TAG.OUTPUT);
		StatsUtils.setCount("hstar.length",
				fTracker.getPresentCommits().size());

		if (fEnableJson) {
			Map<String, Double> sig = new HashMap<String, Double>();
			for (String e : fTracker.getChangedEntities()) {
				sig.put(e, fSignificance.get(e));
			}
			printJSONResults(fTracker.getPresentHistory(),
					Collections.<RevCommit> emptyList(), sig);
		}

		// print the entities
		if (fEnableIntersection) {
			PrintUtils.print("Important Entities:");
			for (String entity : fTracker.getPresentEntities()) {
				PrintUtils.print(entity);
			}
		}

		try {
			cleanBranches();
			fJGit.deleteBranch(REFINE_BRANCH);
		} catch (DeleteBranchFailedException e) {
			PrintUtils.print("Cleanning branches failed!", TAG.WARNING);
		}

		return minimal;
	}

	protected void computeOneMinimalHistory(SCHEME scheme,
			Set<List<RevCommit>> tried) throws IOException {
		StatsUtils.resume("total");
		// initial Daikon run
		// StatsUtils.resume("daikon.init");
		// fCompiler.writeBuildFile();
		// assert initializeDaikon();
		// fCompiler.restoreBuildFile();
		// StatsUtils.stop("daikon.init");

		int n = 2;
		int triedCount = 0;
		boolean success = true;

		do {
			// increase granularity - avoid overflow of n
			n = success ? n : Math.min(200, 2 * n);
			List<CommitBundle> partitions = partition(n, scheme);
			triedCount = tried.size();

			PrintUtils.print("Tried count = " + triedCount + ", n = " + n);

			// decay past significance measure
			decaySignificance();

			// update significance ordering dynamically
			PriorityQueue<CommitBundle> parRank = new PriorityQueue<>(11,
					new Comparator<CommitBundle>() {
						@Override
						public int compare(CommitBundle arg0,
								CommitBundle arg1) {
							return Double.compare(arg0.getSignificance(),
									arg1.getSignificance());
						}
					});

			parRank.addAll(partitions);

			do {
				success = false;

				CommitBundle lowest = parRank.poll();
				// remove tried partitions
				partitions.remove(lowest);
				Set<RevCommit> drop = lowest.getBundle();

				PrintUtils.print("Trying H- (total significance = "
						+ lowest.getSignificance() + "): ");

				for (RevCommit d : drop) {
					PrintUtils.print(
							fHistory.indexOf(d) + " : " + commitSummary(d),
							TAG.DEBUG);
				}

				List<RevCommit> toPick = fTracker.getPresentHistory();
				toPick.removeAll(drop);

				// check if tried before
				if (tried.contains(toPick)) {
					PrintUtils.print("Tried before!", TAG.DEBUG);
					continue;
				}

				tried.add(toPick);
				PrintUtils.print("Add to tried: " + toPick.hashCode(),
						TAG.DEBUG);

				// check empty
				if (toPick.isEmpty()) {
					// this is going back to base
					// which is assumed to fail
					continue;
				}

				// check compilation
				if (fEnableCompCheck && !compilableRemoveCommits(drop)) {
					PrintUtils.print("Comp deps violated!", TAG.DEBUG);
					StatsUtils.count("pre.comp.fail");
					// assert (!fCompiler.checkCompilation());
					continue;
				}

				// check hunk
				// can optimize this with reusing cache
				List<RevCommit> hunks = computeHunkDepSet(toPick);
				if (!toPick.containsAll(hunks)) {
					PrintUtils.print("Hunk deps violated!", TAG.DEBUG);
					StatsUtils.count("hunk.fail");
					continue;
				}

				if (!tryPickCommits(toPick, fExcludedPaths)) {
					continue;
				}

				if (!execute(drop)) {
					significancePrettyPrint();
					undoRemoveCommit(true);

					if (fEnableLearning) {
						// update partition ordering
						parRank.clear();
						for (CommitBundle cb : partitions) {
							cb.totalSig = commitSignificance(cb.getBundle());
							parRank.add(cb);
						}
					}

				} else {
					significancePrettyPrint();
					fTracker.trackCommitDel(drop);
					PrintUtils.print("|H*| = "
							+ fTracker.getPresentCommits().size()
							+ " at Iteration "
							+ StatsUtils.readCount("iteration") + " after "
							+ StatsUtils.readCount("test") + " tests.",
							TAG.OUTPUT);

					success = true;
					n = 2;

					break;
				}

			} while (partitions.size() > 0);

			StatsUtils.count("iteration");
		} while (fTracker.getPresentCommits().size() > 1
				&& (n < fTracker.getPresentCommits().size()
						|| triedCount < tried.size()));

		StatsUtils.stop("total");
	}

	private void significancePrettyPrint() {
		String res = "Significance: \n";

		List<ChangeRank> rank = new ArrayList<>();
		for (String e : fTracker.getChangedEntities()) {
			rank.add(new ChangeRank(e, fSignificance.get(e)));
		}

		Collections.sort(rank, new Comparator<Rank<String>>() {
			@Override
			public int compare(Rank<String> o1, Rank<String> o2) {
				return Double.compare(o1.getSignificance(),
						o2.getSignificance());
			}
		});

		for (ChangeRank e : rank) {
			res += e.element + " : " + fSignificance.get(e.element) + "\n";
		}
		PrintUtils.print(res, TAG.DEBUG);
	}

	public boolean verifyOneMinimal(List<RevCommit> minimal)
			throws IOException {

		boolean res = true;

		for (RevCommit c : minimal) {
			List<RevCommit> pick = new LinkedList<>();

			for (RevCommit o : minimal) {

				if (o != c)
					pick.add(o);
			}

			List<RevCommit> hunks = computeHunkDepSet(pick);
			if (!pick.containsAll(hunks)) {
				PrintUtils.print("Hunk deps violated!", TAG.DEBUG);
				continue;
			}

			if (!tryPickCommits(pick)) {
				continue;
			}

			fCompiler.writeBuildFile();
			if (!fCompiler.checkCompilation()) {
				fCompiler.restoreBuildFile();
				continue;
			}

			if (fCompiler.runSingleTest(fScope)) {
				PrintUtils.print(commitSummary(c) + " can be removed!",
						TAG.DEBUG);
				StatsUtils.count("final.remove");
				res = false;
			}
			fCompiler.restoreBuildFile();
		}

		return res;
	}
}
