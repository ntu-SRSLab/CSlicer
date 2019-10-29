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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureDiffNode;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

/**
 * Extracts changes from a class {@link StructureDiffNode}.
 * 
 * @author Beat Fluri
 * @author Giacomo Ghezzi
 */
public class GitRefSingleClassDistiller {

	private StructureDiffNode fClassDiffNode;
	private ClassHistory fClassHistory;
	private ASTHelper<StructureNode> fASTHelper;
	private StructureEntityVersion fRootEntity;
	// private SourceCodeEntity fParentEntity;
	private List<SourceCodeChange> fChanges;
	private String fVersion;

	private boolean fIsLeft;
	private StructureNode fRootNode;

	/**
	 * Creates a new class distiller.
	 * 
	 * @param classNode
	 *            of which the changes should be extracted
	 * @param classHistory
	 *            to which the changes should be attached
	 * @param aSTHelper
	 *            aids getting info from the AST
	 * @param isLeft
	 *            flag set to {@code true} if file is from left side
	 */
	public GitRefSingleClassDistiller(StructureDiffNode classNode,
			ClassHistory classHistory, ASTHelper<StructureNode> aSTHelper,
			boolean isLeft) {
		fClassDiffNode = classNode;
		fClassHistory = classHistory;
		fASTHelper = aSTHelper;
		fChanges = new LinkedList<SourceCodeChange>();
		fIsLeft = isLeft;
		fRootNode = fIsLeft ? fClassDiffNode.getLeft() : fClassDiffNode
				.getRight();
	}

	/**
	 * Creates a new class distiller.
	 * 
	 * @param classNode
	 *            of which the changes should be extracted
	 * @param classHistory
	 *            to which the changes should be attached
	 * @param aSTHelper
	 *            aids getting info from the AST
	 * @param isLeft
	 *            flag set to {@code true} if file is from left side
	 * @param version
	 *            the number or ID of the version associated to the changes
	 *            being distilled
	 */
	public GitRefSingleClassDistiller(StructureDiffNode classNode,
			ClassHistory classHistory, ASTHelper<StructureNode> aSTHelper,
			boolean isLeft, String version) {
		this(classNode, classHistory, aSTHelper, isLeft);
		fVersion = version;

	}

	/**
	 * Extract the {@link SourceCodeChange}s of the {@link StructureDiffNode}
	 * with which the class distiller was initialized.
	 */
	public void extractChanges() {
		// fParentEntity = fASTHelper.createSourceCodeEntity(fRootNode);

		if (fVersion != null) {
			fRootEntity = fASTHelper.createStructureEntityVersion(fRootNode,
					fVersion);
		} else {
			fRootEntity = fASTHelper.createStructureEntityVersion(fRootNode);
		}
		processDeclarationChanges(fClassDiffNode, fRootEntity);
		fChanges.addAll(fRootEntity.getSourceCodeChanges());

		cleanupInnerClassHistories();
	}

	private void cleanupInnerClassHistories() {
		for (Iterator<ClassHistory> it = fClassHistory.getInnerClassHistories()
				.values().iterator(); it.hasNext();) {
			ClassHistory ch = it.next();
			if (!ch.hasChanges()) {
				it.remove();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processDeclarationChanges(StructureDiffNode diffNode,
			StructureEntityVersion rootEntity) {
		Node root = fASTHelper.createDeclarationTree(fRootNode);

		for (Enumeration<Node> nodes = root.postorderEnumeration(); nodes
				.hasMoreElements();) {
			Node n = nodes.nextElement();
			SourceCodeEntity parent = null;

			if (((Node) n.getParent()) != null)
				parent = ((Node) n.getParent()).getEntity();
			else
				parent = root.getEntity();

			fChanges.add(fIsLeft ? new Delete(rootEntity, n.getEntity(), parent)
					: new Insert(rootEntity, n.getEntity(), parent));
		}
	}

	public List<? extends SourceCodeChange> getSourceCodeChanges() {
		return fChanges;
	}
}
