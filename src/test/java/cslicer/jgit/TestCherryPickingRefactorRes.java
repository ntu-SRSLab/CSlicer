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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cslicer.TestUtils;
import cslicer.utils.PrintUtils;

public class TestCherryPickingRefactorRes {

	JGit repo;
	Set<RevCommit> pool;
	List<RevCommit> feature;

	@Before
	public void setUp() throws Exception {
		// repo = new JGit("/home/liyi/bit/hadoop/.git");
		repo = new JGit("/home/liyi/bit/elasticsearch/.git");
		pool = new HashSet<RevCommit>();
		feature = new LinkedList<RevCommit>();

		// String fileContents = IOUtils.toString(TestUtils.class
		// .getResourceAsStream("/precision/ref_res_no_test"));
		String fileContents = IOUtils.toString(TestUtils.class
				.getResourceAsStream("/precision/groovy_res"));

		for (String line : StringUtils.split(fileContents,
				System.lineSeparator())) {
			String[] items = StringUtils.split(line, ":");
			pool.add(repo.getCommit(items[1].trim()));

			if (items[0].trim().equals("HUNK")
					|| items[0].trim().equals("TEST")
					|| items[0].trim().equals("COMP")) {
				feature.add(repo.getCommit(items[1].trim()));
			}
		}
	}

	@Test
	public void testMerge() throws Exception {

		repo.checkOutNewBranch("groovy-res", feature.get(0));
		assertTrue(repo.pickCommitsToBranch("groovy-res", feature, true,
				Collections.<String> emptySet()));
	}

	// construct the human results
	@Ignore
	@Test
	public void testHuman() throws Exception {
		String fileContents = IOUtils.toString(TestUtils.class
				.getResourceAsStream("/precision/feature-baseline"));

		List<RevCommit> commits = new LinkedList<RevCommit>();

		for (String line : StringUtils.split(fileContents,
				System.lineSeparator())) {
			commits.add(repo.getCommit(line.trim()));
		}
		// Collections.reverse(commits);

		List<RevCommit> hunks = TestUtils
				.computeHunkDepSet(repo, commits, pool);

		repo.checkOutNewBranch("human-base", hunks.get(0));
		assertTrue(repo
				.pickCommitsToBranch(
						"human-base",
						hunks,
						true,
						new HashSet<String>(
								Arrays.asList(
										"CHANGES.txt",
										"hadoop-common-project/hadoop-common/CHANGES.txt",
										"hadoop-hdfs-project/hadoop-hdfs/CHANGES.txt",
										"hadoop-hdfs-project/hadoop-hdfs/CHANGES-HDFS-6581.txt",
										"hadoop-mapreduce-project/CHANGES.txt",
										"hadoop-yarn-project/CHANGES.txt"))));
	}

	// construct our results
	@Ignore
	@Test
	public void test() throws Exception {

		repo.checkOutNewBranch("feature-ref-4", feature.get(0));
		assertTrue(repo
				.pickCommitsToBranch(
						"feature-ref-4",
						feature,
						true,
						new HashSet<String>(
								Arrays.asList(
										"CHANGES.txt",
										"hadoop-common-project/hadoop-common/CHANGES.txt",
										"hadoop-hdfs-project/hadoop-hdfs/CHANGES.txt",
										"hadoop-hdfs-project/hadoop-hdfs/CHANGES-HDFS-6581.txt",
										"hadoop-mapreduce-project/CHANGES.txt",
										"hadoop-yarn-project/CHANGES.txt"))));
	}

	@Ignore
	@Test
	public void testAccuracy() throws Exception {
		String fileContents = IOUtils.toString(TestUtils.class
				.getResourceAsStream("/precision/feature-baseline"));

		List<RevCommit> commits = new LinkedList<RevCommit>();

		for (String line : StringUtils.split(fileContents,
				System.lineSeparator())) {
			commits.add(repo.getCommit(line.trim()));
		}

		List<RevCommit> hunks = TestUtils
				.computeHunkDepSet(repo, commits, pool);

		int falsePos = 0, falseNeg = 0, truePos = 0, trueNeg = 0;

		for (RevCommit c : pool) {
			String info = c.getName() + " : " + c.getShortMessage();

			if (feature.contains(c) && hunks.contains(c)) {
				PrintUtils.print("TP: " + info);
				truePos++;
			} else if (!feature.contains(c) && !hunks.contains(c)) {
				PrintUtils.print("TN: " + info);
				trueNeg++;
			} else if (feature.contains(c) && !hunks.contains(c)) {
				PrintUtils.print("FN: " + info);
				falseNeg++;
			} else if (!feature.contains(c) && hunks.contains(c)) {
				PrintUtils.print("FP: " + info);
				falsePos++;
			}
		}

		PrintUtils.print(String.format("TP: %d, FP: %d, FN: %d, TN: %d",
				truePos, falsePos, falseNeg, trueNeg));
	}
}
