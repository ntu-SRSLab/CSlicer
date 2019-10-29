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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.analyzer.Refiner.SCHEME;
import cslicer.builder.UnitTestScope;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;

public class TestRefiner {

	private RevCommit target;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		// target = TestUtils.setupDaikonPaperRepo(tempFolder,
		// BUILD_SYSTEM.MAVEN);
		target = TestUtils.setupDaikonPartitionRepo(tempFolder,
				BUILD_SYSTEM.MAVEN);
	}

	@Test
	public void test() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Refiner ref = new Refiner(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setStartCommitId(target.name()).setOutputHunkGraph(true)
				.setEnableBuilderOutput(true)
				.setDaikonConfigPath(
						"/home/liyi/bit/gitref/src/test/resources/daikon/example-settings.txt")
				.setDaikonJarPath(
						"/home/liyi/pkg/daikon/daikon-5.2.26/daikon.jar")
				.setChivoryJarPath(
						"/home/liyi/pkg/daikon/daikon-5.2.26/java/ChicoryPremain.jar")
				.setDaikonIncludes("A,B").setEnableInvariant(false)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder)));

		PrintUtils.print(ref.changeDependencyPrettyPrint());

		ref.refineSlice(SCHEME.COMBINED);

		ref.cleanUp();

		StatsUtils.print();
	}

	@Test
	public void testChenguang() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Refiner ref = new Refiner(config

				.setRepositoryPath(
						"/home/polaris/Desktop/jacocoexample/maven/.git")
				.setEndCommitId("d745f8c4").setOutputHunkGraph(true)
				.setAnalysisLength(1).setEnableBuilderOutput(true)
				.setDaikonConfigPath(
						"/home/polaris/Desktop/123/example-settings.txt")
				.setDaikonJarPath(
						"/home/polaris/Desktop/daikonparent/daikon-5.2.22/daikon.jar")
				.setChivoryJarPath(
						"/home/polaris/Desktop/daikonparent/daikon-5.2.22/java/ChicoryPremain.jar")
				.setSubModuleBuildScriptPath(
						"/home/polaris/Desktop/jacocoexample/maven/maven-core/pom.xml")
				.setBuildScriptPath(
						"/home/polaris/Desktop/jacocoexample/maven/pom.xml")
				.setTestCases(new UnitTestScope(
						"DefaultMavenExecutionRequestPopulatorTest")));

		ref.initializeDaikon();

		ref.fJGit.checkOutVersion(ref.fHistory.get(0));

		ref.cleanUp();

		StatsUtils.print();
	}

	@Test
	public void testPaperExample() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Refiner ref = new Refiner(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setStartCommitId(target.name()).setOutputHunkGraph(true)
				.setEnableInvariant(true)
				.setDaikonConfigPath(
						"/home/polaris/Desktop/123/example-settings.txt")
				.setDaikonJarPath(
						"/home/polaris/Desktop/daikonparent/daikon-5.2.22/daikon.jar")
				.setChivoryJarPath(
						"/home/polaris/Desktop/daikonparent/daikon-5.2.22/java/ChicoryPremain.jar")
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder)));

		// PrintUtils.print(ref.changeDependencyToString());
		ref.refineSlice(SCHEME.COMBINED);
		ref.cleanUp();

		StatsUtils.print();
	}

	@Test
	public void testCommonsMath() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Refiner ref = new Refiner(config

				.setRepositoryPath(
						"/home/polaris/Desktop/jacocoexample/commons-math/.git")
				.setEndCommitId("6e4265d6").setOutputHunkGraph(true)
				.setAnalysisLength(50).setEnableBuilderOutput(true)
				.setEnableInvariant(true)
				.setDaikonConfigPath(
						"/home/polaris/Desktop/123/example-settings.txt")
				.setDaikonJarPath(
						"/home/polaris/Desktop/daikonparent/daikon-5.2.22/daikon.jar")
				.setChivoryJarPath(
						"/home/polaris/Desktop/daikonparent/daikon-5.2.22/java/ChicoryPremain.jar")
				.setBuildScriptPath(
						"/home/polaris/Desktop/jacocoexample/commons-math/pom.xml")
				.setExcludedPaths("pom.xml").setTestCases(new UnitTestScope(
						"ContinuousOutputFieldModelTest#testErrorConditions")));

		PrintUtils.print(ref.changeDependencyPrettyPrint());

		ref.refineSlice(SCHEME.COMBINED);

		ref.cleanUp();

		StatsUtils.print();
	}

	@Test
	public void testRegexPattern() {
		Pattern p = Pattern.compile(".*Test.*");
		Matcher m = p.matcher("org.apache.commons.math4.EEEEETest");
		boolean b = m.matches();
		if (b) {
			System.out.println("true");
		} else {
			System.out.println("false");
		}
	}

	@Test
	public void testCheckCompilation() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();
		config.setRepositoryPath(
				"/home/polaris/Desktop/jacocoexample/commons-io/.git")
				.setEndCommitId("0945c7e7").setOutputHunkGraph(true)
				.setAnalysisLength(50).setEnableBuilderOutput(true)
				.setBuildScriptPath(
						"/home/polaris/Desktop/jacocoexample/commons-io/pom.xml");

		CheckCompilation cc = new CheckCompilation(config);
		List<String> list = cc.check();
		for (String s : list) {
			PrintUtils.print(s);
		}
	}

	@Test
	public void testCommonsCollections() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Refiner ref = new Refiner(config

				.setRepositoryPath(
						"/home/polaris/Desktop/jacocoexample/commons-collections/.git")
				.setEndCommitId("90509ce8").setOutputHunkGraph(true)
				.setAnalysisLength(45).setEnableBuilderOutput(true)
				.setEnableInvariant(true)
				.setDaikonConfigPath(
						"/home/polaris/Desktop/123/example-settings.txt")
				.setDaikonJarPath(
						"/home/polaris/Desktop/daikonparent/daikon-5.2.22/daikon.jar")
				.setChivoryJarPath(
						"/home/polaris/Desktop/daikonparent/daikon-5.2.22/java/ChicoryPremain.jar")
				.setBuildScriptPath(
						"/home/polaris/Desktop/jacocoexample/commons-collections/pom.xml")
				.setExcludedPaths("pom.xml")
				.setTestCases(new UnitTestScope("IterableUtilsTest#indexOf")));

		PrintUtils.print(ref.changeDependencyPrettyPrint());

		ref.refineSlice(SCHEME.COMBINED);

		ref.cleanUp();

		StatsUtils.print();
	}
}
