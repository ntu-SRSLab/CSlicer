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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.ProjectConfigInvalidException;
import cslicer.analyzer.ProjectConfiguration;
import cslicer.analyzer.Slicer;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.daikon.ChangedInvVar.InvVarType;
import cslicer.distiller.ChangeDistillerException;
import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.JGit;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.jgit.split.CommitSplitter;
import cslicer.soot.impact.LocalChangeImpactAnalysis;
import cslicer.utils.BytecodeUtils;
import cslicer.utils.PrintUtils;
import daikon.DaikonSimple;
import daikon.PptTopLevel;
import daikon.VarInfo;
import daikon.diff.Diff;
import daikon.diff.InvMap;
import daikon.inv.Invariant;
import plume.UtilMDE;
import daikon.Daikon;

public class TestDaikonLibUtils {

	DaikonLoader loader = new DaikonLoader();

	@Test
	public void testLoadInvMapfromTrace() throws IOException {
		String traceFilePath = "/home/polaris/Desktop/123/test/v0/target/dtrace.gz";
		String config = "/home/polaris/Desktop/123/example-settings.txt";
		InvMap invMap = loader.loadInvMapfromTrace(traceFilePath, config);
		Iterator<PptTopLevel> pptIterator = invMap.pptIterator();
		while (pptIterator.hasNext()) {
			PptTopLevel topLevel = pptIterator.next();
			// if(topLevel.toString().startsWith("org.junit")
			// || topLevel.toString().startsWith("com.sun")
			// || topLevel.toString().startsWith("junit")
			// || topLevel.toString().startsWith("org.apache.maven"))
			// {
			// continue;
			// }
			// else
			{
				System.out.println(topLevel.toString());
				System.out.println(
						"\n===================================================");
				List<Invariant> invList = invMap.get(topLevel);
				for (Invariant inv : invList) {
					if (inv.toString().startsWith("warning:")) {
						continue;
					} else {
						System.out.println(inv);
					}
				}
			}
		}
	}

	@Test
	public void testGetVarsDiffMap() throws IOException {
		String traceFilePath = "/home/polaris/Desktop/PaperExample/v5/target/JUnitCore.dtrace.gz";
		String config = "/home/polaris/Desktop/123/example-settings.txt";
		InvMap invMap = loader.loadInvMapfromTrace(traceFilePath, config);
		Map<PptTopLevel, Set<VarInfo>> varDiffMap = DaikonLoader
				.getVarsDiffMap(invMap);
		 List<ChangedInvVar> varList =
		 DaikonLoader.getChangedVarsListFromVarDiffMap(varDiffMap);
		 for(ChangedInvVar var : varList)
		 {
		 System.out.println("===========================================");
		 System.out.println("[NAME]: " + var.getName());
		 System.out.println("[TYPE]: " + var.getType());
		 if(var.getType() == InvVarType.FIELD_VALUE)
		 {
			 String returnStr = BytecodeUtils.getQualifiedFieldNameFromDaikon(var);
			 System.out.println(returnStr);
		 }
		 //System.out.println("[VARINFO]: " + var.getVarInfo().repr());
		 }
		for (PptTopLevel ppt : varDiffMap.keySet()) {
			Set<VarInfo> varInfoSet = varDiffMap.get(ppt);
			for (VarInfo varInfo : varInfoSet) {
				//System.out.println("[VARINFO]: " + varInfo.repr());
			}
		}
	}

	@Test
	public void testGetChangedVarsListFromVarDiffMap() throws IOException {
		String trace0 = "/home/polaris/Desktop/123/v0.dtrace.gz";
		String trace1 = "/home/polaris/Desktop/123/v2.dtrace.gz";
		InvMap invMap0 = loader.simpleLoadInvMapfromTrace(trace0);
		InvMap invMap1 = loader.simpleLoadInvMapfromTrace(trace1);
		InvMap diffMap = loader.diffTwoInvMaps(invMap0, invMap1);

		Map<PptTopLevel, Set<VarInfo>> varDiffMap = DaikonLoader
				.getVarsDiffMap(diffMap);
		List<ChangedInvVar> varList = DaikonLoader
				.getChangedVarsListFromVarDiffMap(varDiffMap);
		for (ChangedInvVar var : varList) {
			System.out.println("[VARNAME]: " + var.getName());
			System.out.println("[AGRNUM]: " + var.getArgNum());
			System.out.println("[VARTYPE]: " + var.getType());
			System.out.println("[VARPPT]: " + var.getPpt());
			System.out.println(
					"=====================================================================");
		}
	}

	@Test
	public void testDiffTwoInvMaps() throws IOException {
		String trace0 = "/home/polaris/Desktop/123/test/v0/target/JUnitCore.dtrace.gz";
		String trace1 = "/home/polaris/Desktop/123/v2.dtrace.gz";
		String config = "/home/polaris/Desktop/123/example-settings.txt";
		InvMap invMap0 = loader.loadInvMapfromTrace(trace0, config);
		InvMap invMap1 = loader.loadInvMapfromTrace(trace1, config);
		System.out.println(invMap0.size());
		System.out.println(invMap1.size());

		InvMap diffMap = loader.diffTwoInvMaps(invMap0, invMap1);
		Iterator<PptTopLevel> itr = diffMap.pptIterator();
		while (itr.hasNext()) {
			PptTopLevel ppt = itr.next();
			if (diffMap.get(ppt).size() == 0) {
				continue;
			}
			System.out.println("===========================================");
			System.out.println(ppt.toString());
			System.out.println("-------------------------------------------");
			List<Invariant> invlist = diffMap.get(ppt);
			for (Invariant inv : invlist) {
				System.out.println(inv.toString());
			}
		}
	}

