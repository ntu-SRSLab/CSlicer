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
 * Copyright (C) 2014 - 2020 Department of Computer Science, University of Toronto
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Map;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Sets;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import cslicer.analyzer.AtomicChange.CHG_TYPE;
import cslicer.analyzer.SlicingResult.DEP_FLAG;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.maven.MavenInvokerJacoco;
import cslicer.callgraph.BcelStaticCallGraphBuilder;
import cslicer.callgraph.CGNode;
import cslicer.callgraph.ClassPathInvalidException;
import cslicer.callgraph.StaticCallGraphBuilder;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.coverage.CoverageDatabase;
import cslicer.coverage.FullCoverageAnalyzer;
import cslicer.coverage.TestFailureException;
import cslicer.distiller.ChangeDistillerException;
import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CheckoutFileFailedException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.DependencyCache;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.StatsUtils;

/**
 * Implementations of the semantic slicing algorithms.
 * 
 * @author Yi Li
 * @since JDK1.7
 */
public class Slicer extends HistoryAnalyzer {

	private final String fResultPath; // path to save slicing result
	

	// test touching set
	private TouchSet fTestTouchSet;
	private String fTouchSetPath;

	private LinkedList<RevCommit> A;
	private LinkedList<RevCommit> D;

	private Set<String> fIgnoreFilesTotal; // files in which changes ignored

	private VersionTracker fTracker; // track version of each changed entity
	private String fCallGraphPath; // path to call graph DOT file

	private StaticCallGraphBuilder fCallGraphBuilder; // call graph builder

	public Slicer(ProjectConfiguration config)
			throws RepositoryInvalidException, CommitNotFoundException,
			BuildScriptInvalidException, CoverageControlIOException,
			AmbiguousEndPointException, ProjectConfigInvalidException,
			BranchNotFoundException, CoverageDataMissingException, IOException {

		super(config);

		initializeCompiler(config);

		fResultPath = "/tmp/slice.res";
		

		fTestTouchSet = new TouchSet();
		fTouchSetPath = config.getTouchSetPath();
		fCallGraphPath = config.getCallGraphPath();

		// initialize lists to be used by the slicing algorithm
		D = new LinkedList<RevCommit>();
		A = new LinkedList<RevCommit>(fHistory);
		fTracker = new VersionTracker(A, fComparator);
	}

	private void initializeCompiler(ProjectConfiguration config)
			throws BuildScriptInvalidException, IOException {
		if (config.isClassRootPathSet() && config.isSourceRootPathSet()) {
			// class root path is set by the user
			fClassRootPath = config.getClassRootPath();
		} else {
			fCompiler = new MavenInvokerJacoco(config.getBuildScriptPath(),
					config.isBuilderOutputEnabled());

			// class root path is set by Maven invoker
			fTargetPath = org.eclipse.jgit.util.FileUtils
					.createTempDir("gitref", "-target", null);
			fCompiler.initializeBuild(fTargetPath);

			fClassRootPath = fCompiler.getClassDirPath().getAbsolutePath();
		}
	}

