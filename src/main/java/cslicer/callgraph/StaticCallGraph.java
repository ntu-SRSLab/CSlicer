package cslicer.callgraph;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cslicer.utils.BytecodeUtils;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;
import cslicer.utils.graph.Edge;
import cslicer.utils.graph.Graph;
import cslicer.utils.graph.SpanningTreeVisitor;
import cslicer.utils.graph.Vertex;

public class StaticCallGraph implements ICallGraph, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Graph<CGNode> fGraph;
	private Set<String> fScope;
	private Comparator<String> fNodeComparator = new Comparator<String>() {

		@Override
		public int compare(String arg0, String arg1) {
			if (BytecodeUtils.matchWithGenericType(arg0, arg1))
				return 0;
			return -1;
		}

	};

	// Loose matching of method/field names
	private Comparator<String> fContainComparator = new Comparator<String>() {
		@Override
		public int compare(String arg0, String arg1) {
			if (arg0.contains(arg1) || arg1.contains(arg0))
				return 0;
			return -1;
		}
	};

	/**
	 * Implementing a static call graph.
	 */
	public StaticCallGraph() {
		fGraph = new Graph<CGNode>();
		fScope = null;
	}

	public StaticCallGraph(Set<String> scope) {
		this();
		fScope = scope;
	}

	public StaticCallGraph(String filePath) {
		fGraph = loadFromFile(filePath);
	}

	/**
	 * All edges of this graph.
	 * 
	 * @return a list of {@link Edge}
	 */
	public List<Edge<CGNode>> getEdges() {
		return fGraph.getEdges();
	}

	@Override
	public void insertEdge(CGNode from, CGNode to, CGEdgeType type) {
		StatsUtils.resume("insert.edge");
		// ignore excluded nodes
		if (BytecodeUtils.matchExclude(from.getName())
				|| BytecodeUtils.matchExclude(to.getName()))
			return;

		// nodes not in scope
		if (fScope != null && (!fScope.contains(from.getName())
				|| !fScope.contains(to.getName())))
			return;

		Vertex<CGNode> fromV = fGraph.findVertexByData(from);
		if (fromV == null) {
			fromV = createVertex(from);
			fGraph.addVertex(fromV);
		}

		Vertex<CGNode> toV = fGraph.findVertexByData(to);
		if (toV == null) {
			toV = createVertex(to);
			fGraph.addVertex(toV);
		}

		fGraph.addEdge(fromV, toV, type);
		StatsUtils.stop("insert.edge");
	}

	/**
	 * Find all fields/classes/methods referenced by a node.
	 * 
	 * @param node
	 *            a {@link CGNode} in the call graph
	 * @return a set of {@link CGNode} referenced by {@code node}
	 */
	public Set<CGNode> getOutgoingNodes(CGNode node) {
		Set<CGNode> res = new HashSet<>();
		Vertex<CGNode> vertex = fGraph.findVertexByData(node);
		if (vertex != null) {
			for (Edge<CGNode> edge : vertex.getOutgoingEdges()) {
				res.add(edge.getTo().getData());
			}
		}
		return res;
	}

	/**
	 * Find all fields/classes/methods referenced by a node.
	 * 
	 * @param nodeName
	 *            the name of a call graph node
	 * @return a set of {@link CGNode} referenced
	 */
	public Set<CGNode> getOutgoingNodes(String nodeName) {
		Set<CGNode> res = new HashSet<>();
		Vertex<CGNode> vertex = fGraph.findVertexByName(nodeName);
		if (vertex != null) {
			for (Edge<CGNode> edge : vertex.getOutgoingEdges()) {
				res.add(edge.getTo().getData());
			}
		}
		return res;
	}

	/**
	 * Find all fields/classes/methods a node references.
	 * 
	 * @param node
	 *            a {@link CGNode} in the call graph
	 * @return a set of {@link CGNode} which reference {@code node}
	 */
	public Set<CGNode> getIncomingNodes(CGNode node) {
		Set<CGNode> res = new HashSet<>();
		Vertex<CGNode> vertex = fGraph.findVertexByData(node);
		if (vertex != null) {
			for (Edge<CGNode> edge : vertex.getIncomingEdges()) {
				res.add(edge.getFrom().getData());
			}
		}
		return res;
	}

	/**
	 * Find all fields/classes/methods a node references.
	 * 
	 * @param nodeName
	 *            the name of a call graph node
	 * @return a set of {@link CGNode} which reference the given node
	 */
	public Set<CGNode> getIncomingNodes(String nodeName) {
		Set<CGNode> res = new HashSet<>();
		Vertex<CGNode> vertex = fGraph.findVertexByName(nodeName);
		if (vertex != null) {
			for (Edge<CGNode> edge : vertex.getIncomingEdges()) {
				res.add(edge.getFrom().getData());
			}
		}
		return res;
	}

	public Set<String> getHardDependers(String nodeName) {
		Set<String> res = new HashSet<>();
		Vertex<CGNode> vertex = fGraph.findVertexByName(nodeName,
				fNodeComparator);
		if (vertex != null) {
			for (Edge<CGNode> edge : vertex.getIncomingEdges()) {
				if (!edge.getLabel().equals(CGEdgeType.CLASS_FIELD)
						&& !edge.getLabel().equals(CGEdgeType.CLASS_METHOD))
					res.add(edge.getFrom().getName());
			}
		}
		return res;
	}

	public Set<String> getDependees(String nodeName) {
		Set<String> res = new HashSet<>();
		Vertex<CGNode> vertex = fGraph.findVertexByName(nodeName,
				fContainComparator);
		if (vertex != null) {
			for (Edge<CGNode> edge : vertex.getOutgoingEdges()) {
				res.add(edge.getTo().getName());
			}
		}
		return res;
	}

	public boolean hasNode(String nodeName) {
		return fGraph.findVertexByName(nodeName) != null;
	}

	/**
	 * Compute the transitive closure of all successors of a node.
	 * 
	 * @param nodeName
	 *            starting node name
	 * @return a set of {@link CGNode}
	 */
	public Set<CGNode> getTransitiveSuccessors(String nodeName) {
		Set<CGNode> res = new HashSet<CGNode>();
		Vertex<CGNode> vertex = fGraph.findVertexByName(nodeName,
				fNodeComparator);
		if (vertex != null) {
			SpanningTreeVisitor<CGNode> visitor = new SpanningTreeVisitor<CGNode>(
					res);
			fGraph.dfsSpanningTree(vertex, visitor);
		}
		fGraph.clearEdges();
		fGraph.clearMark();

		return res;
	}

	public StaticCallGraph getSubGraph(Set<String> scope) {
		StaticCallGraph res = new StaticCallGraph(scope);
		res.fGraph = this.fGraph.subGraphByName(scope);
		return res;
	}

	/**
	 * Return the names of all recursive successors of a node.
	 * 
	 * @param nodeName
	 *            starting node name
	 * @return a set of recursive successor names
	 */
	public Set<String> getTransitiveSuccessorNames(String nodeName) {
		Set<String> names = new HashSet<String>();
		Set<CGNode> res = getTransitiveSuccessors(nodeName);
		for (CGNode r : res)
			names.add(r.getName());
		return names;
	}

	@Override
	public Set<MethodNode> getCaller(MethodNode method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<MethodNode> getCallee(MethodNode method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<MethodNode> getAccessor(FieldNode field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<FieldNode> getAccessee(MethodNode method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ClassNode> getReferencer(ClassNode clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ClassNode> getReferencee(ClassNode clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Print call graph to standard output.
	 */
	public void printCallGraph() {
		PrintUtils.print(fGraph.toString());
	}

	/**
	 * Print call graph as DOT file.
	 * 
	 * @param path
	 *            path for output DOT file
	 */
	public void outputDOTFile(String path) {
		fGraph.toDOT(path);
	}

	private Vertex<CGNode> createVertex(CGNode node) {
		return new Vertex<CGNode>(node.getName(), node);
	}

	/**
	 * Load plain call graph (entity identifiers only) from DOT file. Written by
	 * Chenguang Zhu.
	 * 
	 * @param FilePath
	 *            path to DOT file
	 */
	private Graph<CGNode> loadFromFile(String FilePath) {
		Graph<CGNode> graph = new Graph<CGNode>();

		File f = new File(FilePath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));

			String tmp = "";
			while ((tmp = reader.readLine()) != null) {
				if (tmp.startsWith("digraph G") || tmp.startsWith("}")) {
					continue;
				}

				if (tmp.contains("->")) {
					String[] splitArray = tmp
							.substring(3, tmp.indexOf("[label=\"") - 1).trim()
							.split("\"->\"");
					String fromVertexName = splitArray[0];
					String toVertexName = splitArray[1];
					Vertex<CGNode> fromVertex = graph
							.findVertexByName(fromVertexName);
					Vertex<CGNode> toVertex = graph
							.findVertexByName(toVertexName);

					if (fromVertex == null) {
						fromVertex = createVertex(
								new GenericNode(fromVertexName));
						graph.addVertex(fromVertex);
					}
					if (toVertex == null) {
						toVertex = createVertex(new GenericNode(toVertexName));
						graph.addVertex(toVertex);
					}
					graph.addEdge(fromVertex, toVertex, CGEdgeType.DEFAULT);
				} else {
					String isolatedVertexName = tmp
							.substring(3, tmp.indexOf(";") - 1).trim();
					Vertex<CGNode> isolatedVertex = new Vertex<CGNode>(
							isolatedVertexName,
							new GenericNode(isolatedVertexName));
					graph.addVertex(isolatedVertex);
				}
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return graph;
	}

	public String toString() {
		return (fScope == null ? "" : fScope.toString() + "\n")
				+ fGraph.toString();
	}

	public void insertEdge(Edge<CGNode> e) {
		Vertex<CGNode> from = e.getFrom();
		Vertex<CGNode> to = e.getTo();

		// ignore excluded nodes
		if (BytecodeUtils.matchExclude(from.getName())
				|| BytecodeUtils.matchExclude(to.getName()))
			return;

		// nodes not in scope
		if (fScope != null && (!fScope.contains(from.getName())
				|| !fScope.contains(to.getName())))
			return;

		Vertex<CGNode> fromV = fGraph.findVertexByName(from.getName());
		if (fromV == null) {
			fromV = createVertex(from.getData());
			fGraph.addVertex(fromV);
		}

		Vertex<CGNode> toV = fGraph.findVertexByName(to.getName());
		if (toV == null) {
			toV = createVertex(to.getData());
			fGraph.addVertex(toV);
		}

		fGraph.addEdge(fromV, toV, e.getLabel());
	}

	public void insertEdges(Collection<Edge<CGNode>> edges) {
		for (Edge<CGNode> e : edges)
			insertEdge(e);
	}

	public void removeEdge(Edge<CGNode> e) {
		fGraph.removeEdge(e.getFrom(), e.getTo());
	}

	public void removeEdges(Collection<Edge<CGNode>> edges) {
		for (Edge<CGNode> e : edges)
			removeEdge(e);
	}

	public void setScope(Set<String> scope) {
		fScope = scope;
		List<Edge<CGNode>> toRemove = new LinkedList<>();
		for (Edge<CGNode> e : fGraph.getEdges()) {
			if (!scope.contains(e.getFrom().getName())
					|| !scope.contains(e.getTo().getName()))
				toRemove.add(e);
		}
		removeEdges(toRemove);
		List<Vertex<CGNode>> toRemoveV = new LinkedList<>();
		for (Vertex<CGNode> v : fGraph.getVerticies()) {
			if (v.getIncomingEdgeCount() == 0 && v.getOutgoingEdgeCount() == 0)
				toRemoveV.add(v);
		}
		for (Vertex<CGNode> v : toRemoveV)
			fGraph.removeVertex(v);
	}
}
