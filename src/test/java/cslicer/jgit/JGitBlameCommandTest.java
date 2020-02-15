package cslicer.jgit;

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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.ProjectConfiguration;
import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.analyzer.Slicer;
import cslicer.builder.UnitTestScope;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;

public class JGitBlameCommandTest {

	private RevCommit target;
	JGit jgit;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		target = TestUtils.setupLongTestRepo(tempFolder, BUILD_SYSTEM.MAVEN);
		jgit = new JGit(TestUtils.getStandardRepoPath(tempFolder));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testHadoopBlame() throws Exception {
		JGit repo = new JGit("/home/liyi/bit/hadoop/.git");
		Set<String> pool = TestUtils.readHadoop6581().keySet();

		Set<RevCommit> deps = repo.findHunkDependencies(
				"b1000fbba43786a8d1da129bc7c7a1bf253a9e7e", pool);

		for (RevCommit d : deps) {
			PrintUtils.print(d.abbreviate(8).name() + " : "
					+ d.getShortMessage());
		}
	}

	@Test
	public void testHadoopBlame2() throws Exception {
		JGit repo = new JGit("/home/liyi/bit/hadoop/.git");
		// Set<String> pool = TestUtils.readHadoop6581().keySet();

		Set<RevCommit> deps = repo.findHunkDependencies(
				"0e74cd6574d25afb9bd08c839cad054fef3bf35c", null);

		for (RevCommit d : deps) {
			PrintUtils.print(d.abbreviate(8).name() + " : "
					+ d.getShortMessage());
		}
	}

	@Ignore
	@Test
	public void testBlame() throws Exception {
		JGit repo = new JGit("/home/liyi/bit/testgit/.git");
		Set<RevCommit> deps = repo.findHunkDependencies(
				"e55b80c7a3c8b8f397f213f266a822945b52c8e9", null);

		for (RevCommit d : deps) {
			PrintUtils.print(d.abbreviate(8).name() + " : "
					+ d.getShortMessage());
		}
	}

	@Ignore
	@Test
	public void test() throws Exception {
		RevCommit head = jgit.getCurrentHead();

		Set<RevCommit> deps = jgit.findHunkDependencies(
				head,
				new HashSet<RevCommit>(jgit
						.getCommitListOnBranch(target, false)));

		for (RevCommit d : deps) {
			PrintUtils.print(d.getName() + " : " + d.getShortMessage());
		}

		assertEquals(3, deps.size());
	}

	@Ignore
	@Test
	public void testGitRefHunkDeps() throws Exception {
		Slicer ref = new Slicer(new ProjectConfiguration()
				.setRepositoryPath("/home/liyi/bit/testgit/.git")
				.setStartCommitId("b95b1946740156280d0ef7cfc3cb0cebe36bbeda")
				.setBuildScriptPath("/home/liyi/bit/testgit/pom.xml")
				.setProjectJDKVersion("1.7")
				.setTestCases(
						new UnitTestScope().includeTest("JGitLogCommandTest",
								"ambiguousEndPointTest"))
				.setEnableBuilderOutput(false));

		Set<String> cSet = new HashSet<String>();
		cSet.add("39cc7124656fafdc1bcfdc2d4e304e52d9e3f1f0");
		cSet.add("7fd3379da3a3beb6e1cfb6063ed34ba2127325fb");
		cSet.add("c248e06777f6ac8e39aeaf6ccfd044ff6a43794d");
		cSet.add("60397630b659c7392f7d291c302fc6c666ae826f");

		ref.cleanUp();
		List<RevCommit> deps = ref.computeHunkDepSetWithId(cSet);

		for (RevCommit d : deps)
			PrintUtils.print(d.abbreviate(8).name() + " : "
					+ d.getShortMessage());
	}

