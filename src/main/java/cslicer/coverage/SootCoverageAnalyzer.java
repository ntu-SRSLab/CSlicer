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
 * Copyright (C) 2014 - 2019 Department of Computer Science, University of Toronto
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.FileUtils;

import cslicer.CSlicer;
import cslicer.builder.UnitTestScope;
import cslicer.soot.slicing.SimpleDataDependency;
import cslicer.utils.BytecodeUtils;
import cslicer.utils.PrintUtils;
import soot.AbstractJasminClass;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;

public class SootCoverageAnalyzer implements ICoverageAnalyzer {

	private Collection<String> fClassNames;
	private final String rtlib = FilenameUtils.concat(CSlicer.SYSTEM_JAVA_HOME,
			"lib/rt.jar");
	private final String junit = CSlicer.JUNIT_JAR_PATH;

	public SootCoverageAnalyzer(String testClassPath,
			Collection<String> classNames, Set<String> slicingCriteria) {

		Options.v().set_soot_classpath(AppendDefaultClassPath(testClassPath));
		Options.v().set_verbose(true);
		Options.v().keep_line_number();
		PhaseOptions.v().setPhaseOption("jb", "use-original-names");
		Options.v().set_allow_phantom_refs(false);

		fClassNames = classNames;
	}

	private String AppendDefaultClassPath(String testClassPath) {
		if (!FileUtils.fileExists(junit))
			return rtlib + ":" + testClassPath;

		return rtlib + ":" + junit + ":" + testClassPath;
	}

	@Override
	public CoverageDatabase analyseCoverage()
			throws CoverageControlIOException, TestFailureException {

		// register data dependency analysis with soot
		PackManager.v().getPack("jtp").add(
				new Transform("jtp.SlicingTransform", new BodyTransformer() {
					@Override
					protected void internalTransform(Body body, String phase,
							Map options) {
						UnitGraph cfg = new ExceptionalUnitGraph(body);

						SimpleDataDependency analysis = new SimpleDataDependency(
								cfg, new SimpleLiveLocals(cfg));

						// HashMutablePDG controlFlow = new HashMutablePDG(cfg);

						// PrintUtils.print(controlFlow.toString());

						// SmartLocalDefs dataFlow = new SmartLocalDefs(cfg,
						// new SimpleLiveLocals(cfg));

						Iterator<Unit> uIt = body.getUnits().iterator();

						while (uIt.hasNext()) {
							Unit u = (Unit) uIt.next();

							if (u.toString().contains("assertEquals")) {
								Iterator<ValueBox> useBoxIt = u.getUseBoxes()
										.iterator();
								while (useBoxIt.hasNext()) {
									Value v = (useBoxIt.next()).getValue();
									if (v instanceof Local) {
										Set<Unit> l = analysis.getDepsAt(u);
										Set<String> func = extractFuncElements(
												l);
										PrintUtils.print(func);
									}
								}
							}

							// DependencyFlowSet s = (DependencyFlowSet)
							// analysis
							// .getFlowAfter(u);
							// PrintUtils
							// .print(u.toString() + " : " + s.toString());
						}
					}

				}));

		// fire up the analysis
		PrintUtils.print(Options.v().soot_classpath());
		String[] names = new String[fClassNames.size()];
		soot.Main.main(fClassNames.toArray(names));

		return null;
	}

	private Set<String> extractFuncElements(Set<Unit> l) {
		Set<String> res = new HashSet<String>();

		for (Unit u : l) {
			Iterator<ValueBox> useBoxIt = u.getUseBoxes().iterator();
			while (useBoxIt.hasNext()) {
				Value v = (useBoxIt.next()).getValue();
				if (v instanceof InvokeExpr) {
					String className = ((InvokeExpr) v).getMethodRef()
							.declaringClass().getJavaStyleName();
					String methodname = ((InvokeExpr) v).getMethod().getName();
					String signature = AbstractJasminClass.jasminDescriptorOf(
							((InvokeExpr) v).getMethodRef());
					res.add(BytecodeUtils.getQualifiedMethodName(className,
							methodname, signature));
				} else if (v instanceof FieldRef) {
					res.add(((FieldRef) v).getFieldRef().getSignature());
				} else if (v instanceof NewExpr) {
					res.add(((NewExpr) v).getBaseType().getSootClass()
							.getJavaStyleName());
				}
			}
		}
		return res;
	}

	@Override
	public CoverageDatabase analyseCoverage(UnitTestScope scope)
			throws CoverageControlIOException, TestFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public COVERAGE_TYPE analysisType() {
		return COVERAGE_TYPE.DEFAULT;
	}

}
