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

import java.util.LinkedList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.builder.UnitTestScope;

public class RemoveCommitsTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void test() throws Exception {
		Slicer ref = new Slicer(
				new ProjectConfiguration()
						.setRepositoryPath("/home/liyi/bit/hadoop/.git")
						.setBuildScriptPath("/home/liyi/bit/hadoop/pom.xml")
						.setStartCommitId(
								"7746a78ef80f64335370196761d90f474c3a8df8")
						.setEnableBuilderOutput(true)
						.setSubModuleBuildScriptPath(
								"/home/liyi/bit/hadoop/hadoop-hdfs-project/hadoop-hdfs/pom.xml")
						.setTestCases(new UnitTestScope().includeTest("TestLeaseRecovery")));

		List<String> drop = new LinkedList<String>();
		drop.add("1348c10e");
		drop.add("95a1f919");
		drop.add("83b9a2e2");
		drop.add("d4e40eef");
		drop.add("0c49b658");
		drop.add("def09e15");
		drop.add("f3a30a0d");
		drop.add("89ec1c2c");
		drop.add("66064f88");
		drop.add("c9966f79");
		drop.add("1462bcdd");

		ref.cleanUp();
		ref.verifyResultWithId(drop);
	}

}
