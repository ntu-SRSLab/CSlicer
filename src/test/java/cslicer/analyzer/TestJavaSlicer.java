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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.utils.StatsUtils;

public class TestJavaSlicer {

	private RevCommit target;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		// the example in gitref.pdf
		target = TestUtils.setupJavaSlicerRepo(tempFolder, BUILD_SYSTEM.MAVEN);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testFull() throws Exception {
		// PrintUtils.supressDebugMessages();
		ProjectConfiguration config = new ProjectConfiguration();

		Slicer ref = new Slicer(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setEnableBuilderOutput(false).setStartCommitId(target.name())
				.setOutputHunkGraph(true)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder)));

		List<RevCommit> drop = ref.doSlicing().getPicks();

		// verify the sliced results
		// assertEquals(1, ref.getSlicingReseult().getDropCount());
		assertTrue(ref.verifyResultTestPassing(drop));
		StatsUtils.print();
	}

	// @Ignore
	@Test
	public void testChecked() throws Exception {
		// PrintUtils.supressDebugMessages();

		String criteria = "TestJavaSlicer.test:45:{total}, Management.calculateSumSalary:12:{idealBonus}";

		ProjectConfiguration config = new ProjectConfiguration();

		Slicer ref = new Slicer(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setEnableBuilderOutput(true).setStartCommitId(target.name())
				.setOutputHunkGraph(true)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder))
				.setJavaSlicerDumpPath(
						"src/test/resources/javaslicer/test.trace, src/test/resources/javaslicer/test.trace")
				.setJavaSlicerCriteria(criteria));

		List<RevCommit> drop = ref.doSlicing().getPicks();

		// verify the sliced results
		// assertEquals(1, ref.getSlicingReseult().getDropCount());
		assertTrue(ref.verifyResultTestPassing(drop));
		StatsUtils.print();
	}

}
