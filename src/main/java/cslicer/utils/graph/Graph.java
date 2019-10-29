package cslicer.utils.graph;

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A directed graph data structure.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 * @param <T>
 *            graph node type
 */
@SuppressWarnings("unchecked")
public class Graph<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Color used to mark unvisited nodes */
	public static final int VISIT_COLOR_WHITE = 1;

	/** Color used to mark nodes as they are first visited in DFS order */
	public static final int VISIT_COLOR_GREY = 2;

	/** Color used to mark nodes after descendants are completely visited */
	public static final int VISIT_COLOR_BLACK = 3;

	/** Vector of graph vertices */
	private Set<Vertex<T>> vertices;

	/** Vector of edges in the graph */
	private List<Edge<T>> edges;

	/** The vertex identified as the root of the graph */
	private Vertex<T> rootVertex;

	/** Vertex name/data map for efficiency **/
	private Map<String, Vertex<T>> vertexNameMap;
	private Map<T, Vertex<T>> vertexDataMap;

	/**
	 * Construct a new graph without any vertices or edges
	 */
	public Graph() {
		vertices = new HashSet<Vertex<T>>();
		edges = new ArrayList<Edge<T>>();
		vertexNameMap = new HashMap<String, Vertex<T>>();
		vertexDataMap = new HashMap<T, Vertex<T>>();
	}

	/**
	 * Deep copy of a graph.
	 * 
	 * @param copy
	 *            other graph to copy
	 */
	public Graph(final Graph<T> copy) {
		this();
		for (Vertex<T> v : copy.vertices)
			this.addVertex(new Vertex<T>(v.getName(), v.getData()));
		for (Edge<T> e : copy.edges)
			this.addEdge(this.findVertexByName(e.getFrom().getName()),
					this.findVertexByName(e.getTo().getName()), e.getLabel());
	}

	/**
	 * Generate a sub-graph given vertex data.
	 * 
	 * @param data
	 *            vertex data to include
	 * @return a deep copy of sub graph
	 */
	@Deprecated
	public Graph<T> subGraphByData(Collection<T> data) {
		Set<String> nodes = new HashSet<>();
		for (T d : data) {
			Vertex<T> node = findVertexByData(d);
			if (node != null)
				nodes.add(node.getName());
		}

		return subGraphByName(nodes);
	}

	/**
	 * Generate a sub-graph given vertex names.
	 * 
	 * @param nodes
	 *            vertex names to include
	 * @return a deep copy of sub graph
	 */
	@Deprecated
	public Graph<T> subGraphByName(Collection<String> nodes) {
		Graph<T> sub = new Graph<>(this);

		Set<Vertex<T>> toRemove = new HashSet<>();
		for (Vertex<T> v : sub.vertices) {
			if (!nodes.contains(v.getName()))
				toRemove.add(v);
		}
		for (Vertex<T> v : toRemove)
			sub.removeVertex(v);

		return sub;
	}

	/**
	 * Are there any vertices in the graph
	 * 
	 * @return {@code true} if there are no vertices in the graph
	 */
	public boolean isEmpty() {
		return vertices.size() == 0;
	}

	/**
	 * Add a vertex to the graph.
	 * 
	 * @param v
	 *            the Vertex to add
	 * @return {@code true} if the vertex was added, {@code false} if it was
	 *         already in the graph.
	 */
	public boolean addVertex(Vertex<T> v) {
		boolean added = false;
		if (!vertices.contains(v)) {
			added = vertices.add(v);
			vertexNameMap.put(v.getName(), v);
			vertexDataMap.put(v.getData(), v);
		}
		return added;
	}

	/**
	 * Get the vertex count.
	 * 
	 * @return the number of vertices in the graph.
	 */
	public int size() {
		return vertices.size();
	}

	/**
	 * Get the root vertex
	 * 
	 * @return the root vertex if one is set, null if no vertex has been set as
	 *         the root.
	 */
	public Vertex<T> getRootVertex() {
		return rootVertex;
	}

	/**
	 * Set a root vertex. If root does no exist in the graph it is added.
	 * 
	 * @param root
	 *            the vertex to set as the root and optionally add if it does
	 *            not exist in the graph.
	 */
	public void setRootVertex(Vertex<T> root) {
		this.rootVertex = root;
		if (vertices.contains(root) == false)
			this.addVertex(root);
	}

	/**
	 * Get the given Vertex.
	 * 
	 * @param n
	 *            the index [0, size()-1] of the Vertex to access
	 * @return the n-th Vertex
	 */
	// public Vertex<T> getVertex(int n) {
	// return verticies.get(n);
	// }

	/**
	 * Get the graph vertices
	 * 
	 * @return the graph vertices
	 */
	public Set<Vertex<T>> getVerticies() {
		return this.vertices;
	}

	/**
	 * Insert a directed, weighted Edge into the graph.
	 * 
	 * @param from
	 *            the Edge starting vertex
	 * @param to
	 *            the Edge ending vertex
	 * @param label
	 *            the Edge label
	 * @return {@code true} if the Edge was added, {@code false} if from already
	 *         has this Edge
	 * @throws IllegalArgumentException
	 *             if from/to are not vertices in the graph
	 */
	public boolean addEdge(Vertex<T> from, Vertex<T> to, EdgeLabel label)
			throws IllegalArgumentException {
		if (!vertices.contains(from))
			throw new IllegalArgumentException("from is not in graph");
		if (!vertices.contains(to))
			throw new IllegalArgumentException("to is not in graph");

		Edge<T> e = new Edge<T>(from, to, label);
		if (from.findEdge(to) != null)
			return false;
		else {
			from.addEdge(e);
			to.addEdge(e);
			edges.add(e);
			return true;
		}
	}

	/**
	 * Insert a bidirectional Edge in the graph
	 * 
	 * @param from
	 *            the Edge starting vertex
	 * @param to
	 *            the Edge ending vertex
	 * @param cost
	 *            the Edge weight/cost
	 * @return {@code true} if edges between both nodes were added,
	 *         {@code false} otherwise
	 * @throws IllegalArgumentException
	 *             if from/to are not vertices in the graph
	 */
	public boolean insertBiEdge(Vertex<T> from, Vertex<T> to, EdgeLabel cost)
			throws IllegalArgumentException {
		return addEdge(from, to, cost) && addEdge(to, from, cost);
	}

	/**
	 * Get the graph edges.
	 * 
	 * @return the graph edges
	 */
	public List<Edge<T>> getEdges() {
		return this.edges;
	}

	/**
	 * Remove a vertex from the graph.
	 * 
	 * @param v
	 *            the Vertex to remove
	 * @return {@code true} if the Vertex was removed
	 */
	public boolean removeVertex(Vertex<T> v) {
		if (!vertices.contains(v))
			return false;

		if (vertexNameMap.get(v.getName()) == v)
			vertexNameMap.remove(v.getName());
		if (vertexDataMap.get(v.getData()) == v)
			vertexDataMap.remove(v.getData());

		if (v == rootVertex)
			rootVertex = null;

		// Remove the edges associated with v
		Set<Edge<T>> toRemove = new HashSet<>();
		toRemove.addAll(v.getOutgoingEdges());
		toRemove.addAll(v.getIncomingEdges());

		for (Edge<T> e : toRemove) {
			removeEdge(e.getFrom(), e.getTo());
		}

		vertices.remove(v);

		return true;
	}

	/**
	 * Remove an Edge from the graph
	 * 
	 * @param from
	 *            the Edge starting vertex
	 * @param to
	 *            the Edge ending vertex
	 * @return {@code true} if the Edge exists, {@code false} otherwise
	 */
	public boolean removeEdge(final Vertex<T> from, final Vertex<T> to) {
		Edge<T> e = from.findEdge(to);
		if (e == null)
			return false;
		else {
			Vertex<T> f = findVertexByName(from.getName());
			if (f != null)
				f.remove(e);
			Vertex<T> t = findVertexByName(to.getName());
			if (t != null)
				t.remove(e);
			edges.remove(e);
			return true;
		}
	}

	/**
	 * Clear the mark state of all vertices in the graph by calling clearMark()
	 * on all vertices.
	 * 
	 * @see Vertex#clearMark()
	 */
	public void clearMark() {
		for (Vertex<T> w : vertices) {
			w.clearMark();
			w.setMarkState(VISIT_COLOR_WHITE);
		}
	}

	/**
	 * Clear the mark state of all edges in the graph by calling clearMark() on
	 * all edges.
	 */
	public void clearEdges() {
		for (Edge<T> e : edges)
			e.clearMark();
	}

	/**
	 * Perform a depth first search using recursion.
	 * 
	 * @param v
	 *            the Vertex to start the search from
	 * @param visitor
	 *            the visitor to inform prior to
	 * @see Visitor#visit(Graph, Vertex)
	 */
	public void depthFirstSearch(Vertex<T> v, final Visitor<T> visitor) {
		VisitorEX<T, RuntimeException> wrapper = new VisitorEX<T, RuntimeException>() {
			public void visit(Graph<T> g, Vertex<T> v) throws RuntimeException {
				if (visitor != null)
					visitor.visit(g, v);
			}
		};
		this.depthFirstSearch(v, wrapper);
	}

	/**
	 * Perform a depth first search using recursion. The search may be cut short
	 * if the visitor throws an exception.
	 * 
	 * @param <E>
	 *            visit exception
	 * 
	 * @param v
	 *            the Vertex to start the search from
	 * @param visitor
	 *            the visitor to inform prior to
	 * @see Visitor#visit(Graph, Vertex)
	 * @throws E
	 *             if visitor.visit throws an exception
	 */
	public <E extends Exception> void depthFirstSearch(Vertex<T> v,
			VisitorEX<T, E> visitor) throws E {
		if (visitor != null)
			visitor.visit(this, v);
		v.visit();
		for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
			Edge<T> e = v.getOutgoingEdge(i);
			if (!e.getTo().visited()) {
				depthFirstSearch(e.getTo(), visitor);
			}
		}
	}

	/**
	 * Perform a breadth first search of this graph, starting at v.
	 * 
	 * @param v
	 *            the search starting point
	 * @param visitor
	 *            the visitor whose visit method is called prior to visiting a
	 *            vertex.
	 */
	public void breadthFirstSearch(Vertex<T> v, final Visitor<T> visitor) {
		VisitorEX<T, RuntimeException> wrapper = new VisitorEX<T, RuntimeException>() {
			public void visit(Graph<T> g, Vertex<T> v) throws RuntimeException {
				if (visitor != null)
					visitor.visit(g, v);
			}
		};
		this.breadthFirstSearch(v, wrapper);
	}

	/**
	 * Perform a breadth first search of this graph, starting at v. The visit
	 * may be cut short if visitor throws an exception during a visit callback.
	 * 
	 * @param <E>
	 *            visit exception
	 * @param v
	 *            the search starting point
	 * @param visitor
	 *            the visitor whose visit method is called prior to visiting a
	 *            vertex.
	 * @throws E
	 *             if vistor.visit throws an exception
	 */
	public <E extends Exception> void breadthFirstSearch(Vertex<T> v,
			VisitorEX<T, E> visitor) throws E {
		LinkedList<Vertex<T>> q = new LinkedList<Vertex<T>>();

		q.add(v);
		if (visitor != null)
			visitor.visit(this, v);
		v.visit();
		while (q.isEmpty() == false) {
			v = q.removeFirst();
			for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
				Edge<T> e = v.getOutgoingEdge(i);
				Vertex<T> to = e.getTo();
				if (!to.visited()) {
					q.add(to);
					if (visitor != null)
						visitor.visit(this, to);
					to.visit();
				}
			}
		}
	}

	/**
	 * Find the spanning tree using a DFS starting from v.
	 * 
	 * @param v
	 *            the vertex to start the search from
	 * @param visitor
	 *            visitor invoked after each vertex is visited and an edge is
	 *            added to the tree.
	 */
	public void dfsSpanningTree(Vertex<T> v, DFSVisitor<T> visitor) {
		v.visit();
		if (visitor != null)
			visitor.visit(this, v);

		for (int i = 0; i < v.getOutgoingEdgeCount(); i++) {
			Edge<T> e = v.getOutgoingEdge(i);
			if (!e.getTo().visited()) {
				if (visitor != null)
					visitor.visit(this, v, e);
				e.mark();
				dfsSpanningTree(e.getTo(), visitor);
			}
		}
	}

	public List<T> topologicalSort(boolean reverse) {
		List<T> result = new ArrayList<T>();

		/* Fire off a DFS from each node in the graph. */
		for (Vertex<T> node : vertices)
			explore(node, result, reverse);

		clearMark();
		/* Hand back the resulting ordering. */
		return result;
	}

	@Deprecated
	public List<Set<T>> layeredSort(boolean reverse) {
		List<Vertex<T>> sorted = new ArrayList<Vertex<T>>();
		for (Vertex<T> node : vertices)
			exploreVertex(node, sorted, reverse);
		clearMark();

		List<Set<T>> result = new ArrayList<Set<T>>();
		Set<Vertex<T>> working = new HashSet<Vertex<T>>();

		for (Vertex<T> v : sorted) {
			// grow working set
			Set<Vertex<T>> grow = reverse ? getPredecessors(working)
					: getSuccessors(working);

			if (grow.contains(v)) {
				// v is the successor of some node in working
				working.clear();
				Set<T> layer = new HashSet<T>();
				layer.add(v.getData());
				result.add(layer);
			} else {
				working.add(v);
				if (!result.isEmpty())
					result.get(result.size() - 1).add(v.getData());
				else {
					Set<T> layer = new HashSet<T>();
					layer.add(v.getData());
					result.add(layer);
				}
			}
		}

		return result;
	}

	private Set<Vertex<T>> getSuccessors(Vertex<T> vertex) {
		Set<Vertex<T>> res = new HashSet<Vertex<T>>();
		for (Edge<T> successor : vertex.getOutgoingEdges())
			res.add(successor.getTo());

		return res;
	}

	private Set<Vertex<T>> getSuccessors(Collection<Vertex<T>> vertices) {
		Set<Vertex<T>> res = new HashSet<Vertex<T>>();
		for (Vertex<T> v : vertices)
			res.addAll(getSuccessors(v));

		return res;
	}

	private Set<Vertex<T>> getPredecessors(Vertex<T> vertex) {
		Set<Vertex<T>> res = new HashSet<Vertex<T>>();
		for (Edge<T> successor : vertex.getIncomingEdges())
			res.add(successor.getFrom());

		return res;
	}

	private Set<Vertex<T>> getPredecessors(Collection<Vertex<T>> vertices) {
		Set<Vertex<T>> res = new HashSet<Vertex<T>>();
		for (Vertex<T> v : vertices)
			res.addAll(getPredecessors(v));

		return res;
	}

	private void exploreVertex(Vertex<T> node, List<Vertex<T>> result,
			boolean reverse) {
		if (node.getMarkState() == VISIT_COLOR_BLACK)
			return;
		if (node.getMarkState() == VISIT_COLOR_GREY)
			throw new IllegalArgumentException("graph contains a cycle");

		node.setMarkState(VISIT_COLOR_GREY);

		if (reverse) {
			for (Edge<T> successor : node.getOutgoingEdges())
				exploreVertex(successor.getTo(), result, reverse);
		} else {
			for (Edge<T> predecessor : node.getIncomingEdges())
				exploreVertex(predecessor.getFrom(), result, reverse);
		}

		result.add(node);
		node.setMarkState(VISIT_COLOR_BLACK);
	}

	private void explore(Vertex<T> node, List<T> result, boolean reverse) {
		if (node.getMarkState() == VISIT_COLOR_BLACK)
			return;
		if (node.getMarkState() == VISIT_COLOR_GREY)
			throw new IllegalArgumentException("graph contains a cycle");

		node.setMarkState(VISIT_COLOR_GREY);

		if (reverse) {
			for (Edge<T> successor : node.getOutgoingEdges())
				explore(successor.getTo(), result, reverse);
		} else {
			for (Edge<T> predecessor : node.getIncomingEdges())
				explore(predecessor.getFrom(), result, reverse);
		}

		result.add(node.getData());
		node.setMarkState(VISIT_COLOR_BLACK);
	}

	/**
	 * Search the vertices for one with name.
	 * 
	 * @param name
	 *            the vertex name
	 * @return the first vertex with a matching name, null if no matches are
	 *         found
	 */
	public Vertex<T> findVertexByName(String name) {
		return vertexNameMap.get(name);
	}

	public Vertex<T> findVertexByName(String name, Comparator<String> cmp) {
		Vertex<T> match = null;
		for (Vertex<T> v : vertices) {
			if (cmp.compare(name, v.getName()) == 0) {
				match = v;
				break;
			}
		}
		return match;
	}

	public Vertex<T> findVertexByData(T data) {
		return vertexDataMap.get(data);
	}

	/**
	 * Search the vertices for one with data.
	 * 
	 * @param data
	 *            the vertex data to match
	 * @param compare
	 *            the comparator to perform the match
	 * @return the first vertex with a matching data, null if no matches are
	 *         found
	 */
	public Vertex<T> findFirstVertexByData(T data, Comparator<T> compare) {
		Vertex<T> match = null;
		for (Vertex<T> v : vertices) {
			if (compare.compare(data, v.getData()) == 0) {
				match = v;
				break;
			}
		}
		return match;
	}

	public Vertex<T> findFirstVertexByData(T data) {
		Vertex<T> match = null;
		for (Vertex<T> v : vertices) {
			if (data.equals(v.getData())) {
				match = v;
				break;
			}
		}
		return match;
	}

	/**
	 * Search the graph for cycles. In order to detect cycles, we use a modified
	 * depth first search called a colored DFS. All nodes are initially marked
	 * white. When a node is encountered, it is marked grey, and when its
	 * descendants are completely visited, it is marked black. If a grey node is
	 * ever encountered, then there is a cycle.
	 * 
	 * @return the edges that form cycles in the graph. The array will be empty
	 *         if there are no cycles.
	 */
	public Edge<T>[] findCycles() {
		ArrayList<Edge<T>> cycleEdges = new ArrayList<Edge<T>>();
		// Mark all vertices as white
		for (Vertex<T> v : vertices) {
			v.setMarkState(VISIT_COLOR_WHITE);
		}
		for (Vertex<T> v : vertices) {
			visit(v, cycleEdges);
		}

		Edge<T>[] cycles = new Edge[cycleEdges.size()];
		cycleEdges.toArray(cycles);
		return cycles;
	}

	private void visit(Vertex<T> v, ArrayList<Edge<T>> cycleEdges) {
		v.setMarkState(VISIT_COLOR_GREY);
		int count = v.getOutgoingEdgeCount();
		for (int n = 0; n < count; n++) {
			Edge<T> e = v.getOutgoingEdge(n);
			Vertex<T> u = e.getTo();
			if (u.getMarkState() == VISIT_COLOR_GREY) {
				// A cycle Edge<T>
				cycleEdges.add(e);
			} else if (u.getMarkState() == VISIT_COLOR_WHITE) {
				visit(u, cycleEdges);
			}
		}
		v.setMarkState(VISIT_COLOR_BLACK);
	}

	public String toString() {
		StringBuffer tmp = new StringBuffer("Graph[");
		for (Vertex<T> v : vertices) {
			tmp.append(v);
			tmp.append("\n");
		}
		tmp.append(']');
		return tmp.toString();
	}

	/**
	 * Output graph to a file in TA format, for saving hunk deps only now.
	 *
	 * @param taPath path of output TA file
	 * @param op operator of ta tuples
	 * @param printLabel print label in attributes or not (not implemented yet)
	 */
	public void toTA(String taPath, String op, boolean printLabel) {
		StringBuilder taContents = new StringBuilder() ;
		String header = "FACT TUPLE :\n";
		taContents.append(header);
		String taTuple = "";
		for (Edge<T> e : edges) {
			Vertex<T> fromVertex = e.getFrom();
			Vertex<T> toVertex = e.getTo();
			taTuple = String.format("%s %s %s\n", op, fromVertex.getName(), toVertex.getName());
			taContents.append(taTuple);
		}
		try {
			File taFile = new File(taPath);
			FileWriter fWriter = new FileWriter(taFile, false);
			fWriter.write(taContents.toString());
			fWriter.flush();
			fWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void toDOT(String dotPath) {
		toDOT(dotPath, false);
	}

	/**
	 * Output the graph in DOT format. Written by: Chenguang Zhu
	 * 
	 * @param dotPath
	 *            output file path
	 * @param pretty
	 *            pretty printing node name
	 */
	public void toDOT(String dotPath, boolean pretty) {
		File dotFile = new File(dotPath);

		try {
			if (!dotFile.exists()) {
				dotFile.createNewFile();
			}

			FileWriter fwriter = new FileWriter(dotFile, false);
			fwriter.write("digraph G{\n");

			for (Edge<T> e : edges) {
				Vertex<T> fromVertex = e.getFrom();
				Vertex<T> toVertex = e.getTo();

				String dotEdgeString = "";

				if (e.getLabel() != null && e.getLabel().toString() != "") {
					dotEdgeString = String.format(
							pretty ? "  \"%.8s\"->\"%.8s\"[label=\"%.8s\", fontcolor=darkgreen];\n"
									: "  \"%s\"->\"%s\"[label=\"%s\", fontcolor=darkgreen];\n",
							fromVertex.getName(), toVertex.getName(),
							e.getLabel().toString());
				} else {
					dotEdgeString = String.format(
							pretty ? "  \"%.8s\"->\"%.8s\";\n"
									: "  \"%s\"->\"%s\";\n",
							fromVertex.getName(), toVertex.getName());
				}

				fwriter.write(dotEdgeString);
			}

			String isolatedVertex = "";
			for (Vertex<T> v : vertices) {
				if (v.getOutgoingEdgeCount() == 0
						&& v.getIncomingEdgeCount() == 0) {
					isolatedVertex = String.format(
							pretty ? "  \"%.8s\";\n" : "  \"%s\";\n",
							v.getName());
					fwriter.write(isolatedVertex);
				}
			}

			fwriter.write("}\n");
			fwriter.flush();
			fwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	/**
	 * Root nodes with no incoming edge.
	 * 
	 * @return a set of root nodes
	 */
	public Set<Vertex<T>> getRootNodes() {
		Set<Vertex<T>> res = new HashSet<>();

		for (Vertex<T> v : vertices) {
			if (v.getIncomingEdgeCount() == 0)
				res.add(v);
		}

		return res;
	}

}
