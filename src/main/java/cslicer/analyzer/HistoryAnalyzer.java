package cslicer.analyzer;

import java.io.BufferedWriter;
import java.io.File;

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
import java.io.StringWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONWriter;

import cslicer.analyzer.VersionTracker.Version;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.BuildToolInvoker;
import cslicer.builder.UnitTestScope;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.coverage.ICoverageAnalyzer;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.CreateBranchFailedException;
import cslicer.jgit.DeleteBranchFailedException;
import cslicer.jgit.JGit;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.DependencyCache;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

public class HistoryAnalyzer {

	/**
	 * Comparator based on the natural ordering of {@link Version}. Comparator
	 * return negative value if v1 is before v2.
	 * 
	 * @author Yi Li
	 *
	 */
	class CommitComparator implements Comparator<RevCommit> {

		// original ordering of commits: chronological order
		private List<String> order;

		public CommitComparator(List<RevCommit> history) {
			order = new LinkedList<String>();
			for (RevCommit h : history)
				order.add(h.getName());
		}

		@Override
		public int compare(RevCommit o1, RevCommit o2) {
			if (o1.getId().equals(o2.getId()))
				return 0;

			int p1 = order.lastIndexOf(o1.getName());
			int p2 = order.lastIndexOf(o2.getName());

			// assert p1 != -1 && p2 != -1;

			assert p1 != p2;

			if (p1 > p2)
				return 1;
			else
				return -1;
		}
	}

	public enum PRUNE_STRATEGY {
		NEW_TO_OLD, OLD_TO_NEW, SIGNIFICANCE, RANDOM
	}

	protected CommitComparator fComparator;
	protected static int fSnapCounter; // index for snap shot branch
	protected JGit fJGit;
	protected String fRepoPath;
	protected RevCommit fStart;
	protected RevCommit fEnd; // optional: could be null

	protected String fClassRootPath;
	protected String fTestClassRootPath;
	protected File fTargetPath; // target directory path

	protected List<RevCommit> fHistory;
	protected final UnitTestScope fTests; // name of the target test cases

	protected Stack<String> fSnapShots;
	protected Hashtable<String, LinkedHashMap<RevCommit, RevCommit>> fCommitMap;

	protected BuildToolInvoker fCompiler;
	protected ICoverageAnalyzer fCoverage; // test coverage analyzer

	protected Set<String> fExcludedPaths;

	protected final boolean fEnableJson;
	protected String fJsonPath;

	protected String fOutputPath;

	protected String fOriginURL;

	protected String fUpstreamURL;

	protected final ProjectConfiguration fConfig;

