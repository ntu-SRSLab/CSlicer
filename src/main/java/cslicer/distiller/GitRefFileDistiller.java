package cslicer.distiller;

/*
 * #%L
 * ChangeDistiller
 * %%
 * Copyright (C) 2011 - 2013 Software Architecture and Evolution Lab, Department of Informatics, UZH
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.inject.Inject;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelperFactory;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.distilling.SourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDiffNode;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDifferencer;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDifferencer.DiffType;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;
import cslicer.analyzer.ProjectConfiguration;

/**
 * Distills {@link SourceCodeChange}s between two {@link File}.
 * 
 * @author Beat Fluri
 * @author Giacomo Ghezzi
 */
public class GitRefFileDistiller {

	private DistillerFactory fDistillerFactory;
	private ASTHelperFactory fASTHelperFactory;
	private GitRefRefactoringCandidateProcessor fRefactoringProcessor;

	private List<SourceCodeChange> fChanges;
	private ASTHelper<StructureNode> fLeftASTHelper;
	private ASTHelper<StructureNode> fRightASTHelper;
	private ClassHistory fClassHistory;
	private String fVersion;
	private boolean fIsLeft;
	private boolean fIsRight;
	private SourceCodeChangeClassifier fClassifier;

	@Inject
	GitRefFileDistiller(DistillerFactory distillerFactory,
			ASTHelperFactory factory,
			GitRefRefactoringCandidateProcessor refactoringProcessor,
			SourceCodeChangeClassifier classifier) {
		fDistillerFactory = distillerFactory;
		fASTHelperFactory = factory;
		fRefactoringProcessor = refactoringProcessor;
		fClassifier = classifier;
	}

	/**
	 * Extracts classified {@link SourceCodeChange}s between two {@link File}s.
	 * 
	 * @param left
	 *            file to extract changes
	 * @param right
	 *            file to extract changes
	 */
	public void extractClassifiedSourceCodeChanges(File left, File right) {
		extractClassifiedSourceCodeChanges(left,
				ProjectConfiguration.DEFAULT_JDK, right,
				ProjectConfiguration.DEFAULT_JDK);
	}

	/**
	 * Extracts classified {@link SourceCodeChange}s between two {@link File}s.
	 * 
	 * @param left
	 *            file to extract changes
	 * @param leftVersion
	 *            version of the language in the left file
	 * @param right
	 *            file to extract changes
	 * @param rightVersion
	 *            version of the language in the right file
	 */
	@SuppressWarnings("unchecked")
	public void extractClassifiedSourceCodeChanges(File left,
			String leftVersion, File right, String rightVersion) {

		// one of the files can be null
		assert (left != null || right != null);
		fIsLeft = left != null && right == null;
		fIsRight = right != null && left == null;

		fLeftASTHelper = fIsRight ? null : fASTHelperFactory.create(left,
				leftVersion);
		fRightASTHelper = fIsLeft ? null : fASTHelperFactory.create(right,
				rightVersion);

		extractDifferences();
	}

	private void extractDifferences() {
		StructureDiffNode structureDiff = new StructureDiffNode();

		if (fLeftASTHelper != null && fRightASTHelper != null) {
			StructureDifferencer structureDifferencer = new StructureDifferencer();

			structureDifferencer.extractDifferences(
					fLeftASTHelper.createStructureTree(),
					fRightASTHelper.createStructureTree());
			structureDiff = structureDifferencer.getDifferences();
		} else {
			// single file compared with empty file
			ASTHelper<StructureNode> fASTHelper = fIsLeft ? fLeftASTHelper
					: fRightASTHelper;
			StructureNode node = fASTHelper.createStructureTree();
			structureDiff.setDiffType(fIsLeft ? DiffType.DELETION
					: DiffType.ADDITION);
			structureDiff.setLeft(fIsLeft ? node : null);
			structureDiff.setRight(fIsLeft ? null : node);

			traverseChildren(structureDiff, node, fIsLeft);
		}

		if (structureDiff != null) {
			fChanges = new LinkedList<SourceCodeChange>();
			// first node is (usually) the compilation unit
			processRootChildren(structureDiff);
		} else {
			fChanges = Collections.emptyList();
		}
	}

