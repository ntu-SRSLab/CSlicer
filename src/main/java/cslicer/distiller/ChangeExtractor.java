package cslicer.distiller;

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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import cslicer.analyzer.ProjectConfiguration;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.JGit;
import cslicer.jgit.JGitUtils;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.StatsUtils;

public class ChangeExtractor {
	private JGit jgit;
	private String path;
	private String fJDKVersion = ProjectConfiguration.DEFAULT_JDK;

	public ChangeExtractor(String _p) throws RepositoryInvalidException {
		path = _p;
		jgit = new JGit(path);
	}

	public ChangeExtractor(JGit _git, String jdk) {
		jgit = _git;
		path = _git.getRepoPath();
		fJDKVersion = jdk;
	}

	public ChangeExtractor(JGit _git) {
		jgit = _git;
		path = _git.getRepoPath();
	}

	public Set<GitRefSourceCodeChange> extractChangesPrecise(
			RevCommit new_commit)
			throws CommitNotFoundException, ChangeDistillerException {
		Set<GitRefSourceCodeChange> ret = new HashSet<>();

		if (new_commit.getParentCount() == 1) {
			// repeat this for each parent for merge commit
			ret.addAll(extractParentDiffPrecise(
					jgit.getCommit(new_commit.getParent(0)), new_commit));
		}

		return ret;
	}

	public Set<GitRefSourceCodeChange> extractParentDiffPrecise(
			RevCommit old_commit, RevCommit new_commit)
			throws ChangeDistillerException, CommitNotFoundException {

		List<DiffEntry> diffs = jgit.getRepoDirDiff(old_commit, new_commit);
		Set<GitRefSourceCodeChange> changes = new HashSet<>();

		for (DiffEntry diff : diffs) {
			try {
				String changedFilePath = null;
				PreciseDistiller distiller = null;

				if (diff.getChangeType() == ChangeType.MODIFY) {
					// file modifications
					PrintUtils.print(
							"--- Modify File: " + diff.getNewPath() + " ---");
					File leftFile = jgit.loadFileInCommit(old_commit,
							diff.getOldPath());
					File rightFile = jgit.loadFileInCommit(new_commit,
							diff.getNewPath());

					distiller = new PreciseDistiller(leftFile, rightFile,
							fJDKVersion);

					assert (diff.getOldPath().equals(diff.getNewPath()));
					changedFilePath = diff.getOldPath();

					Set<String> possibleSet = distiller
							.getPossibleUpdatedEntityNames();
					PrintUtils.print("================================");
					for (String s : possibleSet) {
						PrintUtils.print(s);
					}
					PrintUtils.print("================================");

					PrintUtils.print(distiller.getUpdatedEntityNames());
					// ret.addAll(distiller.getAtomicChanges());
					for (SourceCodeChange change : distiller
							.getAtomicChanges()) {
						// ret.add(change);
						changes.add(new GitRefSourceCodeChange(change,
								old_commit, new_commit, changedFilePath));
					}
				}
			} catch (IOException e) {
				PrintUtils.print(
						"IO exception occoured during change extraction",
						TAG.WARNING);
				continue;
			}
		}

		return changes;
	}

	/**
	 * @param new_commit
	 *            the target commit with respect to which changes are extracted
	 * @return a set of {@link SourceCodeChange}
	 * @throws CommitNotFoundException
	 *             if given commit cannot be located
	 * @throws ChangeDistillerException
	 *             if changedistiller exception occurs during extraction
	 */
	public Set<GitRefSourceCodeChange> extractChanges(RevCommit new_commit)
			throws CommitNotFoundException, ChangeDistillerException {
		StatsUtils.resume("extract.time");
		Set<GitRefSourceCodeChange> ret = new HashSet<GitRefSourceCodeChange>();

		if (new_commit.getParentCount() == 1) {
			// repeat this for each parent for merge commit
			ret.addAll(extractParentDiff(
					jgit.getCommit(new_commit.getParent(0)), new_commit));
		} else if (new_commit.getParentCount() == 2) {
			// merge commit: only keep resolution change
			RevCommit mergeBase = JGitUtils.getMergeBase(jgit.getRepo(),
					jgit.getCommit(new_commit.getParent(0)),
					jgit.getCommit(new_commit.getParent(1)));

			for (RevCommit old_commit : jgit.getParentCommits(new_commit)) {
				ret.addAll(extractParentDiff(old_commit, new_commit));
			}

			PrintUtils.print("Before: " + ret.size());
			for (RevCommit old_commit : jgit.getParentCommits(new_commit)) {
				Set<GitRefSourceCodeChange> branch = extractParentDiff(
						mergeBase, old_commit);

				for (GitRefSourceCodeChange b : branch) {
					for (GitRefSourceCodeChange r : ret) {
						if (souceCodeChangeEquals(b.getSourceCodeChange(),
								r.getSourceCodeChange())) {
							ret.remove(r);
							break;
						}
					}
				}
				// ret.removeAll(branch);
			}
			PrintUtils.print("After: " + ret.size());
		} else {
			// root commit: this should not happen
			assert false;
		}

		StatsUtils.stop("extract.time");

		return ret;
	}

