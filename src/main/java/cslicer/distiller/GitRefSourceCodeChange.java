package cslicer.distiller;

import org.eclipse.jgit.revwalk.RevCommit;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

/**
 * A wrapper of the ChangeDistiller {@link SourceCodeChange}
 * 
 * @author Yi Li
 *
 */
public class GitRefSourceCodeChange {

	private SourceCodeChange fChange;
	private String fChangedFilePath;
	private RevCommit fPreImage;
	private RevCommit fPostImage;

	// Chenguang
	private int significanceRank;
	private RevCommit relatedCommit;

	public String getEnclosingClassName() {
		if (fChange.getRootEntity().getType().isClass()) {
			return fChange.getRootEntity().getUniqueName();
		} else if (fChange.getRootEntity().getType().isMethod()) {
			String methodName = fChange.getRootEntity().getUniqueName();
			methodName = methodName.substring(0,methodName.indexOf("("));
			String className = methodName.substring(0,
					methodName.lastIndexOf("."));
			return className;
		} else if (fChange.getRootEntity().getType().isField()) {
			String fieldName = fChange.getRootEntity().getUniqueName();
			String className = fieldName.substring(0,
					fieldName.lastIndexOf("."));
			return className;
		}
		assert false;
		return null;
	}

	public RevCommit getRelatedCommit() {
		return relatedCommit;
	}

	public void setRelatedCommit(RevCommit relatedCommit) {
		this.relatedCommit = relatedCommit;
	}

	public int getSignificanceRank() {
		return significanceRank;
	}

	public void setSignificanceRank(int significanceRank) {
		this.significanceRank = significanceRank;
	}

	public GitRefSourceCodeChange(SourceCodeChange change, RevCommit preImage,
			RevCommit postImage, String changedFilePath) {
		fPreImage = preImage;
		fPostImage = postImage;
		fChange = change;
		fChangedFilePath = changedFilePath;
	}

	public RevCommit getPreImage() {
		return fPreImage;
	}

	public RevCommit getPostImage() {
		return fPostImage;
	}

	/**
	 * Return file path to the changed code entity.
	 * 
	 * @return changed file path
	 */
	public String getChangedFilePath() {
		return fChangedFilePath;
	}

	public boolean isTestFileChange() {
		return fChangedFilePath.contains("src/test/");
	}

	/**
	 * Return the ChangeDistiller {@link SourceCodeChange}.
	 * 
	 * @return {@link SourceCodeChange} object
	 */
	public SourceCodeChange getSourceCodeChange() {
		return fChange;
	}
}
