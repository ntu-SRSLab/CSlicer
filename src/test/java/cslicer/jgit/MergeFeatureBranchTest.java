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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cslicer.utils.PrintUtils;

public class MergeFeatureBranchTest {

	JGit git;

	@Before
	public void setUp() throws Exception {
		git = new JGit("/Users/liyi/Documents/bit/hadoop/.git");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws CommitNotFoundException {
		RevCommit start = git
				.getCommit("45b90ba84266d31ed996d7bdb966f4fa77b4577e");
		RevCommit end265 = git
				.getCommit("1348c10ede8f73015f60255a7dfe7260e45029c9");
		RevCommit end1052 = git
				.getCommit("3a20f6c74c95007c7d0950ce6232a8381ce272ea");

		List<RevCommit> hdfs265 = git.getCommitList(start, end265, true);
		List<RevCommit> hdfs1052 = git.getCommitList(start, end1052, true);

		PrintUtils.print(hdfs265.size());
		PrintUtils.print(hdfs1052.size());

		ListIterator<RevCommit> i1 = hdfs265.listIterator();
		ListIterator<RevCommit> i2 = hdfs1052.listIterator();

		List<RevCommit> merge = new LinkedList<RevCommit>();
		while (i1.hasNext() || i2.hasNext()) {
			if (!i1.hasNext()) {
				merge.add(i2.next());
			} else if (!i2.hasNext()) {
				merge.add(i1.next());
			} else if (hdfs265.get(i1.nextIndex()).getCommitTime() <= hdfs1052
					.get(i2.nextIndex()).getCommitTime()) {
				merge.add(i1.next());
			} else {
				merge.add(i2.next());
			}
		}

		for (RevCommit c : merge) {
			PrintUtils.print(c.abbreviate(8).name() + ":" + c.getShortMessage());
		}
	}

}
