package cslicer.jgit.hunk;

/*
 * #%L
 * CSlicer
 *    ______ _____  __ _                  
 *   / ____// ___/ / /(_)_____ ___   _____
 *  / /     \__ \ / // // ___// _ \ / ___/
 * / /___  ___/ // // // /__ /  __// /
 * \____/ /____//_//_/ \___/ \___//_/
 * %%
 * Copyright (C) 2014 - 2015 Department of Computer Science, University of Toronto
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

import static org.eclipse.jgit.diff.DiffEntry.Side.NEW;
import static org.eclipse.jgit.diff.DiffEntry.Side.OLD;
import static org.eclipse.jgit.lib.FileMode.GITLINK;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

import cslicer.jgit.JGitUtils;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

/**
 * Analyze hunk dependencies. Modified from the blame command of GIT.
 * 
 * @author Yi Li
 *
 */
public class HunkDependencyAnalyzer {

	/** Magic return content indicating it is empty or no content present. */
	private static final byte[] EMPTY = new byte[] {};

	/** Magic return indicating the content is binary. */
	private static final byte[] BINARY = new byte[] {};

	private int binaryFileThreshold = 52428800;

	private Repository fRepo;
	private Git fGit;
	private ObjectReader fReader;

	private ContentSource.Pair fSource;

	private int fContext;

	public HunkDependencyAnalyzer(Repository repo, int context) {
		fRepo = repo;
		fGit = new Git(repo);
		fReader = fRepo.newObjectReader();
		ContentSource cs = ContentSource.create(fReader);
		fSource = new ContentSource.Pair(cs, cs);
		fContext = context;
	}

	/**
	 * Run git blame on all hunks in the diff between a commit and its parent.
	 * 
	 * @param commit
	 *            target commit
	 * @param parent
	 *            parent commit
	 * @param excludePaths
	 *            paths to exclude
	 * @return a set of earlier commits which the target depends on
	 */
	public Set<RevCommit> blameCommit(RevCommit commit,
			@Nullable RevCommit parent, Collection<String> excludePaths) {
		Set<RevCommit> dependencies = new HashSet<RevCommit>();
		OutputStream out = null;

		releaseReader();

		try {
			// all diffs including other file formats
			List<DiffEntry> diffs = fGit.diff()
					.setOldTree(parent == null ? new EmptyTreeIterator()
							: JGitUtils.prepareTreeParser(fRepo, parent))
					.setNewTree(JGitUtils.prepareTreeParser(fRepo, commit))
					.setContextLines(fContext).setShowNameAndStatusOnly(false)
					.call();

			// for each file in diff
			for (DiffEntry diff : diffs) {

				if (diff.getChangeType() == ChangeType.ADD
						|| diff.getChangeType() == ChangeType.COPY) {
					// add file does not depend on other commits
					continue;
				}

				assert diff.getChangeType() == ChangeType.MODIFY
						|| diff.getChangeType() == ChangeType.DELETE
						|| diff.getChangeType() == ChangeType.RENAME;

				EditList edits;
				if (diff.getOldMode() == GITLINK
						|| diff.getNewMode() == GITLINK) {
					continue;
				} else if (diff.getOldId() == null || diff.getNewId() == null) {
					continue;
				} else {
					// exclude certain paths when computing hunk dependencies
					if (excludePaths.contains(diff.getOldPath())
							|| diff.getOldPath().contains("src/test/")) {
						PrintUtils.print(
								"Excluding " + diff.getOldPath() + " ...");
						continue;
					}

					RawText oldRaw = new RawText(open(OLD, diff));
					RawText newRaw = new RawText(open(NEW, diff));

					DiffAlgorithm diffAlgo = new HistogramDiff();
					edits = diffAlgo.diff(RawTextComparator.DEFAULT, oldRaw,
							newRaw);

					if (edits.isEmpty())
						continue;

					dependencies.addAll(blamePath(diff.getOldPath(), parent,
							regionsFromEdits(edits, oldRaw.size(), fContext)));
				}

			}
		} catch (GitAPIException | IOException e) {
			PrintUtils.print("Blame hunk failed!", TAG.WARNING);
			e.printStackTrace();
		} finally {
			releaseReader();
			IOUtils.closeQuietly(out);
		}

		return dependencies;
	}

