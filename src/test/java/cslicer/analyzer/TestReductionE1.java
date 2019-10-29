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

// E1. Groovy sandbox script.
//
// JUnit test: org.elasticsearch.script.GroovySandboxScriptTests#testDynamicBlacklist
// Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
// Build and test execution: 34.84 s
// Target commit: 647327f4
// Start commit (short): 6982b822
// Start commit (medium): b5cc50d5
// Start commit (long): 33725989

public class TestReductionE1 {

	private ProjectConfiguration config;

	@Before
	public void setup() {
		config = new ProjectConfiguration()
				.setBuildScriptPath("/home/liyi/bit/elasticsearch/pom.xml")
				.setRepositoryPath("/home/liyi/bit/elasticsearch/.git")
				.setEndCommitId("647327f4")
				.setJacocoExecPath(
						"/home/liyi/Dropbox/Ideas/dep/testdata/elastic/e1/jacoco.exec")
				.setSourceRootPath(
						"/home/liyi/bit/elasticsearch/core/src/main/java")
				.setClassRootPath("/home/liyi/bit/elasticsearch/target/classes")
				.setTouchSetPath("/tmp/touch-e1.txt");
	}

	@Ignore
	@Test
	public void testShort() throws Exception {
		config.setStartCommitId("6982b82252e77cce3e0c803ff3231198d3b8f2cf");
		Slicer ref = new Slicer(config);
		ref.doSlicing();
		StatsUtils.print();
	}

	@Ignore
	@Test
	public void testMedium() throws Exception {
		config.setStartCommitId("b5cc50d575a53b27fee9aad496b7b9f10f36cfda");
		Slicer ref = new Slicer(config);
		ref.doSlicing();
		StatsUtils.print();
	}

	// @Ignore
	@Test
	public void testLong() throws Exception {
		config.setStartCommitId("33725989a10e987c11d0b22105f23f1ffaa19430");
		Slicer ref = new Slicer(config);
		List<RevCommit> drop = ref.doSlicing().getPicks();
		StatsUtils.print();

		ref.verifyResultPicking(drop);
	}
}
