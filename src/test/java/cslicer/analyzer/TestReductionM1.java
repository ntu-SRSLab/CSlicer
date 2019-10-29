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

// M1. [MNG-5755] Access toolchains without maven-toolchain-plugin
//
// JUnit test: maven-core/src/test/java/org/apache/maven/toolchain/DefaultToolchainManagerTest.java
// org.apache.maven.toolchain.DefaultToolchainManagerTest
// Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
// Build and test execution: 34.534s
// Target commit: 4b263106db043f5c1d7c314237ab2bc8708c0538

public class TestReductionM1 {

	private ProjectConfiguration config;

	@Before
	public void setup() {
		config = new ProjectConfiguration()
				.setBuildScriptPath("/home/liyi/bit/maven/maven-core/pom.xml")
				.setRepositoryPath("/home/liyi/bit/maven/.git")
				.setEndCommitId("4b263106db043f5c1d7c314237ab2bc8708c0538")
				.setJacocoExecPath(
						"/home/liyi/Dropbox/Ideas/dep/testdata/maven/m1/jacoco.exec")
				.setSourceRootPath(
						"/home/liyi/bit/maven/maven-core/src/main/java")
				.setClassRootPath(
						"/home/liyi/bit/maven/maven-core/target/classes")
				.setTouchSetPath("/tmp/touch-m1.txt");
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