	private void computeTestTouchSet(boolean skip) {
		PrintUtils.print("Computing FUNC & COMP set ... ", TAG.OUTPUT);

		try {
			// do coverage analysis on the latest version
			PrintUtils.print("Running coverage analysis ...", TAG.OUTPUT);
			PrintUtils.print(fCoverage.analysisType());
			StatsUtils.resume("tests.time");
			CoverageDatabase store;
			if (fTests.includeAllTest()) {
				// test scope provided
				store = fCoverage.analyseCoverage();
			} else {
				// run specified tests
				store = fCoverage.analyseCoverage(fTests);
			}
			StatsUtils.stop("tests.time");

			// build static call graph and include compilation dependencies
			PrintUtils.print("Drawing static call graph ...");
			StatsUtils.resume("call.graph.time");

			fCallGraphBuilder = skip ? new BcelStaticCallGraphBuilder()
					: new BcelStaticCallGraphBuilder(fClassRootPath);
			((BcelStaticCallGraphBuilder) fCallGraphBuilder)
					.setRootClasses(store.getPartiallyCoveredClassNames());

			if (fCallGraphPath == null)
				fCallGraphBuilder.buildCallGraph();
			else if (FileUtils.getFile(fCallGraphPath).length() != 0)
				fCallGraphBuilder.loadCallGraph(fCallGraphPath);
			else {
				fCallGraphBuilder.buildCallGraph();
				fCallGraphBuilder.saveCallGraph(fCallGraphPath);
			}

			// fCallGraph.getCallGraph().printCallGraph();
			StatsUtils.stop("call.graph.time");

			// construct FUNC & TEST set
			PrintUtils.print("Adding to touch set ...");
			if (fTouchSetPath != null
					&& fTestTouchSet.loadFromFile(fTouchSetPath))
				return;

			for (SourceCodeEntity c : store.getAllRelevantEntities()) {
				// if (!fCallGraphBuilder.getCallGraph()
				// .hasNode(c.getUniqueName()))
				// continue;

				fTestTouchSet.addToTestSet(c.getUniqueName(), c);
				// compute compilation dependencies using static extended call
				// graph. filter generic type in unique names
				for (CGNode compDep : fCallGraphBuilder.getCallGraph()
						.getTransitiveSuccessors(c.getUniqueName())) {
					fTestTouchSet.addToCompSet(compDep.getName());
				}
			}

		} catch (CoverageControlIOException | ClassPathInvalidException
				| TestFailureException e) {
			PrintUtils.print("Test touch computation failed!",
					PrintUtils.TAG.WARNING);
			e.printStackTrace();
		} finally {
			// clean up
			// fCompiler.cleanUp();
		}

		PrintUtils.print(fTestTouchSet.toString());
		if (fTouchSetPath != null)
			fTestTouchSet.saveToFile(fTouchSetPath);
	}

	/**
	 * Do slicing with the default options.
	 * 
	 * @return a list of {@link RevCommit} to drop
	 * @throws CommitNotFoundException
	 *             if provided commit cannot be found
	 * @throws CoverageControlIOException
	 *             if coverage dump file cannot be read
	 * @throws CoverageDataMissingException
	 *             if coverage data is missing
	 * @throws BuildScriptInvalidException
	 *             if build script is invalid
	 * @throws IOException
	 *             if write JSON file has failed
	 */
	public SlicingResult doSlicing() throws CommitNotFoundException,
			BuildScriptInvalidException, CoverageDataMissingException,
			CoverageControlIOException, IOException {
		return doSlicing(false, false);
	}