	private Region regionsFromEdits(EditList edits, int fileSize, int context) {
		Region head = null, tail = null;
		boolean isFirst = true;

		for (int curIdx = 0; curIdx < edits.size();) {
			Edit curEdit = edits.get(curIdx);
			final int endIdx = findCombinedEnd(edits, curIdx);
			final Edit endEdit = edits.get(endIdx);

			int aCur = Math.max(0, curEdit.getBeginA() - context);
			final int aEnd = Math.min(fileSize, endEdit.getEndA() + context);

			curIdx = endIdx + 1;

			if (isFirst) {
				head = new Region(aCur, aCur, aEnd - aCur);
				isFirst = false;
				tail = head;
				continue;
			}

			tail.next = new Region(aCur, aCur, aEnd - aCur);
			tail = tail.next;
		}

		return head;
	}

	private int findCombinedEnd(final List<Edit> edits, final int i) {
		int end = i + 1;
		while (end < edits.size()
				&& (combineA(edits, end) || combineB(edits, end)))
			end++;
		return end - 1;
	}

	private boolean combineA(final List<Edit> e, final int i) {
		return e.get(i).getBeginA() - e.get(i - 1).getEndA() <= 2 * fContext;
	}

	private boolean combineB(final List<Edit> e, final int i) {
		return e.get(i).getBeginB() - e.get(i - 1).getEndB() <= 2 * fContext;
	}

	private void releaseReader() {
		if (fReader != null)
			fReader.close();
	}

	private byte[] open(DiffEntry.Side side, DiffEntry entry)
			throws IOException {
		if (entry.getMode(side) == FileMode.MISSING)
			return EMPTY;

		if (entry.getMode(side).getObjectType() != Constants.OBJ_BLOB)
			return EMPTY;

		AbbreviatedObjectId id = entry.getId(side);
		assert id.isComplete();
		// if (!id.isComplete()) {
		// Collection<ObjectId> ids = fReader.resolve(id);
		// if (ids.size() == 1) {
		// id = AbbreviatedObjectId.fromObjectId(ids.iterator().next());
		// switch (side) {
		// case OLD:
		// entry.oldId = id;
		// break;
		// case NEW:
		// entry.newId = id;
		// break;
		// }
		// } else if (ids.size() == 0)
		// throw new MissingObjectException(id, Constants.OBJ_BLOB);
		// else
		// throw new AmbiguousObjectException(id, ids);
		// }

		try {

			ObjectLoader ldr = fSource.open(side, entry);
			return ldr.getBytes(binaryFileThreshold);

		} catch (LargeObjectException.ExceedsLimit overLimit) {
			return BINARY;

		} catch (LargeObjectException.ExceedsByteArrayLimit overLimit) {
			return BINARY;

		} catch (LargeObjectException.OutOfMemory tooBig) {
			return BINARY;

		} catch (LargeObjectException tooBig) {
			tooBig.setObjectId(id.toObjectId());
			throw tooBig;
		}
	}

	private Set<RevCommit> blamePath(String path, RevCommit start, Region r) {
		Set<RevCommit> res = new HashSet<RevCommit>();
		GitRefBlameGenerator gen = new GitRefBlameGenerator(fRepo, path, res);
		gen.setFollowFileRenames(false);
		gen.setTextComparator(RawTextComparator.DEFAULT);

		try {
			gen.push(null, start, r);
			while (gen.next())
				;
		} catch (IOException e) {
			PrintUtils.print("Unable to do blame on " + path, TAG.WARNING);
		} finally {
			gen.release();
		}

		return res;
	}
}
