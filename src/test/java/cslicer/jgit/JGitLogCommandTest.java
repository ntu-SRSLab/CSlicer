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

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

public class JGitLogCommandTest {

	private RevCommit target;
	JGit jgit;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		target = TestUtils.setupTwoBranchRepo(tempFolder);
		jgit = new JGit(TestUtils.getStandardRepoPath(tempFolder));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = AmbiguousEndPointException.class)
	public void ambiguousEndPointTest() throws Exception {
		PrintUtils.print("Current Branch: " + jgit.getCurrentBranchName(),
				TAG.OUTPUT);
		jgit.getCommitListOnBranch(target, false);
	}

	@Test
	public void getCommitListTest() throws Exception {
		RevCommit featureHead = jgit.getCommit(jgit
				.getBranch("refs/heads/feature").getObjectId().name());
		List<RevCommit> commits = jgit.getCommitList(target, featureHead, true);
		for (RevCommit c : commits)
			PrintUtils.print(c.getFullMessage(), TAG.OUTPUT);
		assertEquals(2, commits.size());
		assertEquals("C1", commits.get(0).getFullMessage());
		assertEquals("C2", commits.get(1).getFullMessage());

		commits = jgit.getCommitList(target, jgit.getCurrentHead(), true);
		for (RevCommit c : commits)
			PrintUtils.print(c.getFullMessage(), TAG.OUTPUT);
		assertEquals(2, commits.size());
		assertEquals("C3", commits.get(0).getFullMessage());
		assertEquals("C4", commits.get(1).getFullMessage());
	}

	@Test
	public void getCommitListTestLen() throws Exception {
		RevCommit featureHead = jgit.getCommit(jgit
				.getBranch("refs/heads/feature").getObjectId().name());
		List<RevCommit> commits = jgit.getCommitList(featureHead, 2, true);
		for (RevCommit c : commits)
			PrintUtils.print(c.getFullMessage(), TAG.OUTPUT);
		assertEquals(2, commits.size());
		assertEquals("C1", commits.get(0).getFullMessage());
		assertEquals("C2", commits.get(1).getFullMessage());
	}
}
