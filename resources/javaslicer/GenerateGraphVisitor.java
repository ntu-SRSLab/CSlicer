package cslicer.coverage;

/*
 * #%L
 * CSlicer
 *    ______ _____  __ _                  
 *   / ____// ___/ / /(_)_____ ___   _____
 *  / /     \__ \ / // // ___// _ \ / ___/
 * / /___  ___/ // // // /__ /  __// /
 * \____/ /____//_//_/ \___/ \___//_/
 * %%
 * Copyright (C) 2014 - 2016 Department of Computer Science, University of Toronto
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

import java.util.HashSet;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import cslicer.utils.BytecodeUtils;
import cslicer.utils.graph.Graph;
import cslicer.utils.graph.Vertex;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.slicing.SliceVisitor;
import de.unisb.cs.st.javaslicer.variables.Variable;

/**
 * Visitor for a Javaslicer dynamic slice creating a {@link Graph} for the slice.
 * 
 * @author Chenguang Zhu
 *
 */
public class GenerateGraphVisitor implements SliceVisitor {
	private Graph<InstructionSourceCodeEntity> graph = new Graph<InstructionSourceCodeEntity>();

	/**
	 * Create a new instruction vertex.
	 * 
	 * @param node
	 * @return a new created Vertex
	 */
	private Vertex<InstructionSourceCodeEntity> createVertex(
			InstructionSourceCodeEntity node) {
		return new Vertex<InstructionSourceCodeEntity>(node.getUniqueName(),
				node);
	}

	/**
	 * Add the root node into a graph.
	 * 
	 * @param instance
	 *            {@link InstructionInstance} in the dynamic slice
	 */
	@Override
	public void visitMatchedInstance(InstructionInstance instance) {
		SourceRange range = new SourceRange();
		range.setStart(instance.getInstruction().getLineNumber());
		range.setEnd(instance.getInstruction().getLineNumber());

		String qualifiedName = getFullName(instance);
		String className = instance.getInstruction().getMethod().getReadClass()
				.getName();

		HashSet<Integer> lineNumberSet = new HashSet<Integer>();

		Vertex<InstructionSourceCodeEntity> v = createVertex(
				new InstructionSourceCodeEntity(qualifiedName,
						JavaEntityType.METHOD, 0, range, lineNumberSet));

		v.getData().setClassName(className);

		this.graph.addVertex(v);
	}

	/**
	 * Add new nodes and edges into the graph when we find a dependence
	 * 
	 * @param from,
	 *            to, variable, distance
	 */
	@Override
	public void visitSliceDependence(InstructionInstance from,
			InstructionInstance to, Variable variable, int distance) {
		Vertex<InstructionSourceCodeEntity> fromVertex = graph
				.findVertexByName(getFullName(from));
		Vertex<InstructionSourceCodeEntity> toVertex = graph
				.findVertexByName(getFullName(to));
		if (fromVertex == null) {
			SourceRange range = new SourceRange();
			range.setStart(from.getInstruction().getLineNumber());
			range.setEnd(from.getInstruction().getLineNumber());

			String qualifiedName = getFullName(from);
			String className = from.getInstruction().getMethod().getReadClass()
					.getName();

			HashSet<Integer> lineNumberSet = new HashSet<Integer>();
			fromVertex = createVertex(
					new InstructionSourceCodeEntity(qualifiedName,
							JavaEntityType.METHOD, 0, range, lineNumberSet));
			fromVertex.getData().setClassName(className);
			this.graph.addVertex(fromVertex);
		}
		if (toVertex == null) {
			SourceRange range = new SourceRange();
			range.setStart(to.getInstruction().getLineNumber());
			range.setEnd(to.getInstruction().getLineNumber());

			String qualifiedName = getFullName(to);
			String className = to.getInstruction().getMethod().getReadClass()
					.getName();

			HashSet<Integer> lineNumberSet = new HashSet<Integer>();
			toVertex = createVertex(
					new InstructionSourceCodeEntity(qualifiedName,
							JavaEntityType.METHOD, 0, range, lineNumberSet));
			toVertex.getData().setClassName(className);
			this.graph.addVertex(toVertex);
		}

		fromVertex.getData().getLineNumberSet()
				.add(from.getInstruction().getLineNumber());
		toVertex.getData().getLineNumberSet()
				.add(to.getInstruction().getLineNumber());

		this.graph.addEdge(fromVertex, toVertex, null);
	}

	/**
	 * Return the instruction node graph
	 * 
	 * @return graph
	 */
	public Graph<InstructionSourceCodeEntity> getGraph() {
		return this.graph;
	}

	/**
	 * Return full name of an instruction node (class,method,signature)
	 * 
	 * @param instance
	 *            {@link InstructionInstance} in the dynamic slice
	 * @return full name (class, method, signature) of an instruction node
	 */
	public String getFullName(InstructionInstance instance) {
		String qualifiedName = BytecodeUtils.getQualifiedMethodName(
				instance.getInstruction().getMethod().getReadClass().getName(),
				instance.getInstruction().getMethod().getName(),
				instance.getInstruction().getMethod().getDesc());
		return qualifiedName;
	}

}
