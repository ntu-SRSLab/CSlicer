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

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Lists;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import cslicer.analyzer.AtomicChange.CHG_TYPE;
import cslicer.analyzer.Refiner.SCHEME;
import cslicer.analyzer.SlicingResult.DEP_FLAG;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.UnitTestScope;
import cslicer.builder.plain.PlainBuilder;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.distiller.ChangeDistillerException;
import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CheckoutBranchFailedException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.StatsUtils;

public class DeltaDebugger extends HistoryAnalyzer {

	private static final String DELTA_DEBUG_BRANCH = "DELTADB";
	private UnitTestScope fScope;
	protected VersionTracker fTracker;

	public DeltaDebugger(ProjectConfiguration config)
			throws RepositoryInvalidException, CommitNotFoundException,
			BuildScriptInvalidException, CoverageControlIOException,
			AmbiguousEndPointException, ProjectConfigInvalidException,
			BranchNotFoundException, CoverageDataMissingException, IOException,
			CheckoutBranchFailedException {
		super(config);

		List<RevCommit> A = new LinkedList<>(fHistory);

		fTracker = new VersionTracker(A, fComparator);
		preProcessHistory();

		fScope = config.getTestCases();

		initializeCompiler(config);

		//fCompiler.restoreBuildFile();
		//fJGit.checkOutNewBranch(DELTA_DEBUG_BRANCH, fEnd);
		//fCompiler.writeBuildFile();
		assert fCompiler.checkCompilation();
		fCompiler.restoreBuildFile();
	}

	private void initializeCompiler(ProjectConfiguration config)
			throws BuildScriptInvalidException {
		fCompiler = new PlainBuilder(fConfig.getBuildScriptPath(), fConfig.getRepositoryPath());
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
		return isTestPass;
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

		// ---------- random partition into size n groups -----------
		for (List<RevCommit> l : Lists.partition(new LinkedList<>(universe),
				universe.size() / n)) {
			// size n set
			res.add(new CommitBundle(new HashSet<>(l), 0));
			// compliment
			Set<RevCommit> compliment = new HashSet<>(
					CollectionUtils.subtract(universe, l));
			res.add(new CommitBundle(compliment, 0));
		}
		// ---------- random partition into size n groups -----------

		return res;
	}

	/**
	 * Main entrance of the delta debugging algorithm.
	 * 
	 * @param scheme
	 *            significance learning scheme.
	 * @return a list of {@link RevCommit}.
	 * @throws IOException
	 *             throws if IO exception happens.
	 * @throws BuildScriptInvalidException
	 *             throws if build script is invalid.
	 */
	public List<RevCommit> doSlicing(SCHEME scheme)
			throws IOException, BuildScriptInvalidException {

		PrintUtils.print("Start Delta Debugging...");

		StatsUtils.resume("total");

		// tried and failed commits
		Set<List<RevCommit>> tried = new HashSet<>();

		int n = 2;
		int triedCount = 0;
		boolean success = true;

		do {
			// increase granularity - avoid overflow of n
			n = success ? n : Math.min(200, 2 * n);
			List<CommitBundle> partitions = partition(n, scheme);
			triedCount = tried.size();

			PrintUtils.print("Tried count = " + triedCount + ", n = " + n);

			do {
				success = false;

				// remove tried partitions
				CommitBundle lowest = partitions.remove(0);
				Set<RevCommit> drop = lowest.getBundle();

				PrintUtils.print("Trying H- (total significance = "
						+ lowest.getSignificance() + "): ");

				for (RevCommit d : drop) {
					PrintUtils.print(
							fHistory.indexOf(d) + " : " + commitSummary(d));
				}

				List<RevCommit> toPick = fTracker.getPresentHistory();
				toPick.removeAll(drop);

				// check if tried before
				if (tried.contains(toPick)) {
					PrintUtils.print("Tried before!");
					continue;
				}

				tried.add(toPick);
				PrintUtils.print("Add to tried: " + toPick.hashCode());

				// check empty
				if (toPick.isEmpty()) {
					// this is going back to base
					// which is assumed to fail
					continue;
				}

				// check hunk
				// can optimize this with reusing cache
				List<RevCommit> hunks = computeHunkDepSet(toPick);
				if (!toPick.containsAll(hunks)) {
					PrintUtils.print("Hunk deps violated!");
					StatsUtils.count("hunk.fail");
					continue;
				}

				if (!tryPickCommits(toPick, fExcludedPaths)) {
					continue;
				}

				if (!execute(drop)) {
					undoRemoveCommit(true);
				} else {
					fTracker.trackCommitDel(drop);
					PrintUtils.print("|H*| = "
							+ fTracker.getPresentCommits().size()
							+ " at Iteration "
							+ StatsUtils.readCount("iteration") + " after "
							+ StatsUtils.readCount("test") + " tests.");

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

		List<RevCommit> minimal = fTracker.getPresentHistory();
		for (RevCommit c : minimal) {
			PrintUtils.print("H*: " + commitSummary(c), TAG.OUTPUT);
		}

		PrintUtils.print("|H*| = " + fTracker.getPresentCommits().size(),
				TAG.OUTPUT);
		StatsUtils.setCount("hstar.length",
				fTracker.getPresentCommits().size());
		
		PrintUtils.print("Important Entities:");
		for(String entity : fTracker.getPresentEntities())
		{
			PrintUtils.print(entity);
		}

		return minimal;
	}
}
