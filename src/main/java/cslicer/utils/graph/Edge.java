package cslicer.utils.graph;

import java.io.Serializable;

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
 * A directed, weighted edge in a graph
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 * @param <T>
 *            graph node type
 */
public class Edge<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Vertex<T> from;

	private Vertex<T> to;

	private EdgeLabel label;

	private boolean mark;

	/**
	 * Create a zero cost edge between from and to
	 * 
	 * @param from
	 *            the starting vertex
	 * @param to
	 *            the ending vertex
	 */
	public Edge(Vertex<T> from, Vertex<T> to) {
		this(from, to, null);
	}

	/**
	 * Create an edge between from and to with the given cost.
	 * 
	 * @param from
	 *            the starting vertex
	 * @param to
	 *            the ending vertex
	 * @param label
	 *            the edge label
	 */
	public Edge(Vertex<T> from, Vertex<T> to, EdgeLabel label) {
		this.from = from;
		this.to = to;
		this.label = label;
		mark = false;
	}

	/**
	 * Get the ending vertex
	 * 
	 * @return ending vertex
	 */
	public Vertex<T> getTo() {
		return to;
	}

	/**
	 * Get the starting vertex
	 * 
	 * @return starting vertex
	 */
	public Vertex<T> getFrom() {
		return from;
	}

	/**
	 * Get the cost of the edge
	 * 
	 * @return cost of the edge
	 */
	public EdgeLabel getLabel() {
		return label;
	}

	/**
	 * Set the mark flag of the edge
	 * 
	 */
	public void mark() {
		mark = true;
	}

	/**
	 * Clear the edge mark flag
	 * 
	 */
	public void clearMark() {
		mark = false;
	}

	/**
	 * Get the edge mark flag
	 * 
	 * @return edge mark flag
	 */
	public boolean isMarked() {
		return mark;
	}

	/**
	 * String rep of edge
	 * 
	 * @return string rep with from/to vertex names and cost
	 */
	public String toString() {
		StringBuffer tmp = new StringBuffer("Edge[from: ");
		tmp.append(from.getName());
		tmp.append(", to: ");
		tmp.append(to.getName());
		tmp.append(", label: ");
		tmp.append(label);
		tmp.append("]");
		return tmp.toString();
	}

	@Override
	public boolean equals(Object e) {
		if (e == null)
			return false;
		if (!(e instanceof Edge<?>))
			return false;

		return ((Edge<?>) e).getFrom().equals(from)
				&& ((Edge<?>) e).getTo().equals(to);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
