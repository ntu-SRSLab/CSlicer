package cslicer.analyzer;

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

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cslicer.utils.StatsUtils;

// H2. YARN-2767. Added a test case to verify that http static user cannot kill or submit apps in the secure mode. Contributed by Varun Vasudev.
//
// JUnit test: 
// Running org.apache.hadoop.yarn.server.resourcemanager.webapp.TestRMWebServicesHttpStaticUserPermissions
// Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 51.935 sec - 
// in org.apache.hadoop.yarn.server.resourcemanager.webapp.TestRMWebServicesHttpStaticUserPermissions
// Build and test execution: 3 min 33 s
// Target commit: b4c951ab832f85189d815fb6df57eda4121c0199

public class TestReductionH2 {

	private ProjectConfiguration config;

	@Before
	public void setup() {
		config = new ProjectConfiguration()
				.setBuildScriptPath("/home/liyi/bit/hadoop/pom.xml")
				.setRepositoryPath("/home/liyi/bit/hadoop/.git")
				.setEndCommitId("b4c951ab832f85189d815fb6df57eda4121c0199")
				.setJacocoExecPath(
						"/home/liyi/Dropbox/Ideas/dep/testdata/hadoop/h2/jacoco.exec")
				.setSourceRootPath("/home/liyi/bit/hadoop/src/main/java")
				.setClassRootPath(
						"/home/liyi/bit/hadoop/maven-model-builder/target/classes")
				.setTouchSetPath("/tmp/touch-h2.txt");
	}

	@Ignore
	@Test
	public void testMedium() throws Exception {
		config.setAnalysisLength(100);
		Slicer ref = new Slicer(config);
		ref.doSlicing();
		StatsUtils.print();
	}

	@Ignore
	@Test
	public void testShort() throws Exception {
		config.setAnalysisLength(50);
		Slicer ref = new Slicer(config);
		ref.doSlicing();
		StatsUtils.print();
	}

	// @Ignore
	@Test
	public void testLong() throws Exception {
		config.setAnalysisLength(150);
		Slicer ref = new Slicer(config);
		List<RevCommit> drop = ref.doSlicing().getPicks();
		StatsUtils.print();

		ref.verifyResultPicking(drop);
	}
}
