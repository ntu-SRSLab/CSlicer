package cslicer.utils.graph;

/*
 * JBoss, Home of Professional Open Source Copyright 2006, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/**
 * A spanning tree visitor callback interface.
 * 
 * @see Graph#dfsSpanningTree(Vertex, DFSVisitor)
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 * @param <T>
 *            graph node type
 */
public interface DFSVisitor<T> {
	/**
	 * Called by the graph traversal methods when a vertex is first visited.
	 * 
	 * @param g
	 *            the graph
	 * @param v
	 *            the vertex being visited.
	 */
	public void visit(Graph<T> g, Vertex<T> v);

	/**
	 * Used dfsSpanningTree to notify the visitor of each outgoing edge to an
	 * unvisited vertex.
	 * 
	 * @param g
	 *            the graph
	 * @param v
	 *            the vertex being visited
	 * @param e
	 *            the outgoing edge from v
	 */
	public void visit(Graph<T> g, Vertex<T> v, Edge<T> e);
}
