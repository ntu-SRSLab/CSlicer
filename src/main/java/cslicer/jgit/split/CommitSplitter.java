package cslicer.jgit.split;


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

import static org.eclipse.jgit.diff.DiffEntry.Side.NEW;
import static org.eclipse.jgit.diff.DiffEntry.Side.OLD;
import static org.eclipse.jgit.lib.FileMode.GITLINK;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.ContentSource;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.FileHeader.PatchType;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.JGitUtils;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

public class CommitSplitter {
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

	private Map<GitRefSourceCodeChange, String> changePatchMap;
	private Map<String, Set<GitRefSourceCodeChange>> patchChangeMap;

	public Map<GitRefSourceCodeChange, String> getChangePatchMap() {
		return changePatchMap;
	}

	public void setChangePatchMap(
			Map<GitRefSourceCodeChange, String> changePatchMap) {
		this.changePatchMap = changePatchMap;
	}

	public Map<String, Set<GitRefSourceCodeChange>> getPatchChangeMap() {
		return patchChangeMap;
	}

	public void setPatchChangeMap(
			Map<String, Set<GitRefSourceCodeChange>> patchChangeMap) {
		this.patchChangeMap = patchChangeMap;
	}

	public CommitSplitter(Repository repo, int context) {
		fRepo = repo;
		fGit = new Git(repo);
		fReader = fRepo.newObjectReader();
		ContentSource cs = ContentSource.create(fReader);
		fSource = new ContentSource.Pair(cs, cs);
		fContext = context;

		changePatchMap = new HashMap<>();
		patchChangeMap = new HashMap<>();
	}

	private void releaseReader() {
		if (fReader != null)
			fReader.close();
	}

	public void splitCommit(RevCommit commit, @Nullable RevCommit parent,
			Collection<String> excludePaths,
			Set<GitRefSourceCodeChange> changeSet) {
		OutputStream out = null;

		releaseReader();

		// Map<GitRefSourceCodeChange, String> patchMap = new HashMap<>();
		DiffFormatter dformatter = new DiffFormatter(
				DisabledOutputStream.INSTANCE);
		dformatter.setRepository(fRepo);

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

				String diffPath = "";
				if (diff.getChangeType() == ChangeType.DELETE) {
					diffPath = diff.getOldPath();
				} else {
					diffPath = diff.getNewPath();
				}

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

					System.out.println(edits.size());

					FileHeader fh = dformatter.toFileHeader(diff);

					for (GitRefSourceCodeChange change : changeSet) {
						if (!change.getChangedFilePath().equals(diffPath)) {
							continue;
						}

						EditList newEditList = new EditList();

						System.out.println(
								change.getSourceCodeChange().toString());
						int changeStart = change.getSourceCodeChange()
								.getChangedEntity().getSourceRange().getStart();
						int changeEnd = change.getSourceCodeChange()
								.getChangedEntity().getSourceRange().getEnd();
						System.out.println("[CHANGE START]: " + changeStart);
						System.out.println("[CHANGE END]: " + changeEnd);

						Iterator<Edit> itr = edits.iterator();
						while (itr.hasNext()) {
							Edit edit = itr.next();
							System.out
									.println("[BEGIN A]: " + edit.getBeginA());
							System.out.println("[END A]: " + edit.getEndA());
							System.out
									.println("[BEGIN B]: " + edit.getBeginB());
							System.out.println("[END B]: " + edit.getEndB());

							if (edit.getType() == Type.INSERT) {
								// beginA == endA, beginB < endB
								if (edit.getBeginB() >= changeStart
										&& edit.getBeginB() <= changeEnd
										|| edit.getEndB() >= changeStart
												&& edit.getEndB() <= changeEnd
										|| changeStart >= edit.getBeginB()
												&& changeEnd <= edit
														.getEndB()) {
									newEditList.add(edit);
								}
							} else if (edit.getType() == Type.DELETE) {
								// beginA < endA, beginB == endB
								if (edit.getBeginA() >= changeStart
										&& edit.getBeginA() <= changeEnd
										|| edit.getEndA() >= changeStart
												&& edit.getEndA() <= changeEnd
										|| changeStart >= edit.getBeginA()
												&& changeEnd <= edit
														.getEndA()) {
									newEditList.add(edit);
								}
							} else if (edit.getType() == Type.REPLACE) {
								// beginA < endA, beginB < endB
								if (edit.getBeginA() >= changeStart
										&& edit.getBeginA() <= changeEnd
										|| edit.getEndA() >= changeStart
												&& edit.getEndA() <= changeEnd
										|| edit.getBeginB() >= changeStart
												&& edit.getBeginB() <= changeEnd
										|| edit.getEndB() >= changeStart
												&& edit.getEndB() <= changeEnd
										|| changeStart >= edit.getBeginA()
												&& changeEnd <= edit.getEndA()
										|| changeStart >= edit.getBeginB()
												&& changeEnd <= edit
														.getEndB()) {
									newEditList.add(edit);
								}
							} else // Type == Empty
							{

							}
							// System.out.println(newEditList.size());
						}
						FileHeader newFileHeader = new FileHeader(
								fh.getBuffer(), newEditList, PatchType.UNIFIED);
						System.out.println(
								"[NEW LIST SIZE]: " + newEditList.size());

						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						DiffFormatter fmt = new DiffFormatter(bos);
						fmt.setRepository(fRepo);
						fmt.format(newEditList, oldRaw, newRaw);
						byte[] patch = bos.toByteArray();
						String patchStr = newFileHeader.getScriptText()
								+ new String(patch);

						changePatchMap.put(change, patchStr);

						if (patchChangeMap.get(patchStr) == null) {
							Set<GitRefSourceCodeChange> changes = new LinkedHashSet<>();
							changes.add(change);
							patchChangeMap.put(patchStr, changes);
						} else {
							Set<GitRefSourceCodeChange> changes = patchChangeMap
									.get(patchStr);
							changes.add(change);
						}
					}

				}

			}
		} catch (GitAPIException | IOException e) {
			PrintUtils.print("Blame hunk failed!", TAG.WARNING);
			e.printStackTrace();
		} finally {
			releaseReader();
			IOUtils.closeQuietly(out);
		}
		dformatter.close();
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

}
