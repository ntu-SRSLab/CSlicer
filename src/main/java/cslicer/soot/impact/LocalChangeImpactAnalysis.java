package cslicer.soot.impact;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import cslicer.CSlicer;
import cslicer.daikon.ChangedInvVar;
import cslicer.utils.BytecodeUtils;
import cslicer.utils.PrintUtils;
import soot.Body;
import soot.G;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.LiveLocals;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SmartLocalDefs;
import soot.util.Chain;

public class LocalChangeImpactAnalysis {

	class ImpactScope {
		final Body body;
		final Unit unit; // null if return stmt
		final Set<Local> local; // empty if return

		public ImpactScope(Body m, Unit u) {
			body = m;
			unit = u;
			local = new HashSet<>();
		}

		public ImpactScope(Body m, Unit u, Collection<Local> l) {
			this(m, u);
			local.addAll(l);
		}

		// treat null as return stmt
		public ImpactScope(Body m) {
			this(m, null);
		}

		private int getLineNumber() {
			int lineNumber = -1;
			LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
			if (tag != null)
				lineNumber = tag.getLineNumber();

			assert lineNumber > 0;

			return lineNumber;
		}

		public String toString() {
			String res = "";
			res += body.getMethod().getName();
			res += ":";
			res += unit == null ? "" : getLineNumber();
			res += ":";
			res += local.toString();
			return res;
		}
	}

	private final String rtlib = FilenameUtils.concat(CSlicer.SYSTEM_JAVA_HOME,
			"jre/lib/rt.jar");
	public Set<ImpactScope> fTargetMethods;
	public Set<ImpactScope> fTargetCallSites;
	public Set<ImpactScope> fTargetAccessPoints;

	public Set<String> fTargetEntities;
	private Set<String> fScopeClasses;

	/**
	 * Change Impact Analysis within a method.
	 * 
	 * @param classDirectory
	 *            directory containing the target class files.
	 * @param classPath
	 *            class path used to compile the target project.
	 * @param changedClasses
	 *            classes to analyze.
	 */
	public LocalChangeImpactAnalysis(String classDirectory, String classPath,
			final Set<String> changedClasses) {
		fTargetMethods = new HashSet<>();
		fTargetCallSites = new HashSet<>();
		fTargetAccessPoints = new HashSet<>();

		fTargetEntities = new HashSet<>();
		fScopeClasses = changedClasses;

		G.reset();

		Options.v().set_soot_classpath(classPath + ":" + rtlib);
		// Options.v().set_verbose(true);
		Options.v().set_java_version(Options.java_version_1_7);
		Options.v().set_keep_line_number(true);
		Options.v().set_debug(false);
		Options.v().set_verbose(false);
		Options.v().set_whole_program(false);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		// Options.v().app();
		// Options.v().set_process_dir(Arrays.asList(classDirectory));
		Scene.v().loadBasicClasses();
	}

	public void computeImpactAtCallSites() {
		for (ImpactScope callSite : fTargetCallSites) {
			UnitGraph graph = new ExceptionalUnitGraph(callSite.body);
			LiveLocals liveLocals = new SimpleLiveLocals(graph);
			SmartLocalDefs da = new SmartLocalDefs(graph, liveLocals);

			List<Unit> defs = new LinkedList<>();

			for (Local l : callSite.local) {
				defs.addAll(da.getDefsOfAt(l, callSite.unit));
			}

			PrintUtils.print(defs);
		}
	}

	/**
	 * Infer source code that might impact changed invariants.
	 * 
	 * @param impacts
	 *            a collection of change invariants {@link ChangedInvVar}.
	 */
	public void inferImpactingSources(Collection<ChangedInvVar> impacts) {
		Set<String> callNames = new HashSet<>();
		Set<String> methodNames = new HashSet<>();
		Set<String> fieldNames = new HashSet<>();

		for (ChangedInvVar imp : impacts) {
			switch (imp.getType()) {
			case FIELD_VALUE:
				String fieldEntity = BytecodeUtils
						.getQualifiedFieldNameFromDaikon(imp);
				fieldNames.add(fieldEntity);
				break;
			case METHOD_POSTCOND:
				String methodEntity = BytecodeUtils
						.getQualifiedMethodNameFromDaikon(imp);
				methodNames.add(methodEntity);
				break;
			case METHOD_PRECOND:
				callNames.add(
						BytecodeUtils.getQualifiedMethodNameFromDaikon(imp));
				break;
			default:
				break;
			}
		}

		searchAnalysisTargets(callNames, methodNames, fieldNames);

		// computeImpactAtCallSites();
	}

