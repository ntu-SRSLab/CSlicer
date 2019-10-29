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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;
import cslicer.analyzer.SlicingResult.DEP_FLAG;
import cslicer.builder.UnitTestScope;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;

public class PrecisionTest {

	Map<String, Pair<String, String>> feature;
	Slicer ref;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		feature = TestUtils.readHadoop6581();

		ProjectConfiguration config = new ProjectConfiguration()
				.setBuildScriptPath("/home/liyi/bit/hadoop/pom.xml")
				.setRepositoryPath("/home/liyi/bit/hadoop/.git")
				.setStartCommitId("37549576e7aca2fe3d0fe03ea2e82aeb953bca44")
				.setEndCommitId("e750d196cac5580782bab2f8b276ad5f446b4cbe")
				.setSubModuleBuildScriptPath(
						"/home/liyi/bit/hadoop/hadoop-hdfs-project/hadoop-hdfs/pom.xml")
				.setEnableBuilderOutput(true)
				.setJacocoExecPath(
						"/home/liyi/Dropbox/Ideas/dep/jacoco-hdfs6581.exec")
				// "/home/liyi/bit/hadoop/hadoop-hdfs-project/hadoop-hdfs/target/jacoco.exec")
				.setSourceRootPath(
						"/home/liyi/bit/hadoop/hadoop-hdfs-project/hadoop-hdfs/src/main/java")
				.setClassRootPath(
						"/home/liyi/bit/hadoop/hadoop-hdfs-project/hadoop-hdfs/target/classes")
				.setTestCases(
						new UnitTestScope().includeTest("TestLazyPersistFiles"))
				.setExcludedPaths(
						"CHANGES.txt,hadoop-common-project/hadoop-common/CHANGES.txt,hadoop-hdfs-project/hadoop-hdfs/CHANGES.txt,hadoop-hdfs-project/hadoop-hdfs/CHANGES-HDFS-6581.txt,hadoop-mapreduce-project/CHANGES.txt,hadoop-yarn-project/CHANGES.txt");

		ref = new Slicer(config);
	}

	@Test
	public void testMotive() {

	}

	@Ignore
	@Test
	public void testBaseline() throws Exception {
		// PrintUtils.supressDebugMessages();
		Set<String> featureSet = new HashSet<String>();
		for (String k : feature.keySet()) {
			if (feature.get(k).getLeft().equals("F"))
				featureSet.add(k);
		}
		List<RevCommit> hunk = ref.computeHunkDepSetWithId(featureSet);
		PrintUtils.print(hunk.size());
	}

	@Ignore
	@Test
	public void testHDFS6581() throws Exception {
		List<RevCommit> hunk = ref.doSlicing(false, false).getDrops();
		// assertTrue(ref.verifyRefactorPicking(hunk));

		SlicingResult res = ref.getSlicingReseult();
		int falsePos = 0, falseNeg = 0, truePos = 0, trueNeg = 0;

		for (Pair<RevCommit, DEP_FLAG> pair : res.getLabeledHistory()) {
			RevCommit c = pair.getLeft();
			DEP_FLAG f = pair.getRight();

			Set<String> featureSet = readFeatureBranch();

			String info = res.getLabel(c) + " : " + c.getName() + " : "
					+ c.getShortMessage();

			if (f == DEP_FLAG.TEST || f == DEP_FLAG.COMP) {
				if (featureSet.contains(c.getName())) {
					truePos++;
					PrintUtils.print("TP: " + info);
				} else {
					falsePos++;
					PrintUtils.print("FP: " + info);
				}
			} else {
				if (featureSet.contains(c.getName())) {
					falseNeg++;
					PrintUtils.print("FN: " + info);
				} else {
					trueNeg++;
					PrintUtils.print("TN: " + info);
				}
			}
		}

		PrintUtils.print(String.format("TP: %d, FP: %d, FN: %d, TN: %d",
				truePos, falsePos, falseNeg, trueNeg));
		StatsUtils.print();
	}

	private Set<String> readFeatureBranch() throws IOException {
		String fileContents = IOUtils.toString(TestUtils.class
				.getResourceAsStream("/precision/feature-baseline"));
		Set<String> res = new HashSet<String>();

		for (String line : StringUtils.split(fileContents,
				System.lineSeparator())) {
			res.add(line.trim());
		}

		return res;
	}
}
