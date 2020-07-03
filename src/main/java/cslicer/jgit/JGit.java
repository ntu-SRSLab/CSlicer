package cslicer.jgit;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.RepositoryId;


import cslicer.jgit.hunk.HunkDependencyAnalyzer;
import cslicer.utils.DependencyCache;
import cslicer.utils.PathModel;
import cslicer.utils.PathModel.PathChangeModel;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

/**
 * Interface to JGit.
 * 
 * @author Yi Li
 *
 */
public class JGit {

	/**
	 * Length of the abbreviated commit id.
	 */
	public final static int ABBREVIATE_LENGTH = 7;
	private static final int DEFAULT_CONTEXT = 3;
	private Repository fRepo; // Git repository object

	private Git fGit; // Git object
	private DiffFormatter fDiff; // Git diff formatter

	/**
	 * @param path
	 *            path to Git repository
	 * @throws RepositoryInvalidException
	 *             if repository at given path is invalid
	 */
	public JGit(final String path) throws RepositoryInvalidException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			// scan environment GIT_* variables
			// scan up the file system tree
			this.fRepo = builder.setGitDir(new File(path)).readEnvironment()
					.findGitDir().build();

			// initialize a diff formatter
			this.fDiff = new DiffFormatter(DisabledOutputStream.INSTANCE);
			fDiff.setRepository(fRepo);
			fDiff.setContext(DEFAULT_CONTEXT);
			fDiff.setDiffComparator(RawTextComparator.DEFAULT);
			fDiff.setDetectRenames(true); // detect file rename

		} catch (IOException e) {
			throw new RepositoryInvalidException(path, e);
		}
		this.fGit = new Git(fRepo);
	}

	/**
	 * Check whether a commit is contained in a branch.
	 * 
	 * @param branch
	 *            given branch
	 * @param commit
	 *            given commit
	 * @return true if commit is contained in branch
	 */
	public final boolean branchContains(Ref branch, RevCommit commit) {
		boolean contains = false;

		try {
			contains = fGit.branchList().setContains(commit.name()).call()
					.contains(branch);
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return contains;
	}

	/**
	 * Check out an existing branch.
	 * 
	 * @param bName
	 *            branch name
	 */
	public void checkOutExistingBranch(String bName) {
		try {
			fGit.checkout().setName(bName).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checkout files to specific version.
	 * 
	 * @param filePaths
	 *            paths of files to be checked out
	 * @param start
	 *            given commit
	 * @throws CheckoutFileFailedException
	 *             if file check out failed
	 */
	public void checkOutFiles(Collection<String> filePaths, RevCommit start)
			throws CheckoutFileFailedException {
		CheckoutCommand check = fGit.checkout();

		for (String path : filePaths) {
			check = check.addPath(path);
		}

		try {
			check.setStartPoint(start).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			throw new CheckoutFileFailedException(filePaths.toString(), e);
		}
	}

	/**
	 * Checkout the current branch to a specific version.
	 * 
	 * @param start
	 *            {@link RevCommit}
	 * @throws CheckoutFileFailedException
	 *             throws if check out fails.
	 */
	public void checkOutVersion(RevCommit start)
			throws CheckoutFileFailedException {
		CheckoutCommand check = fGit.checkout();

		try {
			check.setName(start.getName()).setCreateBranch(false).call();
			PrintUtils.print("Checkout to: " + start.getShortMessage());
		} catch (GitAPIException e) {
			e.printStackTrace();
			throw new CheckoutFileFailedException(e);
		}
	}

	/**
	 * Check out a new branch.
	 * 
	 * @param bName
	 *            branch name
	 * @param start
	 *            start commit id
	 * @return branch {@link Ref}
	 * @throws CheckoutBranchFailedException
	 *             if branch check out failed
	 */
	public Ref checkOutNewBranch(String bName, RevCommit start)
			throws CheckoutBranchFailedException {
		try {
			return fGit.checkout().setName(bName).setCreateBranch(true)
					.setStartPoint(start).setForce(true).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			throw new CheckoutBranchFailedException(bName, e);
		}
	}

	// cherry-picking a commit excluding a set of paths
	private boolean cherryPickExclude(RevCommit c,
			Collection<String> excludes) {
		try {
			CherryPickResult res = null;

			if (c.getParentCount() > 1) {
				res = fGit.cherryPick().setMainlineParentNumber(1).include(c)
						.setNoCommit(true).call();
			} else {
				res = fGit.cherryPick().include(c).setNoCommit(true).call();
			}

			// revert conflicting and failing files
			if (res.getStatus()
					.equals(CherryPickResult.CherryPickStatus.CONFLICTING)) {
				Set<String> conflicts = fGit.status().call().getConflicting();

				for (String f : conflicts) {
					if (excludes.contains(f) || f.contains("src/test/")) {
						cleanIndexOnPath(f);
					} else
						return false;
				}
			} else if (res.getStatus()
					.equals(CherryPickResult.CherryPickStatus.FAILED)) {

				Map<String, MergeFailureReason> failures = res
						.getFailingPaths();

				for (Map.Entry<String, MergeFailureReason> f : failures
						.entrySet()) {
					String path = f.getKey();
					if (excludes.contains(path) || path.contains("src/test/")) {
						if (f.getValue()
								.equals(MergeFailureReason.COULD_NOT_DELETE)) {
							PrintUtils.print("Cherry picking failed at " + path,
									TAG.WARNING);
							return false;
						}

						cleanIndexOnPath(path);
					} else
						return false;
				}
			}

			fGit.commit().setMessage(c.getFullMessage()).call();

		} catch (GitAPIException e) {
			PrintUtils.print(
					"Picking commit " + c.name() + " results in failure.");
		}

		return true;
	}

	public void cleanIndexOnPath(String f) throws GitAPIException,
			CheckoutConflictException, RefAlreadyExistsException,
			RefNotFoundException, InvalidRefNameException {
		fGit.reset().addPath(f).call();

		// clean untracked directory
		// TODO this will remove target folder
		Set<String> cleaned = fGit.clean().setCleanDirectories(true)
				.setIgnore(true)
				.setPaths(new HashSet<String>(Arrays.asList(f))).call();

		// if f is actually tracked, revert local changes
		if (cleaned.isEmpty())
			fGit.checkout().addPath(f).call();
	}

	/**
	 * Forced garbage collection.
	 */
	public void cleanRepo() {
		try {
			fGit.gc().call();
			fDiff.close();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simply create a new branch without switching to it.
	 * 
	 * @param bName
	 *            branch name
	 * @param start
	 *            start commit
	 * @return branch {@link Ref}
	 * @throws CreateBranchFailedException
	 *             if branch creation failed
	 */
	public Ref createNewBranch(String bName, RevCommit start)
			throws CreateBranchFailedException {
		try {
			return fGit.branchCreate().setName(bName).setStartPoint(start)
					.call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			throw new CreateBranchFailedException(bName, e);
		}
	}

	public void deleteBranch(String bName) throws DeleteBranchFailedException {
		try {
			fGit.branchDelete().setBranchNames(bName).setForce(true).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			throw new DeleteBranchFailedException(bName, e);
		}
	}

	public Set<RevCommit> findHunkDependencies(final RevCommit head,
			final Set<RevCommit> pool) {
		return findHunkDependencies(head, pool, new DependencyCache());
	}

	public Set<RevCommit> findHunkDependencies(RevCommit c,
			final Set<RevCommit> pool, final DependencyCache cache) {
		return findHunkDependencies(c, pool, DEFAULT_CONTEXT, true, cache,
				Collections.<String> emptySet());
	}

	public Set<RevCommit> findHunkDependencies(RevCommit c,
			final Set<RevCommit> pool, final DependencyCache cache,
			Set<String> excludes) {
		return findHunkDependencies(c, pool, DEFAULT_CONTEXT, true, cache,
				excludes);
	}

	/**
	 * Hunk dependency algorithm adopted from the tool {@code git-deps} by Adam
	 * Spiers.
	 * 
	 * @param commit
	 *            a target {@link RevCommit}
	 * @param pool
	 *            a pool of {@link RevCommit} from which to discover hunk
	 *            dependencies
	 * @param context
	 *            size of context
	 * @param recursive
	 *            {@code true} if analyze hunk recursively
	 * @param cache
	 *            cache for hunk dependencies
	 * @param excludes
	 *            paths to excluded files
	 * @return a set of {@link RevCommit} which {@code c} depends on
	 * @see <a href="https://github.com/aspiers/git-deps">git-deps</a>
	 */
	public Set<RevCommit> findHunkDependencies(@Nullable RevCommit commit,
			@Nullable final Set<RevCommit> pool, int context, boolean recursive,
			final DependencyCache cache, final Set<String> excludes) {

		if (commit == null)
			return Collections.emptySet();

		Set<RevCommit> done = new HashSet<RevCommit>();
		List<RevCommit> todoList = new LinkedList<RevCommit>();
		todoList.add(commit);

		// create analyzer
		HunkDependencyAnalyzer hunk = new HunkDependencyAnalyzer(fRepo,
				context);

		while (!todoList.isEmpty()) {
			RevCommit current = todoList.remove(0);

			if (cache.directDepsComputed(current)) {
				continue;
			}

			// find dependencies for c
			Set<RevCommit> deps = new HashSet<RevCommit>();

			// compute dependencies w.r.t. each parent
			for (RevCommit parent : getParentCommits(current, false)) {
				deps.addAll(hunk.blameCommit(current, parent, excludes));
			}

			if (pool != null)
				deps.retainAll(pool); // ignore commits outside of the pool

			// result.addAll(deps); // these are done and added to result
			cache.addDirectDeps(current, deps);
			done.add(current);
			PrintUtils.print("Done: " + current.getShortMessage());

			if (recursive) {
				deps.removeAll(done);
				todoList.addAll(deps);
			}
		}

		// result.retainAll(pool);
		return cache.getTransitiveDeps(commit);
	}

	public Set<RevCommit> findHunkDependencies(final RevCommit head,
			final Set<RevCommit> pool, final int context,
			final boolean recursive, final Set<String> excludes) {
		return findHunkDependencies(head, pool, context, recursive,
				new DependencyCache(), excludes);
	}

	public Set<RevCommit> findHunkDependencies(final Set<RevCommit> pool) {
		RevCommit head = null;
		try {
			head = getCurrentHead();
		} catch (CommitNotFoundException e) {
			PrintUtils.print("HEAD is missing!", TAG.WARNING);
		}
		if (head == null)
			return Collections.emptySet();

		return findHunkDependencies(head, pool, new DependencyCache());
	}

	public Set<RevCommit> findHunkDependencies(final String commit,
			final Set<String> pool) throws CommitNotFoundException {
		RevCommit c = getCommit(commit);
		Set<RevCommit> poolc = null;

		if (pool != null) {
			poolc = new HashSet<RevCommit>();
			for (String p : pool)
				poolc.add(getCommit(p));
		}
		return findHunkDependencies(c, poolc, DEFAULT_CONTEXT, false,
				Collections.<String> emptySet());
	}

	/**
	 * Retrieves a Java Date from a Git commit.
	 *
	 * @param commit
	 *            a target {@link RevCommit}
	 * @return date of the commit or Date(0) if the commit is null
	 */
	public Date getAuthorDate(RevCommit commit) {
		if (commit == null) {
			return new Date(0);
		}
		return commit.getAuthorIdent().getWhen();
	}

	public @Nullable Ref getBranch(String bName)
			throws BranchNotFoundException {
		try {
			List<Ref> bList = fGit.branchList().call();

			for (Ref b : bList) {
				if (b.getName().equals(bName))
					return b;
			}
		} catch (GitAPIException e) {
			e.printStackTrace();
			throw new BranchNotFoundException(bName, e);
		}
		return null;
	}

	/**
	 * Return all local branches.
	 * 
	 * @return branch {@link Ref}
	 */
	public List<Ref> getBranches() {
		try {
			return fGit.branchList().call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return new LinkedList<Ref>();
	}

	@SuppressWarnings("unused")
	private Set<String> getBranchNames() {
		Set<String> ret = new HashSet<String>();

		List<Ref> branches = getBranches();
		for (Ref b : branches)
			ret.add(b.getName());

		return ret;
	}

	public RevCommit getCommit(RevCommit parent) {
		RevWalk rw = new RevWalk(fRepo);
		try {
			parent = rw.parseCommit(parent.getId());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rw.close();
		}
		return parent;
	}

	/**
	 * Returns the specified commit from the repository. If the repository does
	 * not exist or is empty, null is returned.
	 * 
	 * @param objectId
	 *            if unspecified, HEAD is assumed.
	 * @return a {@link RevCommit} object
	 * @throws CommitNotFoundException
	 *             if specified commit cannot be located
	 */
	public final RevCommit getCommit(@Nullable String objectId)
			throws CommitNotFoundException {
		if (!JGitUtils.hasCommits(fRepo)) {
			PrintUtils.print("Repository not valid or contains no commit!",
					TAG.WARNING);
			throw new CommitNotFoundException(objectId);
		}

		RevCommit commit = null;
		RevWalk walk = null;

		try {
			// resolve object id
			ObjectId branchObject;
			if (StringUtils.isEmpty(objectId)
					|| "HEAD".equalsIgnoreCase(objectId)) {
				branchObject = getCurrentBranchId();
			} else {
				branchObject = fRepo.resolve(objectId);
			}

			if (branchObject == null) {
				throw new CommitNotFoundException(objectId);
			}

			walk = new RevWalk(fRepo);
			RevCommit rev = walk.parseCommit(branchObject);
			commit = rev;
		} catch (IOException e) {
			throw new CommitNotFoundException(objectId, e);
		} catch (BranchNotFoundException e) {
			throw new CommitNotFoundException(objectId, e);
		} finally {
			if (walk != null) {
				walk.close();
				walk.dispose();
			}
		}

		return commit;
	}

	/**
	 * Retrieve a {@link Date} object from {@link RevCommit}.
	 *
	 * @param commit
	 *            target {@link RevCommit}
	 * @return date of the commit or {@code Date(0)} if the commit is null
	 */
	public final Date getCommitDate(RevCommit commit) {
		if (commit == null) {
			return new Date(0);
		}
		return new Date(commit.getCommitTime() * 1000L);
	}

	/**
	 * Retrieve a list of history of specific length.
	 * 
	 * @param fEnd
	 *            ending commit
	 * @param analysisLength
	 *            length of history
	 * @param reverse
	 *            a {@code boolean} flag, set to true to reverse the order
	 * @return a list of length n starting backwards from fEnd
	 */
	public List<RevCommit> getCommitList(RevCommit fEnd, int analysisLength,
			boolean reverse) {
		List<RevCommit> ret = new LinkedList<RevCommit>();
		try {
			Iterator<RevCommit> history = fGit.log().add(fEnd)
					.setMaxCount(analysisLength).call().iterator();

			for (; history.hasNext();) {
				ret.add(history.next());
			}
		} catch (MissingObjectException | IncorrectObjectTypeException
				| GitAPIException e) {
			PrintUtils.print("Get commit list failed!", TAG.WARNING);
			e.printStackTrace();
		}
		if (reverse)
			Collections.reverse(ret);

		return ret;
	}

	/**
	 * This list does not include the starting commit.
	 * 
	 * @param start
	 *            starting commit
	 * @param end
	 *            ending commit
	 * @param reverse
	 *            a {@code boolean} flag, set to true to reverse the order
	 * @return a list of {@link RevCommit} objects
	 */
	public final List<RevCommit> getCommitList(RevCommit start, RevCommit end,
			boolean reverse) {
		List<RevCommit> ret = new LinkedList<RevCommit>();
		try {
			Iterator<RevCommit> history = fGit.log().addRange(start, end).call()
					.iterator();

			for (; history.hasNext();) {
				ret.add(history.next());
			}
		} catch (MissingObjectException | IncorrectObjectTypeException
				| GitAPIException e) {
			PrintUtils.print("Get commit list failed!", TAG.WARNING);
			e.printStackTrace();
		}
		if (reverse)
			Collections.reverse(ret);

		return ret;
	}

	/**
	 * Return a list of commits following the given commit.
	 * 
	 * @param start
	 *            given starting commit
	 * @param reverse
	 *            reverse order flag
	 * @return a list of commits
	 * @throws CommitNotFoundException
	 *             if given commit cannot be located
	 * @throws BranchNotFoundException
	 *             if given branch cannot be located
	 * @throws AmbiguousEndPointException
	 *             if end point is ambiguous
	 */
	public final List<RevCommit> getCommitListOnBranch(RevCommit start,
			boolean reverse) throws CommitNotFoundException,
			BranchNotFoundException, AmbiguousEndPointException {
		List<Ref> branches = Collections.emptyList();

		try {
			branches = fGit.branchList().setContains(start.name()).call();
		} catch (GitAPIException e) {
			throw new BranchNotFoundException(start.name(), e);
		}

		if (branches.size() > 1)
			throw new AmbiguousEndPointException("Commit " + start.name()
					+ " is contained in multiple branches."
					+ " Impossible to infer the end point.");

		return getCommitListOnBranch(start, branches.get(0).getName(), reverse);
	}

	/**
	 * Return list of {@link RevCommit} after {@code start} until the end of
	 * that branch.
	 * 
	 * @param start
	 *            starting commit - not included in the list
	 * @param branchName
	 *            name of given branch
	 * @param reverse
	 *            a {@code boolean} flag, set to true to reverse the order
	 * @return a list of {@link RevCommit} objects
	 * @throws CommitNotFoundException
	 *             if given commit cannot be located
	 * @throws BranchNotFoundException
	 *             if given branch cannot be located
	 */
	public final List<RevCommit> getCommitListOnBranch(RevCommit start,
			String branchName, boolean reverse)
			throws CommitNotFoundException, BranchNotFoundException {

		Ref branch = getBranch(branchName);

		if (branch == null) {
			throw new BranchNotFoundException(
					"Branch " + branchName + " is not found.");
		}
		try {
			if (!fGit.branchList().setContains(start.name()).call()
					.contains(branch)) {
				throw new CommitNotFoundException("Commit " + start.name()
						+ " is not found on branch " + branch.getName());
			}
		} catch (GitAPIException e) {
			throw new BranchNotFoundException(start.name(), e);
		}

		return getCommitList(start, getCommit(branch.getObjectId().name()),
				reverse);
	}

	/**
	 * Returns the default branch to use for a repository. Normally returns
	 * whatever branch HEAD points to, but if HEAD points to nothing it returns
	 * the most recently updated branch.
	 *
	 * @return the {@link ObjectId} of a branch
	 * @throws BranchNotFoundException
	 *             if current branch cannot be located
	 */
	public final ObjectId getCurrentBranchId() throws BranchNotFoundException {
		ObjectId object;
		try {
			object = fRepo.resolve(Constants.HEAD);

			if (object == null) {
				throw new BranchNotFoundException("HEAD");
			}
		} catch (RevisionSyntaxException | IOException e) {
			e.printStackTrace();
			throw new BranchNotFoundException("HEAD", e);
		}

		return object;
	}

	/**
	 * @return name of the current branch
	 */
	public String getCurrentBranchName() {
		try {
			return fRepo.getFullBranch();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @return current HEAD {@link RevCommit}
	 * @throws CommitNotFoundException
	 *             if head cannot be located
	 */
	public final RevCommit getCurrentHead() throws CommitNotFoundException {
		return getCommit(Constants.HEAD);
	}

	/**
	 * @param path
	 *            file path
	 * @param commit
	 *            target {@link RevCommit}
	 * @return a {@link PathModel} object for the file; {@code null} if not
	 *         found
	 */
	public final PathModel getFileInPath(final String path, RevCommit commit) {
		if (!JGitUtils.hasCommits(fRepo)) {
			return null;
		}

		final TreeWalk tw = new TreeWalk(fRepo);
		try {
			if (commit == null) {
				commit = getCommit(Constants.HEAD); // use HEAD
			}

			tw.addTree(commit.getTree());
			if (!StringUtils.isEmpty(path)) {
				tw.setRecursive(true);

				while (tw.next()) {
					if (tw.getPathString().equals(path)) {
						return getPathModel(tw, FilenameUtils.getPath(path),
								commit);
					}
				}

			} else
				return null;
		} catch (IOException e) {
			System.err.println(
					"Failed to get files for commit" + commit.getName());
		} catch (CommitNotFoundException e) {
			System.err.println("Failed to get files for commit" + "null");
		} finally {
			tw.close();
		}
		// not found. it's a new file
		return null;
	}

	/**
	 * Returns the list of files changed in a specified commit. If the
	 * repository does not exist or is empty, an empty list is returned.
	 *
	 * @param commit
	 *            if {@code null}, HEAD is assumed.
	 * @return list of {@link PathChangeModel} (files changed) in a commit
	 */
	public List<PathChangeModel> getFilesInCommit(RevCommit commit) {
		List<PathChangeModel> list = new ArrayList<PathChangeModel>();
		if (!JGitUtils.hasCommits(fRepo)) {
			return list;
		}
		RevWalk rw = new RevWalk(fRepo);
		try {
			if (commit == null) {
				ObjectId object = getCurrentBranchId();
				commit = rw.parseCommit(object);
			}

			if (commit.getParentCount() == 0) {
				TreeWalk tw = new TreeWalk(fRepo);
				tw.reset();
				tw.setRecursive(true);
				tw.addTree(commit.getTree());
				TreeFilter s = PathSuffixFilter.create(".java");
				tw.setFilter(s);
				while (tw.next()) {
					list.add(new PathChangeModel(tw.getPathString(),
							tw.getPathString(), 0, tw.getRawMode(0),
							tw.getObjectId(0).getName(),
							commit.getId().getName(), ChangeType.ADD));
				}
				tw.close();
			} else {
				RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
				List<DiffEntry> diffs = fDiff.scan(parent.getTree(),
						commit.getTree());
				for (DiffEntry diff : diffs) {
					// create the path change model
					PathChangeModel pcm = PathChangeModel.from(diff,
							commit.getName());

					list.add(pcm);
				}
			}
		} catch (IOException t) {
			PrintUtils.print("Failed to determine files in commit!",
					PrintUtils.TAG.WARNING);
		} catch (BranchNotFoundException e) {
			e.printStackTrace();
		} finally {
			rw.close();
			rw.dispose();
		}
		return list;
	}

	/**
	 * Returns the list of files in the specified folder at the specified
	 * commit. If the repository does not exist or is empty, an empty list is
	 * returned.
	 * 
	 * @param path
	 *            if unspecified, root folder is assumed
	 * @param commit
	 *            if null, HEAD is assumed.
	 * @return list of {@link PathModel} in specified path
	 * @throws CommitNotFoundException
	 *             if given commit cannot be located
	 */
	public final List<PathModel> getFilesInPath(final String path,
			RevCommit commit) throws CommitNotFoundException {
		List<PathModel> list = new ArrayList<PathModel>();
		if (!JGitUtils.hasCommits(fRepo)) {
			return list;
		}
		if (commit == null) {
			commit = getCommit(Constants.HEAD); // use HEAD
		}
		final TreeWalk tw = new TreeWalk(fRepo);
		try {
			tw.addTree(commit.getTree());
			if (!StringUtils.isEmpty(path)) {
				TreeFilter f = PathFilter.create(path);
				TreeFilter s = PathSuffixFilter.create(".java");
				TreeFilter filter = AndTreeFilter.create(f, s);
				tw.setFilter(filter);
				tw.setRecursive(false);
				boolean foundFolder = false;
				while (tw.next()) {
					if (!foundFolder && tw.isSubtree()) {
						tw.enterSubtree();
					}
					if (tw.getPathString().equals(path)) {
						foundFolder = true;
						continue;
					}
					if (foundFolder) {
						list.add(getPathModel(tw, path, commit));
					}
				}
			} else {
				tw.setRecursive(false);
				while (tw.next()) {
					list.add(getPathModel(tw, null, commit));
				}
			}
		} catch (IOException e) {
			System.err.println(
					"Failed to get files for commit" + commit.getName());
		} finally {
			tw.close();
		}
		Collections.sort(list);
		return list;
	}

	/**
	 * Returns the list of files changed in a specified commit. If the
	 * repository does not exist or is empty, an empty list is returned.
	 *
	 * @param startCommit
	 *            earliest commit
	 * @param endCommit
	 *            most recent commit. if null, HEAD is assumed.
	 * @return list of {@link PathChangeModel} (files changed) in a commit range
	 */
	public final List<PathChangeModel> getFilesInRange(
			final RevCommit startCommit, final RevCommit endCommit) {
		List<PathChangeModel> list = new ArrayList<PathChangeModel>();
		if (!JGitUtils.hasCommits(fRepo)) {
			return list;
		}
		try {
			List<DiffEntry> diffEntries = fDiff.scan(startCommit.getTree(),
					endCommit.getTree());
			for (DiffEntry diff : diffEntries) {
				PathChangeModel pcm = PathChangeModel.from(diff,
						endCommit.getName());
				list.add(pcm);
			}
			Collections.sort(list);
		} catch (IOException t) {
			System.err.println("Failed to determine files in range "
					+ startCommit + " .. " + endCommit + "!");
		}
		return list;
	}

	/**
	 * Returns the list of files changed in a specified commit. If the
	 * repository does not exist or is empty, an empty list is returned.
	 *
	 * @param startCommit
	 *            earliest commit
	 * @param endCommit
	 *            most recent commit. if null, HEAD is assumed.
	 * @return list of {@link PathChangeModel} (files changed) in a commit range
	 */
	public final List<PathChangeModel> getFilesInRange(final String startCommit,
			final String endCommit) {
		List<PathChangeModel> list = new ArrayList<PathChangeModel>();
		if (!JGitUtils.hasCommits(fRepo)) {
			return list;
		}
		try {
			ObjectId startRange = fRepo.resolve(startCommit);
			ObjectId endRange = fRepo.resolve(endCommit);
			RevWalk rw = new RevWalk(fRepo);
			RevCommit start = rw.parseCommit(startRange);
			RevCommit end = rw.parseCommit(endRange);
			list.addAll(getFilesInRange(start, end));
			rw.close();
			rw.dispose();
		} catch (IOException t) {
			System.err.println("Failed to determine files in range "
					+ startCommit + " .. " + endCommit + "!");
		}
		return list;
	}

	public List<RevCommit> getParentCommits(RevCommit new_commit) {
		return getParentCommits(new_commit, false);
	}

	/**
	 * @param c
	 *            target {@link RevCommit}
	 * @param firstParent
	 *            {@code true} if only return the first parent
	 * @return parent {@link RevCommit} of c
	 */
	public final List<RevCommit> getParentCommits(final RevCommit c,
			final boolean firstParent) {
		RevWalk rw = new RevWalk(fRepo);
		List<RevCommit> parents = new LinkedList<RevCommit>();

		try {
			if (firstParent)
				parents.add(rw.parseCommit(c.getParent(0).getId()));
			else
				for (RevCommit p : c.getParents())
					parents.add(rw.parseCommit(p.getId()));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			rw.close();
		}

		return parents;
	}

	/**
	 * Returns a path model of the current file in the treewalk.
	 *
	 * @param tw
	 *            treewalk object
	 * @param basePath
	 *            path to current file
	 * @param commit
	 *            given commit
	 * @return a path model of the current file in the treewalk
	 */
	private PathModel getPathModel(TreeWalk tw, String basePath,
			RevCommit commit) {
		String name;
		long size = 0;
		if (StringUtils.isEmpty(basePath)) {
			name = tw.getPathString();
		} else {
			name = tw.getPathString().substring(basePath.length());
		}
		ObjectId objectId = tw.getObjectId(0);
		try {
			if (!tw.isSubtree() && (tw.getFileMode(0) != FileMode.GITLINK)) {
				size = tw.getObjectReader().getObjectSize(objectId,
						Constants.OBJ_BLOB);
			}
		} catch (IOException e) {
			System.err.println(
					"Failed to retrieve blob size for " + tw.getPathString());
		}
		return new PathModel(name, tw.getPathString(), size,
				tw.getFileMode(0).getBits(), objectId.getName(),
				commit.getName());
	}

	public Repository getRepo() {
		return fRepo;
	}

	/**
	 * Return directory differences (only looking at Java files) given two
	 * versions.
	 * 
	 * @param left
	 *            left commit
	 * @param right
	 *            right commit
	 * @return a list of {@link DiffEntry}
	 */
	public List<DiffEntry> getRepoDirDiff(RevCommit left, RevCommit right) {
		return getRepoDirDiff(left, right, null, DEFAULT_CONTEXT, true);
	}

	/**
	 * @param left
	 *            old {@link RevCommit}, null if no parent
	 * @param right
	 *            new {@link RevCommit}
	 * @param path
	 *            path to target directory
	 * @param context
	 *            size of context
	 * @param onlyJava
	 *            {@code true} if only look at Java files
	 * @return a list of files that are changed
	 */
	public List<DiffEntry> getRepoDirDiff(RevCommit left, RevCommit right,
			String path, int context, boolean onlyJava) {
		List<DiffEntry> diffs = new ArrayList<DiffEntry>();
		TreeFilter f;
		if (path != null)
			f = PathFilter.create(path);
		else
			f = PathFilter.ALL;

		// only look at JAVA files
		TreeFilter s = PathSuffixFilter.create(".java");
		TreeFilter filter = onlyJava ? AndTreeFilter.create(f, s) : f;

		try {
			// use empty tree if no parent
			diffs = fGit.diff()
					.setOldTree(left == null ? new EmptyTreeIterator()
							: JGitUtils.prepareTreeParser(fRepo, left))
					.setNewTree(JGitUtils.prepareTreeParser(fRepo, right))
					.setPathFilter(filter).setContextLines(context)
					.setShowNameAndStatusOnly(false).call();
		} catch (GitAPIException | IOException e) {
			e.printStackTrace();
		}

		return diffs;
	}

	public String getRepoPath() {
		return fRepo.getDirectory().getAbsolutePath();
	}

	/**
	 * Load a file at given path affected by a commit.
	 * 
	 * @param commit
	 *            given commit
	 * @param path
	 *            path to file to be loaded
	 * @return File object corresponding at the given path; null if not affected
	 * @throws CommitNotFoundException
	 *             if given commit cannot be located
	 * @throws IOException
	 *             if I\O exception occurs during file loading
	 */
	public File loadFileInCommit(RevCommit commit, String path)
			throws CommitNotFoundException, IOException {
		assert (!StringUtils.isEmpty(path));

		PathModel file = getFileInPath(path, commit);
		if (file == null)
			return null; // file not found

		ObjectLoader loader = fRepo.open(ObjectId.fromString(file.objectId));
		return writeToFileFromObjectLoader(file.name, commit, loader);
	}

	/**
	 * @param bName
	 *            the name of the existing branch where the commits are picked
	 *            to
	 * @param mapping
	 *            a linked map whose keys should be the commits to be picked in
	 *            order
	 * @param bestEffort
	 *            continue the picking process even upon failure
	 * @return {@code true} if finished successfully
	 */
	// public boolean pickCommitsToBranch(String bName,
	// LinkedHashMap<RevCommit, RevCommit> mapping, boolean bestEffort) {
	// try {
	// fGit.checkout().setName(bName).call();
	//
	// for (RevCommit c : mapping.keySet()) {
	// PrintUtils.print(
	// "Picking " + c.getName() + " : " + c.getShortMessage());
	// CherryPickResult res = fGit.cherryPick().include(c).call();
	//
	// if (!res.getStatus()
	// .equals(CherryPickResult.CherryPickStatus.OK)) {
	// resetHeadHard();
	// if (bestEffort) {
	// PrintUtils.print(
	// "Skipped commit " + c.getShortMessage() + "!");
	// continue;
	// }
	// return false;
	// }
	// // update mapping
	// mapping.put(c, res.getNewHead());
	// }
	//
	// return true;
	// } catch (GitAPIException e) {
	// e.printStackTrace();
	// }
	// return false;
	// }

	/**
	 * Duplicate a list of {@link RevCommit} in order and ignore the
	 * {@link RevCommit} in the ignore set, which is equivalent to remove
	 * commits.
	 * 
	 * @param branch
	 *            name of the existing branch where commits are copied to
	 * @param mapping
	 *            a mapping from original {@link RevCommit} to the duplicated
	 *            ones
	 * @param ignores
	 *            {@link RevCommit} to be ignored/removed
	 * @return {@code true} if successful
	 */
	public boolean pickCommitsToBranch(String branch,
			LinkedHashMap<RevCommit, RevCommit> mapping,
			Set<RevCommit> ignores) {
		Set<ObjectId> ignore_id = new HashSet<ObjectId>();
		for (RevCommit t : ignores)
			ignore_id.add(t.getId());

		try {
			fGit.checkout().setName(branch).call();

			for (RevCommit c : mapping.keySet()) {
				if (ignore_id.contains(c.getId())) {
					PrintUtils.print("Skipping " + c.getName() + " : "
							+ c.getShortMessage());
					continue;
				}

				PrintUtils.print(
						"Picking " + c.getName() + " : " + c.getShortMessage());
				CherryPickResult res = fGit.cherryPick().include(c).call();

				if (!res.getStatus()
						.equals(CherryPickResult.CherryPickStatus.OK)) {
					// print conflict paths
					if (res.getStatus()
							.equals(CherryPickResult.CherryPickStatus.FAILED)) {
						for (Entry<String, MergeFailureReason> conflict : res
								.getFailingPaths().entrySet()) {
							PrintUtils.print("Conflict: " + conflict.getKey(),
									TAG.WARNING);
						}
					} else if (res.getStatus().equals(
							CherryPickResult.CherryPickStatus.CONFLICTING)) {
						PrintUtils.print("Cherry-picking results in conflict!",
								TAG.WARNING);
					}

					resetHeadHard();
					return false;
				}

				// update mapping
				mapping.put(c, res.getNewHead());
			}
			return true;
		} catch (GitAPIException e) {
			PrintUtils.print("Cherry-picking failed!", TAG.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Duplicate a list of {@link RevCommit} to a new branch in order.
	 * 
	 * @param bName
	 *            new branch name
	 * @param commits
	 *            list of {@link RevCommit} to pick
	 * @param withMerge
	 *            consider merge commits
	 * @param excludes
	 *            paths to exclude
	 * @return {@code true} if successful
	 */
	public boolean pickCommitsToBranch(String bName,
			final List<RevCommit> commits, boolean withMerge,
			final Collection<String> excludes) {
		try {
			fGit.checkout().setName(bName).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}

		Iterator<RevCommit> iter = commits.iterator();
		// iter.next(); // skip the first one

		while (iter.hasNext()) {
			RevCommit c = (RevCommit) iter.next();
			PrintUtils.print(
					"Picking " + c.getName() + " : " + c.getShortMessage());

			if (!cherryPickExclude(c, excludes))
				return false;
		}

		return true;
	}

	/**
	 * print info about all commits.
	 */
	public void printRepoLogs() {
		try {
			Iterator<RevCommit> commits = fGit.log().call().iterator();

			RevCommit commit;
			while (commits.hasNext()) {
				commit = commits.next();
				PrintUtils.print(commit.toString(), PrintUtils.TAG.OUTPUT);
			}
		} catch (RevisionSyntaxException | GitAPIException e) {
			PrintUtils.print("Error occured when retriving logs.",
					PrintUtils.TAG.WARNING);
		}
	}

	public boolean resetHeadHard() {
		try {
			fGit.reset().setMode(ResetType.HARD).call();
			return true;
		} catch (GitAPIException e) {
			PrintUtils.print("Reset HEAD failed!", TAG.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Write file to a temporary location.
	 * 
	 * @param name
	 *            file name
	 * @param commit
	 *            version
	 * @param loader
	 *            Git object loader
	 * @return a {@link File} instance representing the temporary directory
	 * @throws IOException
	 *             if I\O exception occurs during file writing
	 */
	private File writeToFileFromObjectLoader(final String name,
			final RevCommit commit, final ObjectLoader loader)
			throws IOException {
		File file = FileUtils.createTempFile(name,
				commit.abbreviate(ABBREVIATE_LENGTH).name(), null);
		file.deleteOnExit();

		OutputStream fout = new FileOutputStream(file);
		loader.copyTo(fout);
		fout.flush();
		fout.close();

		return file;
	}

	public boolean pushUpstream(String username, String password){
		try {
			this.fGit.push().setRemote("upstream").setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();

		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public boolean pullRequest(String username, String password, String upstreamRepo, String originRepo, String originBranch, String title, String body) throws IOException{

		GitHubClient client = new GitHubClient().setCredentials(username, password);
		PullRequestService service = new PullRequestService(client);
		RepositoryService repoService = new RepositoryService(client);
		org.eclipse.egit.github.core.Repository headRepo = repoService.getRepository(RepositoryId.createFromUrl(upstreamRepo));
		org.eclipse.egit.github.core.Repository baseRepo = repoService.getRepository(RepositoryId.createFromUrl(originRepo));
		RepositoryId idBase = RepositoryId.createFromUrl(originRepo);
		PullRequestMarker head = new PullRequestMarker().setRepo(headRepo);
		PullRequestMarker base = new PullRequestMarker().setRepo(baseRepo);
		head.setLabel("VERIFYTEST");
		base.setLabel(originBranch);
		PullRequest request = new PullRequest().setBase(base).setHead(head);
		request.setTitle(title);
		request.setBody(body);
		service.createPullRequest(idBase, request);
		return true;
	}

}