	/**
	 * Implementation of the CSLICER semantic slicing algorithm.
	 * 
	 * @param skipHunk
	 *            skip hunk dependency computation
	 * @param skipCallGraph
	 *            skip call graph construction
	 * @return a list of {@link RevCommit} to drop
	 * @throws CommitNotFoundException
	 *             if provided commit cannot be found
	 * @throws CoverageControlIOException
	 *             if coverage dump file cannot be read
	 * @throws CoverageDataMissingException
	 *             if coverage data is missing
	 * @throws BuildScriptInvalidException
	 *             if build script is invalid
	 * @throws IOException
	 *             if write JSON file has failed
	 */
	public SlicingResult doSlicing(boolean skipHunk, boolean skipCallGraph)
			throws CommitNotFoundException, BuildScriptInvalidException,
			CoverageDataMissingException, CoverageControlIOException,
			IOException {

		initializeCoverage(fConfig);
		// compute touched source code entities by the test
		computeTestTouchSet(skipCallGraph);

		StatsUtils.resume("main.algo");

		Collections.reverse(A);
		PrintUtils.breakLine();
		PrintUtils.print("Initial |S| = " + A.size(), PrintUtils.TAG.OUTPUT);
		ChangeExtractor extractor = new ChangeExtractor(fJGit,
				fConfig.getProjectJDKVersion());

		// inspecting commits from newest to oldest
		int i = 0;
		PrintUtils.print("Analysing Commits ... ", TAG.OUTPUT);
		for (RevCommit c : A) {

			PrintUtils.print(
					"=== Inspecting commit: " + commitSummary(c) + " ===");

			Set<GitRefSourceCodeChange> changes;
			try {
				changes = extractor.extractChangesMerge(c);
			} catch (ChangeDistillerException e) {
				PrintUtils.print(
						"Exception occurs in change distilling! Result will be unreliable!",
						TAG.WARNING);
				D.add(c); // drop if exception in distilling
				e.printStackTrace();
				return new SlicingResult();
			}

			// this set is to grow by the entities deleted
			// i.e. the entity has to exist so that it can be deleted later
			Set<SourceCodeEntity> compGrowth = new HashSet<SourceCodeEntity>();

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
				// number of lines changed
				int locChanged = 0;

				// testTouchSet is the set of field/method that are touched
				// by the tests
				// parent entity is field/method/class which contains the
				// change
				if (change instanceof Delete) {
					// do nothing for delete, since it shouldn't appear
					// in the touch set
					// XXX need to consider lookup changes here
					Delete del = (Delete) change;
					uniqueName = del.getChangedEntity().getUniqueName();
					depType = DEP_FLAG.DROP;
					chgType = CHG_TYPE.DEL;
					locChanged = del.getChangedEntity().getEndPosition()
							- del.getChangedEntity().getStartPosition() + 1;
					compGrowth.add(del.getChangedEntity());

				} else if (change instanceof Insert) {
					Insert ins = (Insert) change;
					uniqueName = ins.getChangedEntity().getUniqueName();

					if (fTestTouchSet.hitTestSet(uniqueName)) {
						depType = DEP_FLAG.TEST;
					} else if (fTestTouchSet.hitCompSet(uniqueName)) {
						depType = DEP_FLAG.COMP;
					}

					locChanged = ins.getChangedEntity().getEndPosition()
							- ins.getChangedEntity().getStartPosition() + 1;

					chgType = CHG_TYPE.INS;
				} else if (change instanceof Update) {
					Update upd = (Update) change;
					uniqueName = upd.getNewEntity().getUniqueName();

					// is signature updated?
					boolean signatureChange = !upd.getChangedEntity()
							.getUniqueName().equals(uniqueName);

					assert !signatureChange;

					// grow FUNC set
					if (fTestTouchSet.hitTestSet(uniqueName)) {
						// touchGrowth.add(change.getChangedEntity());
						depType = DEP_FLAG.TEST;
					}
					// XXX verify that change of signature is treated as add
					// and remove!
					else if (fTestTouchSet.hitCompSet(uniqueName)) {
						depType = DEP_FLAG.COMP;
					}

					chgType = CHG_TYPE.UPD;
					// chgType = signatureChange ? CHG_TYPE.SIG_UPD
					// : CHG_TYPE.BODY_UPD;

					locChanged = upd.getChangedEntity().getEndPosition()
							- upd.getChangedEntity().getStartPosition() + 1;

				} else if (change instanceof Move) {
					// shouldn't detect move for structure nodes
					assert false;
				} else
					assert false;

				// track this atomic change
				fTracker.trackAtomicChangeAdd(new AtomicChange(uniqueName,
						filePath, gitChange.getPreImage(),
						gitChange.getPostImage(), i, depType, chgType,
						locChanged));
			}

			// grow touch set if the commit is affecting -- this is to make sure
			// deleted entity exists beforehand.
			if (fTracker.isKeeping(c)) {
				for (SourceCodeEntity g : compGrowth) {
					fTestTouchSet.addToTestSet(g.getUniqueName(), g);
				}
			} else {
				D.add(c); // drop set from newest to oldest
			}

			PrintUtils.print("");
			PrintUtils.printProgress("Slicing history: ", i++ * 100 / A.size());
		}

		StatsUtils.stop("main.algo");

		// ---------------------------------------------------------------
		// compute hunk dependencies
		// the target commit is the latest commit in history
		// ---------------------------------------------------------------
		PrintUtils.breakLine();
		PrintUtils.print("Analysing Hunk Dependency ... ", TAG.OUTPUT);
		Set<RevCommit> keep = new HashSet<RevCommit>(A);
		keep.removeAll(D);