	@Test
	public void testGetMethodAndLines() throws IOException {
		// String trace0 = "/home/polaris/Desktop/123/v0.dtrace.gz";
		// String trace1 = "/home/polaris/Desktop/123/v1.dtrace.gz";
		String config = "/home/polaris/Desktop/123/example-settings.txt";
		String trace0 = "/home/polaris/Desktop/PaperExample/v5/target/JUnitCore.dtrace.gz";
		String trace1 = "/home/polaris/Desktop/PaperExample/case1/target/JUnitCore.dtrace.gz";
		InvMap invMap0 = loader.loadInvMapfromTrace(trace0, config);
		InvMap invMap1 = loader.loadInvMapfromTrace(trace1, config);
		InvMap diffMap = loader.diffTwoInvMaps(invMap0, invMap1);

		Iterator<PptTopLevel> itr = invMap0.pptIterator();
		while (itr.hasNext()) {
			PptTopLevel ppt = itr.next();
			System.out.println("===========================================");
			System.out.println(ppt.toString());
			System.out.println("-------------------------------------------");
			for (Invariant inv : invMap0.get(ppt)) {
				System.out.println(inv);
			}
		}

		Map<PptTopLevel, Set<VarInfo>> varDiffMap = DaikonLoader
				.getVarsDiffMap(diffMap);
		List<ChangedInvVar> varList = DaikonLoader
				.getChangedVarsListFromVarDiffMap(varDiffMap);
		for (ChangedInvVar var : varList) {
			System.out.println(var.toString());
		}
		String classpath0 = "/home/polaris/Desktop/PaperExample/v5/target/classes";
		// String classpath1 =
		// "/home/polaris/Desktop/123/test/v1/target/classes";

		// LocalChangeImpactAnalysis lcia = new LocalChangeImpactAnalysis(
		// classpath0, classpath0);
		// lcia.inferImpactingSources(varList);
	}

	@Test
	public void testDaikonCommandLine()
			throws FileNotFoundException, IOException {
		String[] args = { "--config",
				"/home/polaris/Desktop/123/example-settings.txt",
				"/home/polaris/Desktop/123/test/v0/target/JUnitCore.dtrace.gz" };
		Daikon.mainHelper(args);
	}

	@Test
	public void testCommitSplitter() throws RepositoryInvalidException,
			CommitNotFoundException, ChangeDistillerException {
		JGit jgit = new JGit("/home/polaris/Desktop/jacocoexample/maven/.git");
		CommitSplitter cs = new CommitSplitter(jgit.getRepo(), 3);
		ChangeExtractor extractor = new ChangeExtractor(jgit, "1.7");
		Set<GitRefSourceCodeChange> changeSet = extractor.extractChanges(
				jgit.getCommit("f8afa711df2aeab23d2d08214ba100ee704d23ba"));
		cs.splitCommit(
				jgit.getCommit("f8afa711df2aeab23d2d08214ba100ee704d23ba"),
				jgit.getCommit("1d84cbeffad39dcd57f85cb312e17c46c9d3dac7"),
				new HashSet<String>(), changeSet);
		System.out.println(
				"[CHANGE-PATCH MAP SIZE]: " + cs.getChangePatchMap().size());
		for (GitRefSourceCodeChange change : cs.getChangePatchMap().keySet()) {
			System.out.println(
					"====================================================");
			System.out.println(
					"[CHANGE]: " + change.getSourceCodeChange().toString());
			System.out
					.println("[PATCH]: " + cs.getChangePatchMap().get(change));
		}
		System.out.println(
				"[PATCH-CHANGE MAP SIZE]: " + cs.getPatchChangeMap().size());
		for (String s : cs.getPatchChangeMap().keySet()) {
			System.out.println(
					"####################################################");
			System.out.println("[PATCH]: " + s);
			System.out.println("[CHANGE LIST]: ");
			for (GitRefSourceCodeChange c : cs.getPatchChangeMap().get(s)) {
				System.out.println(c.getSourceCodeChange().toString());
			}
		}
	}

	private RevCommit target;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		// the example in gitref.pdf
		target = TestUtils.setupDaikonRepo(tempFolder, BUILD_SYSTEM.MAVEN);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRefinementLoop() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Slicer ref = new Slicer(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setEnableBuilderOutput(false).setStartCommitId(target.name())
				.setOutputHunkGraph(true)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder)));

		ref.doSlicing();
		// Get all changes set.
		// Set<GitRefSourceCodeChange> changeSet =
		// ref.extractChangesForDaikon();

		// run test, get end trace file and change dependency graph
		// boolean isPass = ref.verifyResultTestPassing();//generate trace file
		// if(!isPass)
		// {
		// System.out.println("[NOT PASS AT END VERSION!]");
		// return;
		// }

		// calculate invariants of end version (= last-passed-invariants-set)
		// String settings = "/home/polaris/Desktop/123/example-settings.txt";
		// String endVersionTrace = "";
		// InvMap endVersionInvMap = DaikonLibUtils
		// .loadInvMapfromTrace(endVersionTrace, settings);

		// while we have not achieve optimal {

		// Throw out 1 change-dep-combo randomly or by rank.

		// calculate invariants of this new version

		// calculate the minus-set of invariants

		// Try if we can pass test.

		// if we can pass, then lower the minus-set of invariants, update
		// last-passed-invariants-set.

		// else, we higher the minus-set of invariants. add this
		// change-dep-combo back.

		// for all remaining changes, calculate the rank value for them.

		// } end while
	}

	public void testVerifyingTest() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Slicer ref = new Slicer(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setEnableBuilderOutput(false).setStartCommitId(target.name())
				.setOutputHunkGraph(true)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder)));

		// ref.verifyResultTestPassing();
	}

}
