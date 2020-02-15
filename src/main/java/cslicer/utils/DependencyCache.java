package cslicer.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Sets;

import cslicer.utils.graph.Edge;
import cslicer.utils.graph.Graph;
import cslicer.utils.graph.SpanningTreeVisitor;
import cslicer.utils.graph.Vertex;

public class DependencyCache {

	private Graph<RevCommit> fDeps;
	private Set<String> fDone; // dependency computed

	public DependencyCache() {
		fDeps = new Graph<RevCommit>();
		fDone = new HashSet<String>();
	}

	/**
	 * Add direct dependency edge to the graph.
	 * 
	 * @param c
	 *            source {@link RevCommit}
	 * @param deps
	 *            dependencies {@link RevCommit}
	 */
	public void addDirectDeps(RevCommit c, Set<RevCommit> deps) {
		Vertex<RevCommit> fromV = fDeps.findVertexByName(c.getName());

		if (fromV == null) {
			fromV = createVertex(c);
			fDeps.addVertex(fromV);
		}

		for (RevCommit d : deps) {
			Vertex<RevCommit> toV = fDeps.findVertexByName(d.getName());

			if (toV == null) {
				toV = createVertex(d);
				fDeps.addVertex(toV);
			}
			fDeps.addEdge(fromV, toV, DependencyEdgeType.HUNK_DEPS);
		}

		fDone.add(c.getName());
	}

	private Vertex<RevCommit> createVertex(RevCommit c) {
		return new Vertex<RevCommit>(c.getName(), c);
	}

	public boolean directDepsComputed(RevCommit c) {
		return fDone.contains(c.getName());
	}

	/**
	 * Return the transitive closure of all dependencies of a given commit.
	 * 
	 * @param c
	 *            target {@link RevCommit}
	 * @return a set of {@link RevCommit}
	 */
	public Set<RevCommit> getTransitiveDeps(RevCommit c) {
		Set<RevCommit> res = new HashSet<RevCommit>();
		Vertex<RevCommit> vertex = fDeps.findVertexByName(c.getName());
		if (vertex != null) {
			SpanningTreeVisitor<RevCommit> visitor = new SpanningTreeVisitor<RevCommit>(
					res);
			fDeps.dfsSpanningTree(vertex, visitor);
		}
		fDeps.clearEdges();
		fDeps.clearMark();

		return res;
	}

	/**
	 * Return a topologically sorted layers of nodes. Based on Kahn's algorithm.
	 * 
	 * @param range
	 *            sub-graph range
	 * @param reverse
	 *            reverse order
	 * @return a list of layered nodes data
	 */
	public List<Set<RevCommit>> getLayeredDeps(Collection<RevCommit> range,
			boolean reverse) {
		// get sub-graph
		Graph<RevCommit> sub = fDeps.subGraphByData(range);
		// topologically sorted layers
		List<Set<RevCommit>> L = new LinkedList<>();

		while (sub.size() > 0) {
			// iteratively remove nodes with no incoming edges
			Set<Vertex<RevCommit>> S = sub.getRootNodes();
			Set<RevCommit> layer = new HashSet<RevCommit>();

			while (S.size() > 0) {
				// remove a node n from S
				Vertex<RevCommit> n = S.iterator().next();
				S.remove(n);
				// remove n from graph
				sub.removeVertex(n);
				// add n to L
				layer.add(n.getData());
			}

			if (layer.isEmpty())
				break;

			L.add(layer);
		}

		if (reverse)
			Collections.reverse(L);

		return L;
	}

	public Set<Set<RevCommit>> applicableSubSets(Collection<RevCommit> range) {
		Map<Set<RevCommit>, Set<Set<RevCommit>>> cache = new HashMap<>();
		return applicableSubSets(new HashSet<>(range), cache);
	}

	private Set<Set<RevCommit>> applicableSubSets(Set<RevCommit> range,
			Map<Set<RevCommit>, Set<Set<RevCommit>>> cache) {

		if (range.size() <= 1) {
			Set<RevCommit> Vdata = new HashSet<>(range);
			Set<Set<RevCommit>> res = new HashSet<>();
			res.add(Vdata);
			return new HashSet<Set<RevCommit>>(res);
		}

		if (cache.containsKey(range))
			return cache.get(range);

		Set<Set<RevCommit>> L = new HashSet<>();
		Graph<RevCommit> sub = fDeps.subGraphByData(range);

		Set<Vertex<RevCommit>> R = sub.getRootNodes();
		Set<Vertex<RevCommit>> V = sub.getVerticies();
		Set<RevCommit> Vdata = new HashSet<>();
		for (Vertex<RevCommit> v : V)
			Vdata.add(v.getData());

		for (Vertex<RevCommit> r : R) {
			Set<RevCommit> subHist = Sets.difference(Vdata,
					Collections.singleton(r.getData()));
			L.add(subHist);
			L.addAll(applicableSubSets(subHist, cache));
		}

		cache.put(range, L);

		return L;
	}

	public List<RevCommit> getSortedDeps(boolean reverse) {
		return fDeps.topologicalSort(reverse);
	}

	public Set<RevCommit> getUnSortedDeps() {
		Set<RevCommit> res = new HashSet<RevCommit>();

		for (Vertex<RevCommit> v : fDeps.getVerticies())
			res.add(v.getData());
		return res;
	}

	/**
	 * Output dependency graph to file using DOT format.
	 * 
	 * @param filePath
	 *            path to target DOT file
	 */
	public void outputCacheToFile(String filePath) {
		fDeps.toDOT(filePath, true);
	}

	public void outputCacheToFacts(String filePath) {
		fDeps.toTA(filePath, "HunkDep", true);
	}

	@Override
	public String toString() {
		return "Dependency Graph:\n" + fDeps.toString();
	}

	public Set<RevCommit> getDirectDeps(RevCommit c) {
		Set<RevCommit> res = new HashSet<RevCommit>();
		Vertex<RevCommit> vertex = fDeps.findVertexByName(c.getName());
		if (vertex != null) {
			for (Edge<RevCommit> e : vertex.getOutgoingEdges()) {
				res.add(e.getTo().getData());
			}
		}
		return res;
	}
}
