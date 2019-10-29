package cslicer.coverage;

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

import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ISourceFileCoverage;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaASTHelper;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.java.JavaStructureNode.Type;
import cslicer.distiller.GitRefJavaStructureNode;

/**
 * Creates a tree of {@link GitRefJavaStructureNode}s.
 * 
 * @author Beat Fluri
 */
public class ASTCoverageBuilder extends ASTVisitor {

	private Stack<JavaStructureNode> fNodeStack;
	private Stack<char[]> fQualifiers;
	private ISourceFileCoverage fCoverage = null;
	private Scanner fScanner = null;
	private CoverageDatabase fStore;
	private JavaASTHelper fHelper;

	/**
	 * Creates a new Java structure tree builder.
	 * 
	 * @param root
	 *            of the structure tree
	 * @param coverage
	 *            source coverage data
	 * @param scanner
	 *            scanner used by program parser
	 * @param store
	 *            Jacoco coverage storage structure
	 * @param helper
	 *            Java AST helper for changedistiller
	 */
	public ASTCoverageBuilder(JavaStructureNode root,
			ISourceFileCoverage coverage, Scanner scanner, CoverageDatabase store,
			JavaASTHelper helper) {
		this(root);
		fCoverage = coverage;
		fScanner = scanner;
		fStore = store;
		fHelper = helper;
	}

	public ASTCoverageBuilder(JavaStructureNode root) {
		fNodeStack = new Stack<JavaStructureNode>();
		fNodeStack.push(root);
		fQualifiers = new Stack<char[]>();
	}