	private void traverseChildren(StructureDiffNode diff, StructureNode node,
			boolean isLeft) {
		for (StructureNode child : node.getChildren()) {
			StructureDiffNode c = new StructureDiffNode();
			c.setLeft(isLeft ? child : null);
			c.setRight(isLeft ? null : child);
			c.setDiffType(isLeft ? DiffType.DELETION : DiffType.ADDITION);
			diff.addChild(c);
			traverseChildren(c, child, true);
		}
	}

	public void extractClassifiedSourceCodeChanges(File left, File right,
			String version) {
		fVersion = version;
		this.extractClassifiedSourceCodeChanges(left, right);
	}

	private void processRootChildren(StructureDiffNode diffNode) {
		for (StructureDiffNode child : diffNode.getChildren()) {
			if (child.isClassOrInterfaceDiffNode()) {

				if (mayHaveChanges(child.getLeft(), child.getRight())) {
					if (fClassHistory == null) {
						if (fVersion != null) {
							fClassHistory = new ClassHistory(
									fRightASTHelper
											.createStructureEntityVersion(
													child.getRight(), fVersion));
						} else {
							fClassHistory = new ClassHistory(
									fRightASTHelper
											.createStructureEntityVersion(child
													.getRight()));
						}
					}
					processClassDiffNode(child);
				} else if (child.getLeft() == null && child.getRight() != null) {
					if (fClassHistory == null) {
						if (fVersion != null) {
							fClassHistory = new ClassHistory(
									fRightASTHelper
											.createStructureEntityVersion(
													child.getRight(), fVersion));
						} else {
							fClassHistory = new ClassHistory(
									fRightASTHelper
											.createStructureEntityVersion(child
													.getRight()));
						}
					}
					processClassDiffSingleNode(child, false);
				} else if (child.getLeft() != null && child.getRight() == null) {
					if (fClassHistory == null) {
						if (fVersion != null) {
							fClassHistory = new ClassHistory(
									fLeftASTHelper
											.createStructureEntityVersion(
													child.getLeft(), fVersion));
						} else {
							fClassHistory = new ClassHistory(
									fLeftASTHelper
											.createStructureEntityVersion(child
													.getLeft()));
						}
					}
					processClassDiffSingleNode(child, true);
				} else {
					assert false;
				}
			}
		}
	}

	private void processClassDiffNode(StructureDiffNode child) {
		GitRefClassDistiller classDistiller;
		if (fVersion != null) {
			classDistiller = new GitRefClassDistiller(child, fClassHistory,
					fLeftASTHelper, fRightASTHelper, fRefactoringProcessor,
					fDistillerFactory, fVersion);
		} else {
			classDistiller = new GitRefClassDistiller(child, fClassHistory,
					fLeftASTHelper, fRightASTHelper, fRefactoringProcessor,
					fDistillerFactory);
		}
		classDistiller.extractChanges();
		fChanges.addAll(classDistiller.getSourceCodeChanges());
	}

	// diff node contains either left or right, not both
	private void processClassDiffSingleNode(StructureDiffNode child,
			boolean isLeft) {
		GitRefSingleClassDistiller classDistiller;

		if (fVersion != null) {
			classDistiller = new GitRefSingleClassDistiller(child,
					fClassHistory, isLeft ? fLeftASTHelper : fRightASTHelper,
					isLeft, fVersion);
		} else {
			classDistiller = new GitRefSingleClassDistiller(child,
					fClassHistory, isLeft ? fLeftASTHelper : fRightASTHelper,
					isLeft);
		}

		// compute classfied changes
		classDistiller.extractChanges();
		// fChanges.addAll(fClassifier.classifySourceCodeChanges(classDistiller
		// .getSourceCodeChanges()));

		// don't classify
		fChanges.addAll(classDistiller.getSourceCodeChanges());
	}

	private boolean mayHaveChanges(StructureNode left, StructureNode right) {
		return (left != null) && (right != null);
		// return (left != null) || (right != null);
	}

	public List<SourceCodeChange> getSourceCodeChanges() {
		return fChanges;
	}

	public ClassHistory getClassHistory() {
		return fClassHistory;
	}

}