		StatsUtils.resume("hunk.deps.time");
		List<RevCommit> hunkDeps = new ArrayList<RevCommit>();

		if (skipHunk)
			hunkDeps.addAll(keep);
		else {
			// grow hunkDeps set until reaching the fixed-point
			int hunk_count = 0;

			do {
				hunkDeps = computeHunkDepSet(keep);
				fTracker.markHunkDependency(hunkDeps);
				hunk_count = hunkDeps.size();

				// first stage result before hunk analysis
				if (fCallGraphBuilder != null) {
					hunkDeps.addAll(fTracker.computeSlicingResult(
							fCallGraphBuilder.getCallGraph()));
				}

				for (RevCommit d : Collections.unmodifiableList(D)) {
					if (hunkDeps.contains(d)) {
						PrintUtils.print("Hunk depends on: " + commitSummary(d)
								+ " : added back.");
						keep.add(d);
					}
				}
			} while (hunkDeps.size() > hunk_count);
		}

		StatsUtils.stop("hunk.deps.time");

		D.removeAll(keep);

		// ---------------------------------------------------------------
		// output semantic slicing summary
		// ---------------------------------------------------------------
		PrintUtils.breakLine();
		PrintUtils.print("Original |H| " + A.size());
		PrintUtils.print("Tracker:\n" + fTracker.toString(), TAG.DEBUG);
		SlicingResult result = fTracker.getSlicingResult();
		PrintUtils.print("Results:\n" + result, TAG.OUTPUT);

		StatsUtils.setCount("test.count", result.getTestCount());
		StatsUtils.setCount("comp.count", result.getCompCount());
		StatsUtils.setCount("hunk.count", result.getHunkCount());
		StatsUtils.setCount("drop.count", result.getDropCount());
		StatsUtils.setCount("ast.line.count",
				fTracker.getChangedEntityLineCount());
		StatsUtils.setPercentage("change.commit.ratio", fTracker.getChangeCommitRatio());
		// StatsUtils.setCount("commit.line.count",
		// result.getCommitLineCount());
		StatsUtils.setPercentage("reduction.rate",
				result.getReductionRate(false));
		StatsUtils.setPercentage("reduction.hunk",
				result.getReductionRate(true));

		// final files to be reverted

		if (fEnableJson) {
			printJSONResults(result.getPicks(), result.getHunks(),
					new HashMap<String, Double>());
		}

		// clean up
		// fJGit.cleanRepo();

