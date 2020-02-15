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

import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.builder.maven.MavenInvoker;
import cslicer.builder.maven.MavenInvokerJacoco;

public class TestSootCoverage {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		// the example in gitref.pdf
		TestUtils.setupJavaSlicerRepo(tempFolder, BUILD_SYSTEM.MAVEN);
		MavenInvoker invoker = new MavenInvokerJacoco(
				TestUtils.getBuildScriptPath(tempFolder));
		invoker.compileTests();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		String testClassPath = FileUtils
				.getFile(tempFolder.getRoot(), "target", "test-classes")
				.getAbsolutePath();
		String classPath = FileUtils
				.getFile(tempFolder.getRoot(), "target", "classes")
				.getAbsolutePath();
		String junitPath = "/home/liyi/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/liyi/.m2/repository/org/hamcrest/hamcrest-all/1.3/hamcrest-all-1.3.jar";
		SootCoverageAnalyzer analyzer = new SootCoverageAnalyzer(
				classPath + ":" + testClassPath + ":" + junitPath,
				Collections.singleton("TestJavaSlicer"), null);
		analyzer.analyseCoverage();
	}

}