	public void searchAnalysisTargets(final Collection<String> callNames,
			final Collection<String> methodNames,
			final Collection<String> fieldNames) {

		// PackManager.v().getPack("jtp")
		// .add(new Transform("jtp.FindTargets", new BodyTransformer() {
		// @Override
		// protected void internalTransform(Body body, String phase,
		// Map options) {

		for (String c : fScopeClasses) {
			SootClass clazz = Scene.v().loadClassAndSupport(c);
			clazz.setApplicationClass();

			for (SootMethod m : clazz.getMethods()) {

				if (!m.isConcrete())
					continue;

				Body body = m.retrieveActiveBody();

				// SootMethod m = body.getMethod();

				// looking for method body
				if (methodNames.contains(standardMethodName(m))) {
					fTargetMethods.add(new ImpactScope(body));
					fTargetEntities.add(standardMethodName(m));
				}

				for (Unit u : body.getUnits()) {
					// looking for field access points
					for (ValueBox b : u.getDefBoxes()) {
						if (b.getValue() instanceof FieldRef) {
							SootField f = ((FieldRef) b.getValue()).getField();
							if (fieldNames.contains(standardFieldName(f))) {
								// locals that might impact field
								fTargetAccessPoints.add(
										new ImpactScope(body, u, localUses(u)));
								fTargetEntities.add(standardFieldName(f));
							}
						}
					}

					for (ValueBox b : u.getUseBoxes()) {
						if (b.getValue() instanceof FieldRef) {
							SootField f = ((FieldRef) b.getValue()).getField();
							if (fieldNames.contains(standardFieldName(f))) {
								fTargetAccessPoints.add(
										new ImpactScope(body, u, localUses(u)));
								fTargetEntities.add(standardFieldName(f));
							}
						}
					}

					// looking for method call sites
					if (u instanceof InvokeStmt) {
						InvokeStmt i = (InvokeStmt) u;
						PrintUtils.print(standardMethodName(
								i.getInvokeExpr().getMethod()));

						if (callNames.contains(standardMethodName(
								i.getInvokeExpr().getMethod()))) {

							// each local argument is within impact
							// scope
							// XXX can also consider field arguments
							fTargetCallSites.add(
									new ImpactScope(body, u, localUses(u)));
							fTargetEntities.add(standardMethodName(m));
						}
					}
				}
			}
		}

		// }));

		// String[] args = new String[1];
		// args[0] = "-v";
		// soot.Main.main(args);
		//
		PrintUtils.print(fTargetAccessPoints);
		PrintUtils.print(fTargetMethods);
		PrintUtils.print(fTargetCallSites);

	}

	private String standardFieldName(SootField f) {
		return BytecodeUtils.getQualifiedFieldName(f.getName(),
				Scene.v().quotedNameOf(f.getDeclaringClass().getName()),
				f.getType().toString());
	}

	private String standardMethodName(SootMethod m) {
		return BytecodeUtils.getQualifiedMethodName(
				Scene.v().quotedNameOf(m.getDeclaringClass().getName()),
				m.getName(), "(" + m.getBytecodeParms() + ")");
	}

	private Set<Local> localUses(Unit u) {
		Set<Local> defs = new HashSet<>();

		for (ValueBox v : u.getUseBoxes()) {
			if (v.getValue() instanceof Local)
				defs.add((Local) v.getValue());
		}

		return defs;
	}

	private Set<Local> localDefs(Unit u) {
		Set<Local> defs = new HashSet<>();

		for (ValueBox v : u.getDefBoxes()) {
			if (v.getValue() instanceof Local)
				defs.add((Local) v.getValue());
		}

		return defs;
	}

	private Set<Unit> returnStmts(Body body) {
		Set<Unit> res = new HashSet<>();
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> it = units.snapshotIterator();

		while (it.hasNext()) {
			Stmt u = (Stmt) it.next();
			if (u == units.getLast())
				continue;

			if (u instanceof ReturnStmt) {
				res.add(u);
			} else if (u instanceof ReturnVoidStmt) {
				res.add(u);
			}
		}
		return res;
	}
}
