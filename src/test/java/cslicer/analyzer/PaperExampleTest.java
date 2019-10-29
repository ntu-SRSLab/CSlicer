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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.utils.StatsUtils;

public class PaperExampleTest {

	private RevCommit target;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		// the example in gitref.pdf
		target = TestUtils.setupLongTestRepo(tempFolder, BUILD_SYSTEM.MAVEN);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		// PrintUtils.supressDebugMessages();

		ProjectConfiguration config = new ProjectConfiguration();

		Slicer ref = new Slicer(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setStartCommitId(target.name()).setOutputHunkGraph(false)
				.setEnableBuilderOutput(true)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder)));

		List<RevCommit> drop = ref.doSlicing().getPicks();

		// verify the sliced results
		assertEquals(1, ref.getSlicingReseult().getDropCount());
		assertTrue(ref.verifyResultTestPassing(drop));
		// ref.shortenSlice(ref.getSlicingReseult());

		StatsUtils.print();
	}

	@Test
	public void testHunk() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Hunker ref = new Hunker(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setStartCommitId(target.name()).setOutputHunkGraph(true)
				.setAnalysisLength(5));

		System.out.println(ref.generateHunkDependencyFacts());
	}

}
