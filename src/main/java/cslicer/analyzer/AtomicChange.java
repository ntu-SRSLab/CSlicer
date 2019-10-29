package cslicer.analyzer;

import java.util.Objects;

import org.eclipse.jgit.revwalk.RevCommit;

import cslicer.analyzer.AtomicChange.CHG_TYPE;

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

import cslicer.analyzer.SlicingResult.DEP_FLAG;

/**
 * Meta information on an atomic change.
 * 
 * @author Yi Li
 *
 */
public class AtomicChange implements Comparable<AtomicChange> {

	/**
	 * We only consider 3 types of changes: insert, delete and update. All
	 * changes made to identifiers are considered as a delete followed by an
	 * insert automatically. All updates are only on the body contents.
	 * 
	 * @author Yi Li
	 *
	 */
	public enum CHG_TYPE {
		INS, DEL, UPD, /* SIG_UPD is kept for legacy reason */SIG_UPD;
	}

	private final String fId;
	private final String fPath;
	private final RevCommit fPostImageId;
	private final RevCommit fPreImageId;
	private DEP_FLAG fDepType;
	private final CHG_TYPE fChangeType;
	private final int fSeq; // sequence in history
	private final int fChangedLOC;

	public AtomicChange(String id, String path, RevCommit pre, RevCommit post,
			int seq, DEP_FLAG depType, CHG_TYPE chgType, int locChanged) {
		fId = id;
		fPath = path;
		fPreImageId = pre;
		fPostImageId = post;
		fSeq = seq;
		fDepType = depType;
		fChangeType = chgType;
		fChangedLOC = locChanged;
	}

	public AtomicChange(String uniqueName, String filePath, RevCommit preImage,
			RevCommit postImage, int i, DEP_FLAG depType, CHG_TYPE chgType) {
		this(uniqueName, filePath, preImage, postImage, i, depType, chgType,
				-1);
	}

	public boolean isTestFileChange() {
		return fPath.contains("src/test/");
	}

	/**
	 * This {@link AtomicChange} should be kept.
	 * 
	 * @return {@code true} if changed entity is to be kept
	 */
	public boolean isKept() {
		// ignore delete
		if (fChangeType == CHG_TYPE.DEL)
			return false;

		// keep insert if it is in TEST or COMP
		if (fChangeType == CHG_TYPE.INS)
			return fDepType == DEP_FLAG.TEST || fDepType == DEP_FLAG.COMP;

		// now consider updates
		return (fDepType == DEP_FLAG.COMP && fChangeType == CHG_TYPE.SIG_UPD)
				|| fDepType == DEP_FLAG.TEST;
	}

	/**
	 * The dependency type of this {@link AtomicChange}.
	 * 
	 * @return {@link DEP_FLAG} of this {@link AtomicChange}
	 */
	public DEP_FLAG getDependencyType() {
		return fDepType;
	}

	/**
	 * The change type of this {@link AtomicChange}.
	 * 
	 * @return {@link CHG_TYPE} of this {@link AtomicChange}
	 */
	public CHG_TYPE getChangeType() {
		return fChangeType;
	}

	/**
	 * The path to the file that contains this {@link AtomicChange}.
	 * 
	 * @return file path
	 */
	public String getFilePath() {
		return fPath;
	}

	/**
	 * The identifier of the changed entity of this {@link AtomicChange}.
	 * 
	 * @return {@code String} identifier
	 */
	public String getIdentifier() {
		return fId;
	}

	/**
	 * The commits where this {@link AtomicChange} is extracted from.
	 * 
	 * @return {@link RevCommit} commit
	 */
	public RevCommit getPostImage() {
		return fPostImageId;
	}

	public RevCommit getPreImage() {
		return fPreImageId;
	}

	public int getSequence() {
		return fSeq;
	}

	public int getChangedLOC() {
		return fChangedLOC;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + fId + ":" + fChangeType + "," + fDepType + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(fId, fPath, fPreImageId, fPostImageId, fSeq,
				fDepType, fChangeType);
	}

	@Override
	public int compareTo(AtomicChange o) {
		if (!o.getIdentifier().equals(this.getIdentifier()))
			return 0;
		return Integer.compare(this.fSeq, o.getSequence());
	}
}
