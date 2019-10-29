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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cslicer.TestUtils;
import cslicer.utils.PrintUtils;

public class CherryPickingTest {
	JGit repo;
	Map<String, Pair<String, String>> feature;
	Set<RevCommit> pool;
	List<RevCommit> testHistory;

	@Before
	public void setUp() throws Exception {
		repo = new JGit(
				"/Users/liyi/Documents/bit/commons-net/.git");
		// feature = TestUtils.readHadoop6581();
		// pool = new HashSet<RevCommit>();
		// for (String c : feature.keySet())
		// pool.add(repo.getCommit(c));

		testHistory = new ArrayList<RevCommit>();
		testHistory.add(
				repo.getCommit("529ae45a9740ea80df10035ed192e5920c715b32"));
		testHistory.add(
				repo.getCommit("bcba9364c60b6b24dc916b1f30af6912c73949e6"));
		testHistory.add(
				repo.getCommit("22c63cafa19859560bda1055440eb71008bdbf97"));
		testHistory.add(
				repo.getCommit("efc34a93b510eb7d736d35523f0d2b9aa41a5469"));
		testHistory.add(
				repo.getCommit("5e305c6a2c7d0e3d2dfc9c07a702167f55944373"));
		testHistory.add(
				repo.getCommit("0a25cf0181718402f85b3dae371e7c73e094caf9"));
		testHistory.add(
				repo.getCommit("86450a6f5d7a79021c6d9d5aa9cc84ef5209fdbe"));
		testHistory.add(
				repo.getCommit("9d090d40ef488c6c8e0784da877b93a05034e339"));
		testHistory.add(
				repo.getCommit("a02169b37f0a9075d04dac69be2f50a0700b12d2"));
		testHistory.add(
				repo.getCommit("476395d79f05ed4dee56d964d15aeba2928c6fe9"));
		testHistory.add(
				repo.getCommit("7879fd6518258118611af31d826bd0791adbb5c2"));
		testHistory.add(
				repo.getCommit("f2e08e8429dd35afbc522e72091f11e5c2731bac"));
		testHistory.add(
				repo.getCommit("ad3da80ff944adb1c611fb0c96bd7a1a34aa422e"));
		testHistory.add(
				repo.getCommit("9c352db423c3d0e823e998f4ba46e25e3d426099"));
		testHistory.add(
				repo.getCommit("e333ff8e0a4b9bc4747fcc084fa02d3e3aea6a54"));
		testHistory.add(
				repo.getCommit("1ab1a17fd37c377d96c8b41f41178ef1498c7fb5"));
		testHistory.add(
				repo.getCommit("717068898a67d03b7c2435e9f16bb65aced68048"));
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void pickCommitsTest() {
		repo.pickCommitsToBranch("TEST", testHistory, false,
				Collections.<String> emptySet());
	}

	@Ignore
	@Test
	public void pickFeatureAll() throws Exception {
		List<RevCommit> commits = getFeatureCommits(false);

		for (RevCommit c : commits) {
			PrintUtils.print(JGitUtils.summary(c));
		}
		PrintUtils.print("Linear history has " + commits.size() + " commits.");

		repo.checkOutNewBranch("feature", commits.get(0));
		assertTrue(repo.pickCommitsToBranch("feature", commits, true,
				Collections.<String> emptySet()));
	}

	@Ignore
	@Test
	public void pickFeatureRes() throws Exception {
		List<RevCommit> commits = getFeatureCommits(true);

		List<RevCommit> hunks = TestUtils.computeHunkDepSet(repo, commits,
				pool);
		Set<String> keys = new HashSet<String>();

		for (RevCommit h : hunks) {
			keys.add(h.name());
			PrintUtils.print(JGitUtils.summary(h));
		}

		for (String f : feature.keySet()) {
			if (!keys.contains(f))
				PrintUtils.print(f);
		}

		repo.checkOutNewBranch("feature-res", hunks.get(0));
		assertTrue(repo.pickCommitsToBranch("feature-res", hunks, true,
				Collections.<String> emptySet()));
	}

	private List<RevCommit> getFeatureCommits(boolean skipFF)
			throws CommitNotFoundException {
		List<RevCommit> commits = new LinkedList<RevCommit>();
		for (String c : feature.keySet()) {

			if (feature.get(c).getLeft().equals("F")
					|| feature.get(c).getLeft().equals("R")) {
				// PrintUtils.print(c);
				commits.add(repo.getCommit(c));
			}

			if (!skipFF && feature.get(c).getLeft().equals("M")) {
				RevCommit resolve = repo.getCommit(c);
				RevCommit base = JGitUtils.getMergeBase(repo.getRepo(),
						resolve.getParent(1), resolve.getParent(0));
				commits.addAll(
						repo.getCommitList(base, resolve.getParent(1), false));
			}
		}

		Collections.reverse(commits);
		return commits;
	}
}
