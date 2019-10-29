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

import java.util.Collections;
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
import cslicer.builder.UnitTestScope;
import cslicer.utils.StatsUtils;

public class ChangeSignificanceTest {

	private RevCommit target;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		target = TestUtils.setupShortTestRepo(tempFolder, BUILD_SYSTEM.MAVEN);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testSigCompute() throws Exception {
		ProjectConfiguration config = new ProjectConfiguration();

		Slicer ref = new Slicer(config
				.setRepositoryPath(TestUtils.getStandardRepoPath(tempFolder))
				.setStartCommitId(target.name()).setEnableBuilderOutput(false)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder)));

		List<RevCommit> drop = ref.doSlicing().getDrops();

		assertTrue(ref.verifyResultTestPassing(drop));
	}

	@Ignore
	@Test
	public void testGitRefRepo() throws Exception {
		Slicer ref = new Slicer(
				new ProjectConfiguration()
						.setRepositoryPath("/home/liyi/bit/testgit/.git")
						// .setTargetCommitId("b95b1946740156280d0ef7cfc3cb0cebe36bbeda")
						.setStartCommitId(
								"01926b41edc1ddd3fe1f8cffbda4683e1261b5fb")
						.setBuildScriptPath("/home/liyi/bit/testgit/pom.xml")
						.setProjectJDKVersion("1.7")
						.setTestCases(
								// new
								// UnitTestScope().includeTest("DirectoryDistillTest",
								// "testDiff"))
								new UnitTestScope().includeTest(
										"RemoveCommitsTest", "test"))
				// .setJacocoExecPath("/Users/liyi/Documents/bit/testgit/target/jacoco.exec")
				// .setSourceRootPath("/Users/liyi/Documents/bit/testgit/src/main/java")
				// .setClassRootPath("/Users/liyi/Documents/bit/testgit/target/classes")
				.setEnableBuilderOutput(true));

		List<RevCommit> drop = ref.doSlicing(false, false).getDrops();
		// assertTrue(ref.verifyRefactorPicking(drop));
		// assertTrue(ref.verifyRefactorTestResult(drop));
		// ref.undoRemoveCommit(true);
		StatsUtils.print();
	}

	@Ignore
	@Test
	public void testGitRefChosen() throws Exception {
		Slicer ref = new Slicer(new ProjectConfiguration()
				.setRepositoryPath("/Users/liyi/Documents/bit/testgit/.git")
				// .setTargetCommitId("b95b1946740156280d0ef7cfc3cb0cebe36bbeda")
				.setStartCommitId("17ee38a72de0b5f2b73c43c676508b8c73d88066")
				.setBuildScriptPath("/Users/liyi/Documents/bit/testgit/pom.xml")
				.setProjectJDKVersion("1.7")
				.setTestCases(new UnitTestScope()
						.includeTest("DirectoryDistillTest", "testDiff"))
				.setJacocoExecPath(
						"/Users/liyi/Documents/bit/testgit/target/jacoco.exec")
				.setSourceRootPath(
						"/Users/liyi/Documents/bit/testgit/src/main/java")
				.setClassRootPath(
						"/Users/liyi/Documents/bit/testgit/target/classes")
				.setEnableBuilderOutput(true));

		List<RevCommit> drop = ref.doSlicing(Collections
				.singleton("7b140f0cd1296278da009d8586aecc8bf7ea6388"));
		assertTrue(ref.verifyResultTestPassing(drop));
		ref.undoRemoveCommit(true);
		StatsUtils.print();
	}

	@Ignore
	@Test
	public void testMavenCoreRepo() throws Exception {
		Slicer ref = new Slicer(
				new ProjectConfiguration()
						.setRepositoryPath("/home/liyi/bit/maven/.git")
						.setStartCommitId(
								"3729324a13424c553ad3d143dfde771a35c5b53b")
				.setBuildScriptPath("/home/liyi/bit/maven/maven-core/pom.xml")
				.setTestCases(new UnitTestScope()
						.includeTest("MavenProjectTest", "testClone"))
				.setEnableBuilderOutput(true));

		List<RevCommit> drop = ref.doSlicing().getDrops();
		// assertTrue(ref.verifyRefactorTestResult(drop));
		StatsUtils.print();
	}

	@Ignore
	@Test
	public void testHadoopHDFS() throws Exception {
		Slicer ref = new Slicer(new ProjectConfiguration()
				.setRepositoryPath("/Users/liyi/Documents/bit/hadoop/.git")
				.setStartCommitId("9e81be01144d5cf520313608e85cdc1d8063aa15")
				.setBuildScriptPath("/Users/liyi/Documents/bit/hadoop/pom.xml")
				.setSubModuleBuildScriptPath(
						"/Users/liyi/Documents/bit/hadoop/hadoop-hdfs-project/hadoop-hdfs/pom.xml")
				.setEnableBuilderOutput(true)
				.setTestCases(new UnitTestScope().includeTest(
						"TestDecommissioningStatus",
						"testDecommissionDeadDN")));

		List<RevCommit> drop = ref.doSlicing().getDrops();
		assertTrue(ref.verifyResultTestPassing(drop));

		ref.undoRemoveCommit(true);
		StatsUtils.print();
	}

	@Ignore
	@Test
	public void testHadoopArchive() throws Exception {
		Slicer ref = new Slicer(
				new ProjectConfiguration()
						.setRepositoryPath("/home/liyi/bit/hadoop/.git")
						.setStartCommitId(
								"bcaf15e2fa94db929b8cd11ed7c07085161bf950")
				.setBuildScriptPath("/home/liyi/bit/hadoop/pom.xml")
				.setSubModuleBuildScriptPath(
						"/home/liyi/bit/hadoop/hadoop-mapreduce-project/hadoop-mapreduce-client/pom.xml")
				.setEnableBuilderOutput(true)
				.setTestCases(new UnitTestScope().includeTest("TestCounters",
						"testResetOnDeserialize")));

		List<RevCommit> drop = ref.doSlicing().getDrops();
		assertTrue(ref.verifyResultTestPassing(drop));
	}

	@Ignore
	@Test
	public void testHadoopAppend() throws Exception {
		Slicer ref = new Slicer(
				new ProjectConfiguration()
						.setRepositoryPath("/home/liyi/bit/hadoop/.git")
						.setBuildScriptPath("/home/liyi/bit/hadoop/build.xml")
						.setStartCommitId(
								"5883451ba25dee346d2b308712edd96df1609bd2")
				.setEnableBuilderOutput(false)
				.setSubModuleBuildScriptPath(
						"/home/liyi/bit/hadoop/hadoop-hdfs-project/hadoop-hdfs/pom.xml")
				.setJacocoExecPath("/home/liyi/bit/hadoop/jacoco.exec")
				.setSourceRootPath("/home/liyi/bit/hadoop/src/java")
				.setClassRootPath("/home/liyi/bit/hadoop/build/classes")
				.setTestCases(
						new UnitTestScope().includeTest("TestLeaseRecovery")));

		List<RevCommit> drop = ref.doSlicing().getDrops();
		assertTrue(ref.verifyResultPicking(drop));

		// ref.undoRemoveCommit(true);
		StatsUtils.print();
	}

	@Ignore("Ant test is not ready!")
	@Test
	public void testAntRepo() throws Exception {
		Slicer ref = new Slicer(
				new ProjectConfiguration()
						.setRepositoryPath("/Users/liyi/Documents/bit/ant/.git")
						.setStartCommitId(
								"4965535328781e63aacbf211d2e96a8462ea133a")
				.setBuildScriptPath("/Users/liyi/Documents/bit/ant/build.txt")
				.setBuildSystem(BUILD_SYSTEM.ANT));

	}
}
