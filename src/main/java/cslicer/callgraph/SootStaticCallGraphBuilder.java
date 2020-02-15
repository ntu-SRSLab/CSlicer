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

import java.util.ArrayList;
import java.util.List;

import cslicer.utils.PrintUtils;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class SootStaticCallGraphBuilder extends StaticCallGraphBuilder {

	public SootStaticCallGraphBuilder(String classPath) {
		Options.v().set_whole_program(true);
		Options.v().set_verbose(true);
		Options.v().set_debug(true);

		Scene.v().setSootClassPath(classPath);
		//SootClass entry = Scene.v().loadClassAndSupport("cslicer.analyzer.Refactor");
		Scene.v().loadBasicClasses();

		// Set entry points
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();

		for (SootClass sc : Scene.v().getClasses()) {
			if (sc.declaresMethodByName("main"))
				entryPoints.add(sc.getMethodByName("main"));
		}

		Scene.v().setEntryPoints(entryPoints);
	}

	@Override
	public void buildCallGraph() {
		CHATransformer.v().transform();
		CallGraph cg = Scene.v().getCallGraph();
		PrintUtils.print(cg.toString());
	}

	@Override
	public StaticCallGraph getCallGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveCallGraph(String path) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadCallGraph(String path) {
		// TODO Auto-generated method stub

	}
}
