package cslicer.soot.slicing;

import java.util.HashSet;

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

import java.util.List;
import java.util.Set;

import cslicer.utils.graph.Graph;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.JastAddJ.SwitchStmt;
import soot.jimple.IfStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.PDGNode;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.LiveLocals;
import soot.toolkits.scalar.SimpleLiveLocals;

/**
 * @author Yi Li
 *
 */
public class SimpleDataDependency {
	private LiveLocals fLive;
	private DependencyAnalysis fAnalysis;
	private UnitGraph fCFG;
	private Graph<PDGNode> fDataDepGraph;

	public SimpleDataDependency(UnitGraph graph,
			SimpleLiveLocals simpleLiveLocals) {

		fLive = simpleLiveLocals;
		fCFG = graph;
		fAnalysis = new DependencyAnalysis(graph);
	}

	private Local localDef(Unit u) {
		List<ValueBox> defBoxes = u.getDefBoxes();
		int size = defBoxes.size();
		if (size == 0)
			return null;
		if (size != 1)
			throw new RuntimeException();
		ValueBox vb = (ValueBox) defBoxes.get(0);
		Value v = vb.getValue();
		if (!(v instanceof Local))
			return null;
		return (Local) v;
	}

	Set<Unit> visited = new HashSet<Unit>();

	public Set<Unit> getDepsAt(Unit u) {
		visited.add(u);
		Set<Unit> deps = new HashSet<Unit>();
		Set<Unit> direct = fAnalysis.getFlowAfter(u).fDataDependency;
		deps.addAll(direct);

		for (Unit d : direct) {
			if (!visited.contains(d)) {
				deps.addAll(getDepsAt(d));
			}
		}

		return deps;
	}

	class DependencyAnalysis
			extends ForwardFlowAnalysis<Unit, DependencyFlowSet> {

		public DependencyAnalysis(UnitGraph graph) {
			super(graph);
			doAnalysis();
		}

		@Override
		protected void flowThrough(DependencyFlowSet in, Unit d,
				DependencyFlowSet out) {
			// PrintUtils.print("flowThrough:" + d.toString());
			in.copy(out);
			Set<Unit> deps = directDataDeps(d, out);

			if (d instanceof IfStmt || d instanceof SwitchStmt) {
				return;
			} else {
				out.fDataDependency.clear();
				out.updateLocal(localDef(d), d);
				out.fDataDependency.addAll(deps);
			}
		}

		@Override
		protected void merge(DependencyFlowSet in1, DependencyFlowSet in2,
				DependencyFlowSet out) {
			// PrintUtils.print("merge");
			in1.copy(out);
			out.merge(in2);
		}

		@Override
		protected void copy(DependencyFlowSet source, DependencyFlowSet dest) {
			// PrintUtils.print("copy");
			source.copy(dest);
		}

		@Override
		protected DependencyFlowSet newInitialFlow() {
			// PrintUtils.print("newInitialFlow");
			return new DependencyFlowSet();
		}

		@Override
		protected DependencyFlowSet entryInitialFlow() {
			// PrintUtils.print("entryInitialFlow");
			return new DependencyFlowSet();
		}

		// return map from Local --> Unit
		private Set<Unit> directDataDeps(Unit u, DependencyFlowSet flow) {
			Set<Unit> deps = new HashSet<Unit>();
			List<ValueBox> useBoxes = u.getUseBoxes();

			for (ValueBox vb : useBoxes) {
				Value v = vb.getValue();

				if (v instanceof Local) {
					deps.addAll(flow.fLocalLastDef.get(v));
				} else {
					continue;
				}
			}

			return deps;
		}
	}
}
