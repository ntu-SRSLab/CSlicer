package cslicer.daikon;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cslicer.utils.PrintUtils;
import daikon.Daikon;
import daikon.DaikonSimple.SimpleProcessor;
import daikon.DynamicConstants;
import daikon.FileIO;
import daikon.PptMap;
import daikon.PptSlice2;
import daikon.PptSlice3;
import daikon.PptTopLevel;
import daikon.PrintInvariants;
import daikon.VarInfo;
import daikon.diff.Diff;
import daikon.diff.InvMap;
import daikon.diff.MinusVisitor;
import daikon.diff.RootNode;
import daikon.inv.Invariant;
import daikon.suppress.NIS;
import plume.UtilMDE;

public class DaikonLoader {

	private final boolean EXAMINE_ALL_PPTS = false;

	public Diff diff;

	public DaikonLoader() {
		diff = new Diff(EXAMINE_ALL_PPTS,
				new Invariant.ClassVarnameFormulaComparator());
	}

	public InvMap loadInvMapFromInvFile(String filePath)
			throws IOException, ClassNotFoundException {
		File file = new File(filePath);

		Object o = UtilMDE.readObject(file);
		if (o instanceof InvMap) {
			return (InvMap) o;
		} else {
			PptMap pptMap = FileIO.read_serialized_pptmap(file, false);
			return diff.convertToInvMap(pptMap);
		}
	}

	public InvMap diffTwoInvMaps(InvMap map1, InvMap map2) {
		MinusVisitor v = new MinusVisitor();
		RootNode root = diff.diffInvMap(map1, map2);
		root.accept(v);
		InvMap minusMap = v.getResult();

		return minusMap;
	}

	public static Map<PptTopLevel, Set<VarInfo>> getVarsDiffMap(InvMap map) {
		Map<PptTopLevel, Set<VarInfo>> varDiffMap = new HashMap<>();
		Iterator<PptTopLevel> pptIterator = map.pptIterator();
		while (pptIterator.hasNext()) {
			PptTopLevel ppt = pptIterator.next();
			if (isUninterestingPpt(ppt)) {
				continue;
			}
			List<Invariant> invList = map.get(ppt);
			Set<VarInfo> varSet = new LinkedHashSet<>();
			for (Invariant inv : invList) {
				if (inv.toString().startsWith("warning:")) {
					continue;
				}
				if (inv.ppt.var_infos.length > 1) {
					continue;
				}
				for (VarInfo varInfo : inv.ppt.var_infos) {
					if (varInfo.str_name().startsWith("orig(")) {
						continue;
					}
					varSet.add(varInfo);
				}
			}
			varDiffMap.put(ppt, varSet);
		}

		return varDiffMap;
	}

	public List<ChangedInvVar> getChangedVars(InvMap oldMap, InvMap newMap) {
		return getChangedVarsListFromVarDiffMap(
				getVarsDiffMap(diffTwoInvMaps(oldMap, newMap)));
	}

	public static List<ChangedInvVar> getChangedVarsListFromVarDiffMap(
			Map<PptTopLevel, Set<VarInfo>> varDiffMap) {
		List<ChangedInvVar> varList = new ArrayList<>();
		for (PptTopLevel ppt : varDiffMap.keySet()) {
			for (VarInfo varInfo : varDiffMap.get(ppt)) {
				ChangedInvVar var = new ChangedInvVar(varInfo, ppt);
				varList.add(var);
			}
		}

		return varList;
	}

	public static boolean isUninterestingPpt(PptTopLevel ppt) {
		String pptStr = ppt.toString();
		if (pptStr.startsWith("com.sun.")) {
			return true;
		}
		if (pptStr.startsWith("org.junit.")) {
			return true;
		}
		if (pptStr.startsWith("junit.")) {
			return true;
		}

		return false;
	}

	public InvMap loadInvMapfromTrace(String traceFile, String configFile)
			throws IOException {
		InvMap res = new InvMap();
		try {
			res = diff.convertToInvMap(
					Daikon.loadInvMapFromTraceFile(traceFile, configFile));
		} catch (Daikon.TerminationMessage e) {
			PrintUtils.print(e.getMessage());
		}
		return res;
	}