		return result;
	}

	/**
	 * Dummy {@code doSlicing} method where H' is given.
	 * 
	 * @param keep
	 *            sliced sub-history H'
	 * @return H' plus hunk dependencies
	 */
	public List<RevCommit> doSlicing(Set<String> keep) {
		List<RevCommit> hunkDeps = computeHunkDepSetWithId(keep);

		Set<String> hunkId = new HashSet<String>();
		for (RevCommit h : hunkDeps)
			hunkId.add(h.getName());

		List<RevCommit> drop = new LinkedList<RevCommit>();

		Collections.reverse(A);
		for (RevCommit a : A) {
			if (hunkId.contains(a.getName())) {
				PrintUtils.print("hunk: " + commitSummary(a));
				continue;
			}
			if (keep.contains(a.getName())) {
				PrintUtils.print("keep: " + commitSummary(a));
				continue;
			}

			drop.add(a);
			PrintUtils.print("Drop: " + commitSummary(a));
		}

		return drop;
	}

	// enumerate all possible cherry-pickable subsets
	// with respect to existing hunk dependencies among
	// FUNC + COMP commits
	private Set<Set<RevCommit>> getLayeredCommitSet(
			List<Set<RevCommit>> layers) {
		Set<Set<RevCommit>> res = new HashSet<>();

		if (layers == null || layers.size() == 0)
			return res;

		Set<RevCommit> total = new HashSet<>();
		for (Set<RevCommit> l : layers)
			total.addAll(l);

		for (int i = 0; i < layers.size(); i++) {
			Set<RevCommit> top = layers.get(i);
			PrintUtils.print("Layer " + i + " : " + top.size());

			for (Set<RevCommit> s : Sets.powerSet(top)) {
				// avoiding empty set
				if (i == layers.size() - 1 && s.size() == top.size())
					continue;

				if (s.size() > 0)
					res.add(new HashSet<>(Sets.difference(total, s)));
			}

			total.removeAll(layers.get(i));
		}

		assert total.isEmpty();

		return res;
	}

	// Chenguang
	public List<RevCommit> getOriginalCommitList() {
		return A;
	}

	protected Set<String> getReducedSet() {
		Set<String> dIds = new HashSet<String>();
		for (RevCommit d : D)
			dIds.add(commitSummary(d));
		return dIds;
	}

	public final SlicingResult getSlicingReseult() {
		return fTracker.getSlicingResult();
	}

	private void initializeCoverage(ProjectConfiguration config)
			throws BuildScriptInvalidException, CoverageDataMissingException,
			CoverageControlIOException {

		// setup coverage analyzer
		if (config.isClassRootPathSet() && config.isSourceRootPathSet()) {

			// if (config.isJavaSlicerDumpPathSet()) {
			// fCoverage = new CheckedCoverageAnalyzer(
			// config.getJavaSlicerDumpPath(),
			// config.getJavaSlicerCriteria());
			// } else
			if (config.isJacocoExecPathSet())
				fCoverage = new FullCoverageAnalyzer(config.getJacocoExecPath(),
						config.getSourceRootPath(), config.getClassRootPath());
			else
				fCoverage = null;
		} else {
			// if (config.isJavaSlicerDumpPathSet()) {
			// fCoverage = new CheckedCoverageAnalyzer(fCompiler,
			// config.getJavaSlicerDumpPath(),
			// config.getJavaSlicerCriteria());
			// } else {

			fCoverage = (config.isSubModuleSet())
					? new FullCoverageAnalyzer(fCompiler,
							config.getSubModuleBuildScriptPath())
					: new FullCoverageAnalyzer(fCompiler);
			// }
		}
	}

	public SlicingResult loadSlicingResult()
			throws IOException, ClassNotFoundException {
		SlicingResult res;
		FileInputStream fileStream = FileUtils
				.openInputStream(FileUtils.getFile(fResultPath));
		ObjectInputStream objectStream = new ObjectInputStream(fileStream);

		res = (SlicingResult) objectStream.readObject();
		return res;
	}

	public void saveSlicingResult() throws IOException {
		FileOutputStream fileStream = FileUtils
				.openOutputStream(FileUtils.getFile(fResultPath));
		ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
		objectStream.writeObject(getSlicingReseult());
		objectStream.close();
		fileStream.close();
	}

	/**
	 * Further shorten slicing result by enumeration with respect to hunk
	 * dependencies.
	 * 
	 * @param result
	 *            {@link SlicingResult} to shorten
	 * @return a set of shorter slices
	 */
	public Set<List<RevCommit>> shortenSlice(SlicingResult result) {
		DependencyCache cache = new DependencyCache();
		// compute restricted hunk dependency: only on COMP+FUNC
		List<RevCommit> hunks = computeHunkDepSet(result.getFuncComp(), cache);

		assert hunks.size() == result.getTotalCount() - result.getDropCount();

		Set<Set<RevCommit>> candidate = cache
				.applicableSubSets(result.getFuncComp());
		// getLayeredCommitSet(
		// cache.getLayeredDeps(result.getFuncComp(), false));

		// sort candidate snapshots by their length
		List<Set<RevCommit>> snapshots = new ArrayList<>(candidate);
		Collections.sort(snapshots, new Comparator<Set<RevCommit>>() {
			@Override
			public int compare(Set<RevCommit> o1, Set<RevCommit> o2) {
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				return o1.size() - o2.size();
			}
		});

		Set<List<RevCommit>> res = new HashSet<List<RevCommit>>();

		// PrintUtils.print("candidate.count = " + candidate.size());
		StatsUtils.setCount("snapshot.size", candidate.size());

		for (Set<RevCommit> r : snapshots) {
			if (res.size() > 50) {
				PrintUtils.print("Too many branches to try. Quit!");
				break;
			}

			if (!r.isEmpty()) {
				List<RevCommit> h = computeHunkDepSet(r, cache);

				// assert !res.contains(h);
				res.add(h);

				if (fConfig.getSkipPicking())
					continue;

				if (!tryPickCommits(h))
					PrintUtils.print("picking failed!");
			}
		}

		return res;
	}

	/**
	 * Calls on the EGit pull request functionality
	 *
	 *
	 * @return true iff process successful
	 */
	public boolean callPullRequest(List<RevCommit> picks){
		try {
			this.fJGit.pushUpstream(this.fUsername, this.fPassword);
			this.fJGit.pullRequest(this.fUsername, this.fPassword, this.fUpstreamRepo,
					this.fOriginRepo, this.fOriginBranch,
					this.fTitle, this.fBody);
			return true;
		}
		catch (Exception e) {
			PrintUtils.print("Error in creating pull request. " +
							"Please ensure input parameters of upstreamRepo, originRepo, " +
							"branch to request merge from originRepo (originBranch),  GitHub Username and GitHub Password",
					TAG.WARNING);
		}
		return false;
	}

	/**
	 * Creates a pull request with head upstreamRepo:VERIFYTEST and base originRepo:master
	 * Attempts to perform verifyResultPicking on the picks specified by the input result and if successful creates a
	 * branch which is then given as a pull request after pushing to upstreamRepo
	 *
	 * @param result
	 *            {@link SlicingResult} to make into a new branch and then a pull request
	 * @return true iff successful in creating the new branch and pull request
	 */
	public boolean createPullRequest(SlicingResult result) {
		PrintUtils.print("Attempting to Create Branch", TAG.DEBUG);
		List<RevCommit> picks = result.getPicks();
		if (picks.size() > 0){
			if(this.verifyResultPicking(picks)){
				PrintUtils.print("Successfully Picked Relevant Commits: Branch VERIFYTESTS Created. " +
						"Attempting to create pull request", TAG.DEBUG);
				try {
					if (this.callPullRequest(picks)){
						PrintUtils.print("Pull Request Created", TAG.DEBUG);
						return true;
					} else {
						return false;
					}


				} catch (Exception e) {
					PrintUtils.print(e.getStackTrace());
					PrintUtils.print("Error pull request failed", TAG.WARNING);
					return false;
				}
			}
			else {
				PrintUtils.print("Picked commits alone could not be formed (cherrypicked) into a branch " +
						"without errors and hence branch was not created", TAG.WARNING);
				return false;
			}
		} else {
			PrintUtils.print("Picked commits were empty and hence branch not created", TAG.WARNING);
			return false;
		}
	}

	 /**
	 * Display AST differencing results for a commit.
	 * 
	 * @param commitID
	 *            target commit
	 * @throws CommitNotFoundException
	 *             if target commit cannot be found
	 */
	public void showASTDiff(String commitID) throws CommitNotFoundException {
		ChangeExtractor extractor = new ChangeExtractor(fJGit,
				fConfig.getProjectJDKVersion());

		RevCommit target = fJGit.getCommit(commitID);

		try {
			PrintUtils.print("AST Diff for commit: " + commitSummary(target));
			extractor.extractChangesMerge(target);
			PrintUtils.print("ChangeDistiller Diff for commit: "
					+ commitSummary(target));
			extractor.extractChangesPrecise(target);
		} catch (ChangeDistillerException e) {
			PrintUtils.print(
					"Exception occurs in change distilling! Result will be unreliable!",
					TAG.WARNING);
			e.printStackTrace();
		}
	}

	/**
	 * Revert selected files silently.
	 * 
	 * @return {@code true} if revert successes
	 */
	protected boolean tryRevertFiles() {
		try {
			fJGit.checkOutFiles(fIgnoreFilesTotal, fStart);
			return true;
		} catch (CheckoutFileFailedException e) {
			e.printStackTrace();
		}
		return false;
	}
}