	@Override
	public boolean visit(CompilationUnitDeclaration compilationUnitDeclaration,
			CompilationUnitScope scope) {
		if (compilationUnitDeclaration.currentPackage != null) {
			for (char[] qualifier : compilationUnitDeclaration.currentPackage.tokens) {
				fQualifiers.push(qualifier);
			}
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		StringBuffer name = new StringBuffer();
		name.append(fieldDeclaration.name);
		name.append(" : ");
		if (fieldDeclaration.type == null && fNodeStack.peek().getType()
				.compareTo(JavaStructureNode.Type.ENUM) == 0) {
			name.append(fNodeStack.peek().getName());
		} else {
			fieldDeclaration.type.print(0, name);
		}

		StructureNodeStatus status = getVariableDeclarationStatus(
				fieldDeclaration);
		// String fieldName = name.toString();
		JavaStructureNode fieldNode = push(Type.FIELD, name.toString(),
				fieldDeclaration);
		// add to coverage store
		fStore.addFieldEntity(fHelper.createSourceCodeEntity(fieldNode),
				status);
		// fStore.coveredFieldVersion.add(fHelper
		// .createStructureEntityVersion(fieldNode));

		return false;
	}

	// compute coverage status
	private StructureNodeStatus getVariableDeclarationStatus(
			AbstractVariableDeclaration fieldDeclaration) {
		if (fCoverage == null || fScanner == null)
			return StructureNodeStatus.UNKNOWN;

		int lineStart = fScanner
				.getLineNumber(fieldDeclaration.declarationSourceStart);
		int lineEnd = fScanner
				.getLineNumber(fieldDeclaration.declarationSourceEnd);

		int lineStatus = ICounter.EMPTY;
		for (int i = lineStart; i <= lineEnd; i++) {
			// System.out.println(i + " : " + fCoverage.getLine(i).getStatus());
			lineStatus = lineStatus | fCoverage.getLine(i).getStatus();
		}
		return flagToStatus(lineStatus);
	}

	private StructureNodeStatus getMethodDeclarationStatus(
			AbstractMethodDeclaration methodDeclaration) {
		if (fCoverage == null || fScanner == null)
			return StructureNodeStatus.UNKNOWN;

		int lineStart = fScanner
				.getLineNumber(methodDeclaration.declarationSourceStart);
		int lineEnd = fScanner
				.getLineNumber(methodDeclaration.declarationSourceEnd);
		int lineStatus = ICounter.EMPTY;

		// System.out.println (fCoverage.getFirstLine() + " -> " +
		// fCoverage.getLastLine());
		// System.out.println (lineStart + ":" + lineEnd);
		// assert (fCoverage.getLastLine() >= lineEnd);
		for (int i = lineStart; i <= lineEnd; i++) {
			lineStatus = lineStatus | fCoverage.getLine(i).getStatus();
		}
		return flagToStatus(lineStatus);
	}

	private StructureNodeStatus getTypeDeclarationStatus(
			TypeDeclaration typeDeclaration) {
		int lineStart = fScanner
				.getLineNumber(typeDeclaration.declarationSourceStart);
		int lineEnd = fScanner
				.getLineNumber(typeDeclaration.declarationSourceEnd);
		int lineStatus = ICounter.EMPTY;
		for (int i = lineStart; i <= lineEnd; i++) {
			lineStatus = lineStatus | fCoverage.getLine(i).getStatus();
		}
		return flagToStatus(lineStatus);
	}

	/**
	 * Convert {@code Jacoco} coverage flag to {@link StructureNodeStatus}.
	 * 
	 * @param lineStatus
	 *            an integer flag returned by Jacoco
	 * @return coverage status
	 */
	private StructureNodeStatus flagToStatus(int lineStatus) {
		switch (lineStatus) {
		case ICounter.EMPTY:
			return StructureNodeStatus.UNKNOWN;
		case ICounter.FULLY_COVERED:
			return StructureNodeStatus.FULLY_COVERED;
		case ICounter.NOT_COVERED:
			return StructureNodeStatus.NOT_COVERED;
		case ICounter.PARTLY_COVERED:
			return StructureNodeStatus.PARTIALLY_COVERED;
		}
		return StructureNodeStatus.UNKNOWN;
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		pop();
	}

	@Override
	public boolean visit(ConstructorDeclaration constructorDeclaration,
			ClassScope scope) {

		StructureNodeStatus status = getMethodDeclarationStatus(
				constructorDeclaration);
		String signature = getMethodSignature(constructorDeclaration);

		JavaStructureNode constructorNode = push(Type.CONSTRUCTOR, signature,
				constructorDeclaration);
		// add to coverage store
		fStore.addMethodEntity(fHelper.createSourceCodeEntity(constructorNode),
				status);

		return false;
	}

	@Override
	public void endVisit(ConstructorDeclaration constructorDeclaration,
			ClassScope scope) {
		pop();
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration,
			ClassScope scope) {
		StructureNodeStatus status = getMethodDeclarationStatus(
				methodDeclaration);
		String signature = getMethodSignature(methodDeclaration);

		JavaStructureNode methodNode = push(Type.METHOD, signature,
				methodDeclaration);
		// add to coverage store
		fStore.addMethodEntity(fHelper.createSourceCodeEntity(methodNode),
				status);

		return false;
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration,
			ClassScope scope) {
		pop();
	}

	@Override
	public boolean visit(TypeDeclaration localTypeDeclaration,
			BlockScope scope) {
		return visit(localTypeDeclaration, (CompilationUnitScope) null);
	}

	@Override
	public void endVisit(TypeDeclaration localTypeDeclaration,
			BlockScope scope) {
		endVisit(localTypeDeclaration, (CompilationUnitScope) null);
	}

	@Override
	public boolean visit(TypeDeclaration memberTypeDeclaration,
			ClassScope scope) {
		return visit(memberTypeDeclaration, (CompilationUnitScope) null);
	}

	@Override
	public void endVisit(TypeDeclaration memberTypeDeclaration,
			ClassScope scope) {
		endVisit(memberTypeDeclaration, (CompilationUnitScope) null);
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration,
			CompilationUnitScope scope) {
		int kind = TypeDeclaration.kind(typeDeclaration.modifiers);
		Type type = null;
		switch (kind) {
		case TypeDeclaration.INTERFACE_DECL:
			type = Type.INTERFACE;
			break;
		case TypeDeclaration.CLASS_DECL:
			type = Type.CLASS;
			break;
		case TypeDeclaration.ANNOTATION_TYPE_DECL:
			type = Type.ANNOTATION;
			break;
		case TypeDeclaration.ENUM_DECL:
			type = Type.ENUM;
			break;
		default:
			assert(false);
		}

		StructureNodeStatus status = getTypeDeclarationStatus(typeDeclaration);

		JavaStructureNode typeNode = push(type,
				String.valueOf(typeDeclaration.name), typeDeclaration);
		fQualifiers.push(typeDeclaration.name);
		// add to coverage store
		fStore.addClassEntity(fHelper.createSourceCodeEntity(typeNode), status);

		return true;
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration,
			CompilationUnitScope scope) {
		pop();
		fQualifiers.pop();
	}

	private String getMethodSignature(
			AbstractMethodDeclaration methodDeclaration) {
		StringBuffer signature = new StringBuffer();
		signature.append(methodDeclaration.selector);
		signature.append('(');
		if (methodDeclaration.arguments != null) {
			for (int i = 0; i < methodDeclaration.arguments.length; i++) {
				if (i > 0) {
					signature.append(",");
				}
				methodDeclaration.arguments[i].type.print(0, signature);
			}
		}
		signature.append(')');
		return signature.toString();
	}

	private JavaStructureNode push(Type type, String name, ASTNode astNode) {
		JavaStructureNode node = new JavaStructureNode(type, getQualifier(),
				name, astNode);

		fNodeStack.peek().addChild(node);
		fNodeStack.push(node);

		return node;
	}

	private String getQualifier() {
		if (!fQualifiers.isEmpty()) {
			StringBuilder qualifier = new StringBuilder();
			for (int i = 0; i < fQualifiers.size(); i++) {
				qualifier.append(fQualifiers.get(i));
				if (i < fQualifiers.size() - 1) {
					qualifier.append('.');
				}
			}
			return qualifier.toString();
		}
		return null;
	}

	private void pop() {
		fNodeStack.pop();
	}

}
