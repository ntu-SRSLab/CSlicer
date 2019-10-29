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
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PrecisionTest2 {

	Map<String, Pair<String, String>> feature;
	Slicer ref;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration()
				.setBuildScriptPath("/home/liyi/bit/elasticsearch/pom.xml")
				.setRepositoryPath("/home/liyi/bit/elasticsearch/.git")
				.setStartCommitId("6a27ecf2")
				.setEndCommitId("82e0c5762cc560740a15fc18bbac085cd81a233d")
				.setEnableBuilderOutput(true)
				.setJacocoExecPath(
						// "/home/liyi/Dropbox/Ideas/dep/elastic-9511.exec")
						"/home/liyi/bit/elasticsearch/target/jacoco.exec")
				.setSourceRootPath("/home/liyi/bit/elasticsearch/src/main/java")
				.setClassRootPath("/home/liyi/bit/elasticsearch/target/classes")
				.setTouchSetPath("/tmp/precision2.txt");

		ref = new Slicer(config);
	}

	@Test
	public void testElasticMerge() throws Exception {
		List<RevCommit> hunk = ref.doSlicing(false, false).getPicks();
		// assertTrue(ref.verifyRefactorPicking(hunk));
	}
}
