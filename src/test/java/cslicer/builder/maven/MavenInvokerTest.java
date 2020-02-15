package cslicer.builder.maven;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.builder.UnitTestScope;
import cslicer.coverage.CoverageDatabase;
import cslicer.coverage.FullCoverageAnalyzer;
import cslicer.utils.PrintUtils;

/**
 * Test Maven invokation through {@link MavenInvoker} and coverage analysis
 * using {@link FullCoverageAnalyzer}.
 * 
 * @author Yi Li
 * @see {@link MavenInvoker}, {@link FullCoverageAnalyzer}
 */
public class MavenInvokerTest {

	private MavenInvoker invoker;
	private String newPomPath;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		TestUtils.addHierachyTestSuite(tempFolder, BUILD_SYSTEM.MAVEN);
		newPomPath = TestUtils.getBuildScriptPath(tempFolder);
		invoker = new MavenInvokerJacoco(newPomPath, false);
	}

	@After
	public void tearDown() throws Exception {
		invoker.cleanUp();
	}

	@Ignore
	@Test
	public void testCoverage() throws Exception {
		invoker.initializeBuild(tempFolder.newFolder());
		FullCoverageAnalyzer checker = new FullCoverageAnalyzer(invoker);
		CoverageDatabase coverage = checker.analyseCoverage();

		// assertTrue(coverage.isMethodCovered("Main.foo()"));
	}

	@Test
	public void testSingleTest() throws Exception {
		invoker.initializeBuild(tempFolder.newFolder());
		FullCoverageAnalyzer checker = new FullCoverageAnalyzer(invoker);
		CoverageDatabase coverage = checker.analyseCoverage(
				new UnitTestScope().includeTest("MainTest", "test1"));

		// assertTrue(coverage.getFullyCoveredEntities().contains(("Main.foo()",
		// false));
	}

	@Test
	public void testPrintClassPath() throws Exception {
		PrintUtils.print(invoker.getTestClassPath());
		PrintUtils.print(invoker.getCompileClassPath());
	}
}
