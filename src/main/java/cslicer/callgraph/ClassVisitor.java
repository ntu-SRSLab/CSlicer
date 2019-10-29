package cslicer.callgraph;

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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.codehaus.plexus.util.StringUtils;

/**
 * The simplest of class visitors, invokes the method visitor class for each
 * method found.
 */
public class ClassVisitor extends EmptyVisitor {

	public enum DependencyLevel {
		WHOLE_CLASS, IGNORE_FIELDS, IGNORE_METHODS, IGNORE_FIELDS_METHODS;
	}

	private JavaClass fClass;
	private ConstantPoolGen fConstants;
	// private String fClassReferenceFormat;
	private StaticCallGraph fGraph;
	private ClassNode fCNode;
	private DependencyLevel fLevel;

	public ClassVisitor(JavaClass jc, StaticCallGraph graph,
			DependencyLevel level) {
		fClass = jc;
		fGraph = graph;
		fConstants = new ConstantPoolGen(fClass.getConstantPool());
		fCNode = new ClassNode(fClass.getClassName());
		fLevel = level;
	}

	public void visitJavaClass(JavaClass jc) {
		jc.getConstantPool().accept(this);

		Method[] methods = jc.getMethods();
		for (int i = 0; i < methods.length; i++) {
			methods[i].accept(this);
			if (fLevel != DependencyLevel.IGNORE_METHODS
					&& fLevel != DependencyLevel.IGNORE_FIELDS_METHODS)
				fGraph.insertEdge(fCNode, new MethodNode(methods[i].getName(),
						methods[i].getSignature(), jc.getClassName()),
						CGEdgeType.CLASS_METHOD);
		}

		Field[] fields = jc.getFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].accept(this);
			if (fLevel != DependencyLevel.IGNORE_FIELDS
					&& fLevel != DependencyLevel.IGNORE_FIELDS_METHODS)
				fGraph.insertEdge(fCNode,
						new FieldNode(fields[i].getName(), jc.getClassName(),
								fields[i].getType().toString()),
						CGEdgeType.CLASS_FIELD);
		}
	}

	public void visitConstantPool(ConstantPool constantPool) {
		for (int i = 0; i < constantPool.getLength(); i++) {
			Constant constant = constantPool.getConstant(i);
			if (constant == null)
				continue;
			if (constant.getTag() == Constants.CONSTANT_Class) {
				String referencedClass = constantPool
						.constantToString(constant);

				// skip self reference
				if (fClass.getClassName().equals(referencedClass))
					continue;

				// skip enum synthetic classes
				if (StringUtils.isNumeric(referencedClass
						.substring(referencedClass.lastIndexOf("$") + 1)))
					continue;

				// insert an class reference edge
				fGraph.insertEdge(fCNode, new ClassNode(referencedClass),
						CGEdgeType.CLASS_REFERENCE);
			}
		}
	}

	public void visitMethod(Method method) {
		// skip synthetic methods
		if (method.isSynthetic())
			return;
		
		MethodGen mg = new MethodGen(method, fClass.getClassName(), fConstants);
		MethodVisitor visitor = new MethodVisitor(mg, fClass, fGraph);
		visitor.start();
	}

	public void visitField(Field field) {
		// ignore basic types like int
		if (field.getType() instanceof BasicType)
			return;

		fGraph.insertEdge(new FieldNode(field.getName(), fClass.getClassName(),
				field.getType().toString()), new ClassNode(field.getType()
				.toString()), CGEdgeType.FIELD_REFERENCE);
	}

	public void start() {
		visitJavaClass(fClass);
	}
}