	public Set<GitRefSourceCodeChange> extractChangesMerge(RevCommit new_commit)
			throws CommitNotFoundException, ChangeDistillerException {
		StatsUtils.resume("extract.time");
		Set<GitRefSourceCodeChange> ret = new HashSet<GitRefSourceCodeChange>();

		if (new_commit.getParentCount() == 1) {
			// repeat this for each parent for merge commit
			ret.addAll(extractParentDiff(
					jgit.getCommit(new_commit.getParent(0)), new_commit));
		} else if (new_commit.getParentCount() == 2) {
			ret.addAll(extractParentDiff(
					jgit.getCommit(new_commit.getParent(1)), new_commit));
		} else {
			// root commit: this should not happen
			assert false;
		}

		StatsUtils.stop("extract.time");

		return ret;
	}

	private boolean souceCodeChangeEquals(SourceCodeChange left,
			SourceCodeChange right) {
		// equals of StructureEntityVersion is not implemented correctly
		// XXX hack
		if (!left.getChangeType().equals(right.getChangeType()))
			return false;
		if (!left.getChangedEntity().equals(right.getChangedEntity()))
			return false;
		if (!left.getParentEntity().equals(right.getParentEntity()))
			return false;
		if (!left.getRootEntity().toString()
				.equals(right.getRootEntity().toString()))
			return false;
		return true;
	}

	public Set<GitRefSourceCodeChange> extractParentDiff(RevCommit old_commit,
			RevCommit new_commit)
			throws CommitNotFoundException, ChangeDistillerException {
		List<DiffEntry> diffs = jgit.getRepoDirDiff(old_commit, new_commit);
		// Set<SourceCodeChange> ret = new HashSet<SourceCodeChange>();
		Set<GitRefSourceCodeChange> changes = new HashSet<GitRefSourceCodeChange>();

		for (DiffEntry diff : diffs) {

			// ignore test files:
			if (diff.getOldPath().contains("src/test/")
					|| diff.getNewPath().contains("src/test/"))
				continue;

			try {
				GitRefDistiller distiller = null;
				String changedFilePath = null;

				if (diff.getChangeType() == ChangeType.MODIFY) {
					// file modifications
					PrintUtils.print(
							"--- Modify File: " + diff.getNewPath() + " ---");
					File leftFile = jgit.loadFileInCommit(old_commit,
							diff.getOldPath());
					File rightFile = jgit.loadFileInCommit(new_commit,
							diff.getNewPath());
					distiller = new GitRefDistiller(leftFile, rightFile,
							fJDKVersion);

					assert (diff.getOldPath().equals(diff.getNewPath()));
					changedFilePath = diff.getOldPath();

				} else if (diff.getChangeType() == ChangeType.ADD) {
					// add new file, keep new file
					PrintUtils.print(
							"--- New File: " + diff.getNewPath() + " ---");
					File rightFile = jgit.loadFileInCommit(new_commit,
							diff.getNewPath());
					distiller = new GitRefDistiller(null, rightFile,
							fJDKVersion);

					changedFilePath = diff.getNewPath();

				} else if (diff.getChangeType() == ChangeType.DELETE) {
					// delete file, keep old file
					PrintUtils.print(
							"--- Delete File: " + diff.getOldPath() + " ---");

					File leftFile = jgit.loadFileInCommit(old_commit,
							diff.getOldPath());
					distiller = new GitRefDistiller(leftFile, null,
							fJDKVersion);

					changedFilePath = diff.getOldPath();
				}

				distiller.printChanges();
				// ret.addAll(distiller.getAtomicChanges());
				for (SourceCodeChange change : distiller.getAtomicChanges()) {
					// ret.add(change);
					changes.add(new GitRefSourceCodeChange(change, old_commit,
							new_commit, changedFilePath));
				}

			} catch (IOException e) {
				PrintUtils.print(
						"IO exception occoured during change extraction",
						TAG.WARNING);
				continue;
			}
		}

		return changes;
	}

	public void extractChanges(String commitID)
			throws CommitNotFoundException, ChangeDistillerException {
		RevCommit commit = jgit.getCommit(commitID);
		extractChanges(commit);
	}
}
