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

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cslicer.utils.StatsUtils;

// M2. [MNG-5568] fixed edge case version parsing bug causing inconsistent comparison results
//
// JUnit test: 
// org.apache.maven.artifact.versioning.ComparableVersionTest
// Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
// Build and test execution: 8.762s
// Target commit: 14e4885de9729c88715b4036a740733c0476e472

public class TestReductionM2 {

	private ProjectConfiguration config;

	@Before
	public void setup() {
		config = new ProjectConfiguration()
				.setBuildScriptPath(
						"/home/liyi/bit/maven/maven-artifact/pom.xml")
				.setRepositoryPath("/home/liyi/bit/maven/.git")
				.setEndCommitId("14e4885de9729c88715b4036a740733c0476e472")
				.setJacocoExecPath(
						"/home/liyi/Dropbox/Ideas/dep/testdata/maven/m2/jacoco.exec")
				.setSourceRootPath(
						"/home/liyi/bit/maven/maven-artifact/src/main/java")
				.setClassRootPath(
						"/home/liyi/bit/maven/maven-artifact/target/classes")
				.setTouchSetPath("/tmp/touch-m2.txt");
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