	public InvMap simpleLoadInvMapfromTrace(String traceFile)
			throws IOException {
		PptMap all_ppts;

		daikon.LogHelper.setupLogs(daikon.LogHelper.INFO);

		Daikon.use_equality_optimization = false;
		DynamicConstants.dkconfig_use_dynamic_constant_optimization = false;
		Daikon.suppress_implied_controlled_invariants = false;
		NIS.dkconfig_enabled = false;

		Daikon.using_DaikonSimple = true;

		Set<File> decls_files = new HashSet<File>();

		Set<String> dtrace_files = new HashSet<String>();
		dtrace_files.add(traceFile);

		if ((decls_files.size() == 0) && (dtrace_files.size() == 0)) {
			throw new Daikon.TerminationMessage(
					"No .decls or .dtrace files specified");
		}

		Daikon.setup_proto_invs();

		all_ppts = FileIO.read_declaration_files(decls_files);

		SimpleProcessor processor = new SimpleProcessor();
		FileIO.read_data_trace_files(dtrace_files, all_ppts, processor, true);

		InvMap invMap = diff.convertToInvMap(all_ppts);
		return invMap;
	}

	public static void creatInvFileFromTraceFile(String traceFile,
			String invFile) throws IOException, FileNotFoundException {
		PptMap all_ppts;

		daikon.LogHelper.setupLogs(daikon.LogHelper.INFO);

		Daikon.use_equality_optimization = false;
		DynamicConstants.dkconfig_use_dynamic_constant_optimization = false;
		Daikon.suppress_implied_controlled_invariants = false;
		NIS.dkconfig_enabled = false;

		Daikon.using_DaikonSimple = true;

		Set<File> decls_files = new HashSet<File>();

		Set<String> dtrace_files = new HashSet<String>();
		dtrace_files.add(traceFile);

		if ((decls_files.size() == 0) && (dtrace_files.size() == 0)) {
			throw new Daikon.TerminationMessage(
					"No .decls or .dtrace files specified");
		}

		Daikon.setup_proto_invs();

		all_ppts = FileIO.read_declaration_files(decls_files);

		SimpleProcessor processor = new SimpleProcessor();
		FileIO.read_data_trace_files(dtrace_files, all_ppts, processor, true);

		for (PptTopLevel ppt : all_ppts.pptIterable()) {

			if (ppt.num_samples() == 0) {
				continue;
			}
			List<Invariant> invs = PrintInvariants
					.sort_invariant_list(ppt.invariants_vector());
			List<Invariant> filtered_invs = filter_invs(invs);
		}

		File inv_file = new File(invFile);
		try {
			FileIO.write_serialized_pptmap(all_ppts, inv_file);
		} catch (IOException e) {
			throw new RuntimeException(
					"Error while writing .inv file: " + inv_file, e);
		}
	}

	static List<Invariant> filter_invs(List<Invariant> invs) {
		List<Invariant> new_list = new ArrayList<Invariant>();

		for (Invariant inv : invs) {
			VarInfo[] vars = inv.ppt.var_infos;

			// This check is the most non-intrusive way to filter out the invs
			// Filter out reflexive invariants in the binary invs
			if (!((inv.ppt instanceof PptSlice2) && vars[0] == vars[1])) {

				// Filter out the reflexive and partially reflexive invs in the
				// ternary slices
				if (!((inv.ppt instanceof PptSlice3) && (vars[0] == vars[1]
						|| vars[1] == vars[2] || vars[0] == vars[2]))) {
					if (inv.ppt.num_values() != 0) {

						// filters out "warning: too few samples for
						// daikon.inv.ternary.threeScalar.LinearTernary
						// invariant"
						if (inv.isActive()) {
							new_list.add(inv);
						}
					}
				}
			}
		}

		return new_list;
	}

	public static Set<String> getVariablesFromInvariant(Invariant inv) {
		Set<String> variables = new LinkedHashSet<>();

		for (VarInfo varInfo : inv.ppt.var_infos) {
			System.out.println("[VARINFO]: " + varInfo.aux);
			variables.add(varInfo.get_simplify_size_name());
		}

		return variables;
	}
}