	public HistoryAnalyzer(ProjectConfiguration config)
			throws RepositoryInvalidException, CommitNotFoundException,
			BuildScriptInvalidException, CoverageControlIOException,
			AmbiguousEndPointException, ProjectConfigInvalidException,
			BranchNotFoundException, CoverageDataMissingException, IOException {

		// load configurations
		if (!config.isConsistent())
			throw new ProjectConfigInvalidException();

		fConfig = config;
		fRepoPath = config.getRepositoryPath();
		fOriginURL = config.getOriginURL();
		fUpstreamURL = config.getUpstreamURL();
		fJGit = new JGit(this.fRepoPath);
		// initialization
		fTests = config.getTestCases();
		fExcludedPaths = config.getExcludedPaths();
		fSnapShots = new Stack<String>();
		fSnapShots.push(fJGit.getCurrentBranchName());
		fSnapCounter = 0;

		fEnableJson = config.getEnableJson();
		if (fEnableJson) {
			fJsonPath = config.getJsonPath();
		}

		fOutputPath = Paths.get(Paths.get(fRepoPath).getParent().toString(), ".facts").toString();
		try {
			Files.createDirectory(Paths.get(fOutputPath));
		} catch (FileAlreadyExistsException e) {
			PrintUtils.print(String.format("%s exists, files inside it will be overridden.", fOutputPath),
					PrintUtils.TAG.WARNING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		initializeHistory(config);
		// final List<RevCommit> totalHistory = new LinkedList<>(fHistory);
		// totalHistory.add(0, fStart);
		fComparator = new CommitComparator(fHistory);
		PrintUtils.print("Original |H|: " + fHistory.size());
		// initialize commit map
		initializeCommitMap();
	}

	protected boolean checkCompile() {
		return fCompiler.checkCompilation();
	}

	protected void printJSONResults(List<RevCommit> results,
			List<RevCommit> hunks, Map<String, Double> significance)
			throws IOException {
		// save result to json file
		StringWriter sw = new StringWriter();
		BufferedWriter bw = new BufferedWriter(sw);
		JSONWriter jw = new JSONWriter(bw);
		jw.object();
		jw.key("simple");
		jw.array();
		for (RevCommit c : results) {
			String sha = c.abbreviate(7).name();
			jw.value(sha);
		}
		jw.endArray();

		jw.key("hunk");
		jw.array();
		for (RevCommit c : hunks) {
			String sha = c.abbreviate(7).name();
			jw.value(sha);
		}
		jw.endArray();

		jw.key("full");
		jw.array();
		for (String e : significance.keySet()) {
			jw.array();
			jw.value(e);
			jw.value(String.format("%.2f", significance.get(e)));
			jw.endArray();
		}
		jw.endArray();

		jw.endObject();
		bw.close();
		PrintUtils.print("RESULTS: " + sw.toString(), TAG.OUTPUT);
	}

	/**
	 * Restore repository state. Cleanup generated temporary files. Can only be
	 * called once.
	 */
	public void cleanUp() {
		if (fCompiler == null)
			return;

		try {
			fCompiler.restoreBuildFile();
		} catch (IOException e) {
			PrintUtils.print("Build file not restored!", TAG.WARNING);
		}
		fCompiler.cleanUp();
	}

	public void cleanBranches() throws DeleteBranchFailedException {
		fJGit.checkOutExistingBranch(fSnapShots.firstElement());
		while (fSnapShots.size() > 1) {
			fJGit.deleteBranch(fSnapShots.pop());
		}
	}

	protected String commitSummary(RevCommit c) {
		return c.abbreviate(8).name() + " : " + c.getShortMessage();
	}

	protected List<RevCommit> computeHunkDepSet(
			final Collection<RevCommit> cSet) {
		return computeHunkDepSet(cSet, new DependencyCache());
	}

	protected List<RevCommit> computeHunkDepSet(
			final Collection<RevCommit> cSet, Collection<RevCommit> aSet,
			DependencyCache cache) {
		int i = 0;
		for (RevCommit a : cSet) {
			if (cache.directDepsComputed(a)) {
				continue;
			} else {
				fJGit.findHunkDependencies(a, new HashSet<RevCommit>(aSet),
						cache, fExcludedPaths);
			}

			PrintUtils.printProgress("Computing Hunks: ",
					i++ * 100 / cSet.size());
		}

		// PrintUtils.print(cache);
		if (fConfig.getOutputHunkGraph()) {
			cache.outputCacheToFile(
					FileUtils
							.getFile(FileUtils.getTempDirectory(),
									"cslicer-hunk-deps-graph.dot")
							.getAbsolutePath());
			String hunkFactsFile = Paths.get(fOutputPath, "hunkdep.ta").toString();
			cache.outputCacheToFacts(hunkFactsFile);
		}

		List<RevCommit> res = new LinkedList<RevCommit>();
		// Set<RevCommit> deps = cache.getUnSortedDeps();
		Set<RevCommit> deps = new HashSet<RevCommit>();
		for (RevCommit a : cSet)
			deps.addAll(cache.getTransitiveDeps(a));

		for (RevCommit h : fHistory) {
			if (deps.contains(h))
				res.add(h);
		}

		return res;
	}

	protected List<RevCommit> computeHunkDepSet(
			final Collection<RevCommit> cSet, DependencyCache cache) {
		return computeHunkDepSet(cSet, fHistory, cache);
	}

	public List<RevCommit> computeHunkDepSetWithId(
			final Collection<String> cSet) {
		Set<RevCommit> set = new HashSet<RevCommit>();

		for (String id : cSet) {
			try {
				set.add(fJGit.getCommit(id));
			} catch (CommitNotFoundException e) {
				PrintUtils.print("Provided commit not found in the repo!",
						TAG.WARNING);
				return Collections.emptyList();
			}
		}
		return computeHunkDepSet(set);
	}

	@SuppressWarnings("unused")
	private LinkedHashMap<RevCommit, RevCommit> currentMapping() {
		assert (fCommitMap.keySet().contains(fSnapShots.peek()));
		assert (fCommitMap.get(fSnapShots.peek()) != null);
		return fCommitMap.get(fSnapShots.peek());
	}

	@SuppressWarnings("unused")
	private String currentSnapName() {
		return fSnapShots.peek();
	}

	protected String freshSnapName() {
		return "SNAPSHOT" + (fSnapCounter++);
	}

	/**
	 * Return a view of the partial history starting from {@code commit}
	 * inclusive.
	 * 
	 * @param commit
	 *            the starting {@code RevCommit}
	 * @return list of {@code RevCommit} in the remaining history
	 */
	private List<RevCommit> getRemainingHistory(RevCommit commit) {
		int index = fHistory.indexOf(commit);
		if (index == -1)
			return Collections.emptyList();

		return Collections
				.unmodifiableList(fHistory.subList(index, fHistory.size()));
	}

	private void initializeCommitMap() {
		fCommitMap = new Hashtable<String, LinkedHashMap<RevCommit, RevCommit>>();
		LinkedHashMap<RevCommit, RevCommit> mapping = new LinkedHashMap<RevCommit, RevCommit>();
		for (RevCommit h : fHistory) {
			mapping.put(h, h);
		}
		fCommitMap.put(fSnapShots.peek(), mapping);
	}

	private void initializeHistory(ProjectConfiguration config)
			throws CommitNotFoundException {
		fEnd = fJGit.getCommit(config.getEndCommitId());
		// if optional history length is provided
		if (config.getAnalysisLength() > 0) {
			fHistory = fJGit.getCommitList(fEnd, config.getAnalysisLength(),
					true);
			fStart = fHistory.get(0);
		} else {
			fStart = fJGit.getCommit(config.getStartCommitId());
			// history is a list of commits from target.child -> head
			fHistory = fJGit.getCommitList(fStart, fEnd, true);
		}
	}

	/**
	 * Map {@link RevCommit} to the duplicate on the current snapshot branch.
	 * 
	 * @param origCommit
	 *            {@code RevCommit} on the original branch
	 * @return corresponding commit on the latest snapshot branch
	 */
	protected RevCommit resolveSnapCommit(RevCommit origCommit) {
		RevCommit res = origCommit;
		for (String snap : fSnapShots) {
			if (fCommitMap.get(snap).containsKey(res)
					&& fCommitMap.get(snap).get(res) != null)
				res = fCommitMap.get(snap).get(res);
		}
		return res;
	}

	protected boolean tryPickCommits(final List<RevCommit> toPick,
			final Collection<String> excludes) {
		return tryPickCommits(toPick, freshSnapName(), excludes);
	}

	protected boolean tryPickCommits(final List<RevCommit> toPick) {
		return tryPickCommits(toPick, fExcludedPaths);
	}

	protected boolean tryPickCommits(final List<RevCommit> toPick,
			final String bName, final Collection<String> excludes) {
		if (fConfig.getSkipPicking())
			return false;

		if (toPick.isEmpty())
			return true;

		try {
			// create a new snapshot branch from parent of root
			Ref snap = fJGit.createNewBranch(bName, fStart);
			PrintUtils.print(
					"Begin picking commits on " + snap.getName() + " ...");
			fSnapShots.push(snap.getName());

			// no need to pick fStart
			// toPick.remove(fStart);
			List<RevCommit> noStartPick = new LinkedList<>();
			for (RevCommit p : toPick)
				if (!p.equals(fStart))
					noStartPick.add(p);

			if (!fJGit.pickCommitsToBranch(snap.getName(), noStartPick, false,
					excludes)) {

				undoRemoveCommit(true);

				return false;
			}

			PrintUtils
					.print("Finish picking " + toPick.size() + " commits ...");
			return true;
		} catch (CreateBranchFailedException e) {
			e.printStackTrace();
		}


		return false;
	}

	protected boolean tryRemoveCommit(List<RevCommit> toRemove) {
		if (toRemove.isEmpty())
			return true;

		// XXX assume the toRemove set is sorted from newest to oldest
		RevCommit root = resolveSnapCommit(toRemove.get(toRemove.size() - 1));
		// XXX assume only one parent
		RevCommit parent = fJGit.getParentCommits(root).get(0);

		try {
			PrintUtils.print("Begin removing commits ...");
			PrintUtils.print("Root for removal: " + root.getShortMessage());

			// create a new snapshot branch from parent of root
			Ref snap = fJGit.createNewBranch(freshSnapName(), parent);
			fSnapShots.push(snap.getName());

			LinkedHashMap<RevCommit, RevCommit> mapping = new LinkedHashMap<RevCommit, RevCommit>();
			for (RevCommit p : getRemainingHistory(root)) {
				mapping.put(p, null);
			}

			if (!fJGit.pickCommitsToBranch(snap.getName(), mapping,
					new HashSet<RevCommit>(toRemove))) {
				undoRemoveCommit(false);
				return false;
			}

			fCommitMap.put(snap.getName(), mapping);

			PrintUtils.print("Finishing remove commits ...");
			return true;
		} catch (CreateBranchFailedException e) {
			e.printStackTrace();
		}


		return false;
	}

	/**
	 * Undo commit removal and restore to the original(HEAD) state.
	 * 
	 * @param hard
	 *            reset repository hard
	 */
	protected void undoRemoveCommit(boolean hard) {
		String orighead = fSnapShots.pop();
		assert (fJGit.getCurrentBranchName().equals(orighead));

		if (hard)
			fJGit.resetHeadHard();

		fJGit.checkOutExistingBranch(fSnapShots.peek());
		try {
			fJGit.deleteBranch(orighead);
			PrintUtils.print("Undo " + orighead + " and go back to "
					+ fSnapShots.peek());
		} catch (DeleteBranchFailedException e) {
			PrintUtils.print("Delete of branch " + orighead + " has failed!");
			e.printStackTrace();
		}
	}

	/**
	 * Cherry-picking the computed subset. Verify that there is not conflict.
	 * 
	 * @param pick
	 *            set of {@link RevCommit} to pick
	 * @return {@code true} if slicing does not cause conflict.
	 */
	public boolean verifyResultPicking(List<RevCommit> pick) {
		// clean up working dir
		fJGit.resetHeadHard();

		// try remove commits
		if (!tryPickCommits(pick, "VERIFYTEST", fExcludedPaths))
			return false;

		return true;
	}

	/**
	 * Verify that the sliced history passes the tests.
	 * 
	 * @param pick
	 *            set of {@link RevCommit} to pick
	 * @return {@code true} if the slice does not cause conflict and the tests
	 *         pass
	 */
	public boolean verifyResultTestPassing(List<RevCommit> pick) {
		// clean up working dir
		fJGit.resetHeadHard();

		// try remove commits
		if (!tryPickCommits(pick))
			return false;

		// revert files to be ignored
		// if (!tryRevertFiles())
		// return false;

		// run test
		// XXX assume junit is in the dependencies
		boolean pass = fCompiler.runSingleTest(fTests);
		fCompiler.cleanUp();

		return pass;
	}

	/**
	 * Verify the slicing results with given ID of dropped commits.
	 * 
	 * @param dropId
	 *            a list of commit ID
	 * @return {@code true} if test passes
	 */
	public boolean verifyResultWithId(List<String> dropId) {
		List<RevCommit> drop = new LinkedList<RevCommit>();

		for (String id : dropId) {
			try {
				drop.add(fJGit.getCommit(id));
			} catch (CommitNotFoundException e) {
				PrintUtils.print("Provided commit not found in the repo!",
						TAG.WARNING);
				return false;
			}
		}
		return verifyResultTestPassing(drop);
	}
}
