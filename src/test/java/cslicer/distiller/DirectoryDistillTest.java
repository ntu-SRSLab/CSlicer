package cslicer.distiller;

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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.jgit.JGit;

public class DirectoryDistillTest {

	private Git repo;
	private String repo_path;
	private RevCommit left;
	private RevCommit right;

	private JGit git;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {

		// set up repo
		File root = tempFolder.newFolder("repo");
		repo = Git.init().setDirectory(root).call();
		File mainFile = FileUtils.getFile(root, "Main.java");
		repo_path = Paths.get(root.getAbsolutePath(), ".git").toString();

		FileUtils.copyInputStreamToFile(
				getClass().getResourceAsStream("/basic/Main.java.txt"),
				mainFile);
		FileUtils.writeStringToFile(FileUtils.getFile(root, "Lala.java"),
				"public class Lala {private double z;\n//comments\npublic void getFoo(){return 120;}}");
		repo.add().addFilepattern(".").call();
		left = repo.commit().setMessage("C0").call();

		repo.rm().addFilepattern("Lala.java").call();
		FileUtils.copyInputStreamToFile(
				getClass().getResourceAsStream("/basic/Main.java-2.txt"),
				mainFile);
		FileUtils.writeStringToFile(FileUtils.getFile(root, "Haha.java"),
				"public class Haha {private int y;public void getBar(){return 100;}}");
		repo.add().addFilepattern(".").call();
		right = repo.commit().setMessage("C1").call();

		git = new JGit(repo_path);
	}

	@After
	public void tearDown() throws Exception {
		repo.close();
	}

	@Test
	public void testDiff() throws Exception {
		List<DiffEntry> diffs = git.getRepoDirDiff(left, right);
		assertEquals(3, diffs.size());
	}

	@Test
	public void testDistill() throws Exception {
		ChangeExtractor extractor = new ChangeExtractor(git);
		extractor.extractChanges(right);
	}

}
