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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PrecisionTest3 {

	Slicer ref;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		// ProjectConfiguration config = new ProjectConfiguration()
		// .setBuildScriptPath("/home/liyi/bit/hadoop/build.xml")
		// .setRepositoryPath("/home/liyi/bit/hadoop/.git")
		// .setStartCommitId("99a2267e7b50b7bc15441d91f6f38a2edef20a82")
		// .setEndCommitId("5317558fd50c3002fe3507e86a6d05f679444908")
		// .setEnableBuilderOutput(true)
		// .setJacocoExecPath("/home/liyi/bit/hadoop/jacoco.exec")
		// .setSourceRootPath("/home/liyi/bit/hadoop/src/java")
		// .setClassRootPath("/home/liyi/bit/hadoop/build/classes")
		// .setExcludedPaths(
		// new HashSet<String>(Arrays.asList("CHANGES.txt")));
		//
		// ref = new Slicer(config);
	}

	// @Test
	// public void test() throws Exception {
	// List<RevCommit> hunk = ref.doSlicing(false, false);
	// assertTrue(ref.verifyResultPicking(hunk));
	// }

	@Test
	public void testCompare() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration()
				.setRepositoryPath("/Users/liyi/Documents/bit/maven/.git")
				.setAnalysisLength(800)
				.setBuildScriptPath(
						"/Users/liyi/Documents/bit/maven/maven-core/pom.xml")
				.setSourceRootPath(
						"/Users/liyi/Documents/bit/maven/maven-core/src/main/java")
				.setClassRootPath(
						"/Users/liyi/Documents/bit/maven/maven-core/target/classes")
				.setEndCommitId("9f7971dadbec8882b4c119345494b620d3a1f897")
				.setJacocoExecPath("EXEC_PATH").setJavaSlicerDumpPath(
						"/home/polaris/Desktop/Feature-toolchain/javaslicer/20percent1/DefaultToolchainsBuilderTest.trace,/home/polaris/Desktop/Feature-toolchain/javaslicer/20percent1/DefaultToolchainTest.trace,/home/polaris/Desktop/Feature-toolchain/javaslicer/20percent1/MavenToolchainMergerTest.trace,/home/polaris/Desktop/Feature-toolchain/javaslicer/20percent1/RequirementMatcherFactoryTest.trace,/home/polaris/Desktop/Feature-toolchain/javaslicer/20percent1/ToolchainsBuildingExceptionTest.trace");
		ref = new Slicer(config);
		ref.doSlicing();
	}
}
