package cslicer.soot.stats;

import java.util.ArrayList;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import cslicer.CSlicer;
import cslicer.utils.PrintUtils;
import soot.Body;
import soot.BodyTransformer;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.options.Options;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class MethodStats {

	private final String[] assertMethodNames = { "assertEquals", "assertFalse",
			"assertTrue", "assertNotNull", "assertNull", "assertNotSame",
			"assertSame", "assertThat" };
	private final String rtlib = FilenameUtils.concat(CSlicer.SYSTEM_JAVA_HOME,
			"lib/rt.jar");
	private final Set<String> fAssertMethods = new HashSet<String>(
			Arrays.asList(assertMethodNames));

	private List<SootMethod> fEntryPoints;
	private CallGraph fCg;

	public MethodStats(String classPath, String testPath) {
		Options.v().set_soot_classpath(classPath + ":" + rtlib);
		Options.v().set_verbose(false);
		Options.v().set_debug(false);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		// Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_process_dir(Arrays.asList(testPath));
		// Options.v().setPhaseOption("wjop.si",
		// Options.getDefaultOptionsForPhase("wjop.si"));
		Options.v().setPhaseOption("cg", "verbose:true,implicit-entry:false");

		Scene.v().loadNecessaryClasses();

		fEntryPoints = new ArrayList<SootMethod>();
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod m : sc.getMethods()) {
				String mName = m.getName();
				if (mName.startsWith("test") || mName.startsWith("Test")
						|| mName.endsWith("test") || mName.endsWith("Test")) {
					if (!m.isConcrete())
						continue;

					fEntryPoints.add(m);
				}
			}
		}

		buildCallGraph();
	}

	private void buildCallGraph() {

		List<SootMethod> oneEntry = new LinkedList<SootMethod>();
		oneEntry.add(fEntryPoints.get(0));
		Scene.v().setEntryPoints(oneEntry);
		CHATransformer.v().transform();
		fCg = Scene.v().getCallGraph();

		Set<MethodOrMethodContext> entry = new HashSet<MethodOrMethodContext>();
		entry.add((SootMethod) fEntryPoints.get(0));
		ReachableMethods ream = new ReachableMethods(fCg, entry.iterator());
		PrintUtils.print(ream);
	}

	public void processTests() {

		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.TestStats", new BodyTransformer() {
					@Override
					protected void internalTransform(Body body, String phase,
							Map options) {
						// PrintUtils.print(getStats(body));
					}
				}));

		String[] args = new String[1];
		args[0] = "-time";
		soot.Main.main(args);
	}

	public String getStats(Body body) {
		String className = body.getMethod().getDeclaringClass()
				.getJavaPackageName() + "."
				+ body.getMethod().getDeclaringClass().getJavaStyleName();
		String methodName = body.getMethod().getName();

		if (!methodName.contains("test") && !methodName.contains("Test"))
			return null;

		UnitGraph graph = new ExceptionalUnitGraph(body);
		int methodSize = graph.size();
		BlockGraph blocks = new ExceptionalBlockGraph(body);
		int blockCount = blocks.size();
		int localCount = body.getLocalCount();

		int assertCount = 0;
		int invokeCount = 0;

		for (Unit u : body.getUnits()) {
			if (u instanceof InvokeStmt) {
				invokeCount++;

				String invokeName = ((InvokeStmt) u).getInvokeExpr().getMethod()
						.getName();
				if (fAssertMethods.contains(invokeName))
					assertCount++;
			}
		}

		return className + "#" + methodName + " : (" + methodSize + ","
				+ blockCount + "," + invokeCount + "," + localCount + ","
				+ assertCount + ")";
	}
}