	@Ignore
	@Test
	public void testComputeHunkDeps() throws Exception {
		Slicer ref = new Slicer(
				new ProjectConfiguration()
						.setRepositoryPath("/home/liyi/bit/hadoop/.git")
						.setBuildScriptPath("/home/liyi/bit/hadoop/pom.xml")
						.setStartCommitId(
								"7746a78ef80f64335370196761d90f474c3a8df8")
						.setEnableBuilderOutput(true)
						.setSubModuleBuildScriptPath(
								"/home/liyi/bit/hadoop/hadoop-hdfs-project/hadoop-hdfs/pom.xml")
						.setTestCases(
								new UnitTestScope()
										.includeTest("TestLeaseRecovery")));

		Set<String> cSet = new HashSet<String>();
		cSet.add("cc07fe635f3ba5d7abf7c8f1953fc46ea2f19dc5");
		cSet.add("89da84364ec751ff4afbeccfa6763f53b4c08a14");
		cSet.add("b7906a0d78360d6c2fa654489fb5fbca18ca2b18");
		cSet.add("53ec15530122df746fa7c0d654adb1d8ca9bf3bb");
		cSet.add("63e473aa27ae53737ff5546689fcfabc23713942");
		cSet.add("060ee0096552e46d1c7774f4d566615f76899170");
		cSet.add("860c65888594737516516dc0488b7dd170243396");
		cSet.add("bee153af281413662ee0c983e85f84251a8edc19");
		cSet.add("c33374507739c9caedc87f9f2d6e47a6c9d9baf8");
		cSet.add("567ebb43baf5700e535914fefd3a0a45533511c7");
		cSet.add("c1a98ff71f58dc3d3aa2c6b51afe353cd508b24d");
		cSet.add("5317558fd50c3002fe3507e86a6d05f679444908");
		cSet.add("ceb1c08a784b22fd0b1eb25cc303458b37de74a9");
		cSet.add("9e146f69a97696ccfd68b465758d72856f5b103b");
		cSet.add("af8338958a5401dfc1790c0e093b0a9de0607d8c");
		cSet.add("f290fa4705e42de53d74560f7e946cdf9a694dae");
		cSet.add("2626fd61e83ea48f64c048e4bb2bdd3268c279b7");
		cSet.add("f3f7ad73f9b28d2babf9c59e54c5d273d3dc00d6");
		cSet.add("0809d3a98bab1499c4f0c9c4b12c0c2781947e66");
		cSet.add("9b4c9b09d8c627fcbbe6992a613f9526fbb7a586");
		cSet.add("cb434269c4d216723865dbf12ae8cc6ef88adca7");
		cSet.add("e7f903ea4d2fca17e4ddc77153f2e32d2fdf72f9");
		cSet.add("fe729c7b09dd1d0a1b8ad1a894ce2884d07c72e2");
		cSet.add("163e6dca7d6bbbd57088ae3e5be4b002bad8edfd");
		cSet.add("f9440003c76a971f0e26d4b974234bc3ed409a71");
		cSet.add("8cfd45906682b7a2a0e3cd73e9e852b2bceaa498");
		cSet.add("eee1658db697b2f18a6a1be1c655c5aa0760394d");
		cSet.add("84f69ca6bb20bcb745e0aa1f6e83b97e91a1aef8");
		cSet.add("ac44d450c5ae7a7c022854f450087865a13b7aa4");
		cSet.add("99a2267e7b50b7bc15441d91f6f38a2edef20a82");
		cSet.add("e5f1df8aa30c2ec51a2ef379b91b6158c470872f");
		cSet.add("f27adbb4c64d6c2c072d033e69cabd0d7b5a1fec");
		cSet.add("825165d09a7c2f111cc215e02f892c3b5add4bf1");
		cSet.add("4171c28379dc6a5970c6c8a566cc33555e4760f1");
		cSet.add("97b0a474b4d785eb1f1a68e5d1a463e2636437af");
		cSet.add("d81962810c171d2449f516f80d69ea9fbeb0fcea");
		cSet.add("0101a69538db2609de3d0140c7cf979355412801");
		cSet.add("c4b7ee0dcfe513982f6ae21e5c577f940246c0f6");
		cSet.add("637a38a43dac2cc246a3e002b27bcc72f99a321a");
		cSet.add("06c0662c7fb733893bfd3cb4905c75502d40662a");
		cSet.add("191e0c47e62a81353a9b098939340a21c36dab86");
		cSet.add("8ca952015ee4dc495ff2c58ab6cc12f1e339bcb3");
		cSet.add("1e9c6058fae3509408c7f483a2f2cdd8432e90cf");

		ref.cleanUp();
		List<RevCommit> deps = ref.computeHunkDepSetWithId(cSet);

		for (RevCommit d : deps)
			PrintUtils.print(d.abbreviate(8).name() + " : "
					+ d.getShortMessage());
	}

	@Ignore
	@Test
	public void test2() throws Exception {
		JGit repo = new JGit("/Users/liyi/Documents/bit/gitref/.git");
		RevCommit head = repo.getCommit("702a0146");

		Set<RevCommit> deps = repo.findHunkDependencies(head, null, 3, false,
				Collections.<String> emptySet());

		for (RevCommit d : deps) {
			PrintUtils.print(d.getName() + " : " + d.getShortMessage());
		}

		StatsUtils.print();
	}
}
