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

import java.io.File;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.utils.PrintUtils;

public class MergeHunkDetectionTest {
	Git repo;
	RevCommit target;
	RevCommit feature;
	RevCommit master;
	RevCommit merger;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		repo = Git.init().setDirectory(tempFolder.getRoot()).call();
		File a = tempFolder.newFile("a.txt");

		FileUtils.writeStringToFile(a, "1\n2\n3\n4\n5\n6\n7\n");
		repo.add().addFilepattern("a.txt").call();
		repo.commit().setMessage("Initial").call();

		FileUtils.writeStringToFile(a, "1\n2\n3\n4\n5\n6\n7\n9");
		repo.add().addFilepattern("a.txt").call();
		repo.commit().setMessage("base-1").call();

		FileUtils.writeStringToFile(a, "1\n2\n3\n4\n5\n6\n7\n8");
		repo.add().addFilepattern("a.txt").call();
		repo.commit().setMessage("base").call();

		repo.branchCreate().setName("feature").call();
		repo.checkout().setName("feature").call();
		FileUtils.writeStringToFile(a, "1\n2\n3\n4.5\n5\n6\n7\n8");
		repo.add().addFilepattern("a.txt").call();
		feature = repo.commit().setMessage("feature").call();

		repo.checkout().setName("master").call();
		FileUtils.writeStringToFile(a, "1\n2\n3\n4\n5.5\n6\n7\n8");
		repo.add().addFilepattern("a.txt").call();
		master = repo.commit().setMessage("master").call();

		repo.merge().include(feature.getId()).call();
		FileUtils.writeStringToFile(a, "1\n2\n3\n4.5\n5.5\n6\n7\n8");
		repo.add().addFilepattern("a.txt").call();
		merger = repo.commit().setMessage("merger").call();

		FileUtils.writeStringToFile(a, "1.5\n2\n3\n4.5\n5.5\n6\n7\n8");
		repo.add().addFilepattern("a.txt").call();
		target = repo.commit().setMessage("final").call();
	}

	@After
	public void tearDown() throws Exception {
		repo.close();
	}

	@Test
	public void test() throws Exception {
		JGit git = new JGit(tempFolder.getRoot().getAbsolutePath() + "/.git");
		Set<RevCommit> hunks = git.findHunkDependencies(target, null, 3, false,
				Collections.<String> emptySet());

		for (RevCommit h : hunks) {
			PrintUtils.print(JGitUtils.summary(h));
		}

		ChangeExtractor extractor = new ChangeExtractor(git);
		Set<GitRefSourceCodeChange> changes = extractor.extractChanges(merger);
		PrintUtils.print(changes.size());
	}

	@Test
	public void testMergeBase() throws Exception {
		RevCommit base = JGitUtils.getMergeBase(repo.getRepository(), master,
				feature);
		PrintUtils.print(JGitUtils.summary(base));
	}
}
