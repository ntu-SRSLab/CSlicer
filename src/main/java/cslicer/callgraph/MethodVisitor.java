package cslicer.callgraph;

import org.apache.bcel.Repository;

/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReturnInstruction;

import cslicer.utils.BytecodeUtils;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

/**
 * The simplest of method visitors, prints any invoked method signature for all
 * method invocations.
 * 
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
public class MethodVisitor extends EmptyVisitor {

	JavaClass visitedClass;
	private MethodGen mg;
	private ConstantPoolGen cp;
	// private String format;
	// private LineNumberTable lineTable;
	// private int pos;

	private StaticCallGraph fCallGraph;

	public MethodVisitor(MethodGen m, JavaClass jc, StaticCallGraph cg) {
		visitedClass = jc;
		fCallGraph = cg;
		mg = m;
		cp = mg.getConstantPool();
		// format = "M:" + visitedClass.getSourceFileName() + ":"
		// + visitedClass.getClassName() + ":" + mg.getName() + " "
		// + "(%s)%s:%s";
		// lineTable = m.getLineNumberTable(cp);
	}

	public void start() {
		if (mg.isAbstract() || mg.isNative())
			return;
		for (InstructionHandle ih = mg.getInstructionList()
				.getStart(); ih != null; ih = ih.getNext()) {
			Instruction i = ih.getInstruction();

			if (!visitInstruction(i))
				i.accept(this);
		}
	}

	private boolean visitInstruction(Instruction i) {
		short opcode = i.getOpcode();

		return ((InstructionConstants.INSTRUCTIONS[opcode] != null)
				&& !(i instanceof ConstantPushInstruction)
				&& !(i instanceof ReturnInstruction));
	}

	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
		MethodNode callerNode = new MethodNode(mg.getName(), mg.getSignature(),
				visitedClass.getClassName());

		String mName = i.getMethodName(cp);
		String mSig = i.getSignature(cp);
		MethodNode calleeNode = null;

		// PrintUtils.print(i.getReferenceType(cp).toString() + "." + mName +
		// "("
		// + mSig + ")");

//		try {
//			// search callee definition in this class
//			JavaClass referencedClass = Repository.lookupClass(BytecodeUtils
//					.filterReferenceName(i.getReferenceType(cp).toString()));
//			calleeNode = searchMethodDefinition(mName, mSig, referencedClass);
//
//			// search callee definition in super classes
//			if (calleeNode == null) {
//
//				JavaClass[] superClass = Repository
//						.getSuperClasses(referencedClass);
//				for (int j = 0; j < superClass.length; j++) {
//					calleeNode = searchMethodDefinition(mName, mSig,
//							superClass[j]);
//					if (calleeNode != null)
//						break;
//				}
//			}
//		} catch (ClassNotFoundException e) {
//			PrintUtils.print("Callee definition not in application code!",
//					TAG.WARNING);
//		}

		if (calleeNode == null)
			calleeNode = new MethodNode(i.getMethodName(cp), i.getSignature(cp),
					i.getReferenceType(cp).toString());

		fCallGraph.insertEdge(callerNode, calleeNode,
				CGEdgeType.INVOKE_VIRTUAL);
	}

	private MethodNode searchMethodDefinition(String mName, String mSig,
			JavaClass clazz) {
		MethodNode calleeNode = null;
		Method[] definitions = clazz.getMethods();

		for (Method m : definitions) {
			if (m.getName().equals(mName) && m.getSignature().equals(mSig))
				calleeNode = new MethodNode(mName, mSig, clazz.getClassName());
		}
		return calleeNode;
	}

	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
		fCallGraph.insertEdge(
				new MethodNode(mg.getName(), mg.getSignature(),
						visitedClass.getClassName()),
				new MethodNode(i.getMethodName(cp), i.getSignature(cp),
						i.getReferenceType(cp).toString()),
				CGEdgeType.INVOKE_INTERFACE);
	}

	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL i) {
		fCallGraph.insertEdge(
				new MethodNode(mg.getName(), mg.getSignature(),
						visitedClass.getClassName()),
				new MethodNode(i.getMethodName(cp), i.getSignature(cp),
						i.getReferenceType(cp).toString()),
				CGEdgeType.INVOKE_SPECIAL);
	}

	@Override
	public void visitINVOKESTATIC(INVOKESTATIC i) {
		fCallGraph.insertEdge(
				new MethodNode(mg.getName(), mg.getSignature(),
						visitedClass.getClassName()),
				new MethodNode(i.getMethodName(cp), i.getSignature(cp),
						i.getReferenceType(cp).toString()),
				CGEdgeType.INVOKE_STATIC);
	}

	@Override
	public void visitInvokeInstruction(InvokeInstruction i) {

	}

	public void visitGETFIELD(GETFIELD i) {
		fCallGraph.insertEdge(
				new MethodNode(mg.getName(), mg.getSignature(),
						mg.getClassName()),
				new FieldNode(i.getFieldName(cp),
						i.getReferenceType(cp).toString(),
						i.getType(cp).toString()),
				CGEdgeType.FIELD_READ);
	}

	public void visitPUTFIELD(PUTFIELD i) {
		fCallGraph.insertEdge(
				new MethodNode(mg.getName(), mg.getSignature(),
						mg.getClassName()),
				new FieldNode(i.getFieldName(cp),
						i.getReferenceType(cp).toString(),
						i.getType(cp).toString()),
				CGEdgeType.FIELD_WRITE);
	}

	public void visitGETSTATIC(GETSTATIC i) {
		fCallGraph.insertEdge(
				new MethodNode(mg.getName(), mg.getSignature(),
						mg.getClassName()),
				new FieldNode(i.getFieldName(cp),
						i.getReferenceType(cp).toString(),
						i.getType(cp).toString()),
				CGEdgeType.STATIC_READ);
	}

	public void visitPUTSTATIC(PUTSTATIC i) {
		fCallGraph.insertEdge(
				new MethodNode(mg.getName(), mg.getSignature(),
						mg.getClassName()),
				new FieldNode(i.getFieldName(cp),
						i.getReferenceType(cp).toString(),
						i.getType(cp).toString()),
				CGEdgeType.STATIC_WRITE);
	}

	@Override
	public void visitFieldInstruction(FieldInstruction i) {

	}
}
