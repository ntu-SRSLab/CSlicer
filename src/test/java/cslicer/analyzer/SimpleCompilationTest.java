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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cslicer.TestUtils;

public class SimpleCompilationTest {
	private Git repo;

	private final String REV0 = "public class Main {\n"
			+ "\tpublic static void main (String[] args) {\n"
			+ "\t\tSystem.out.println(\"Hello World!\");\n" + "\t}\n" + "}";
	private final String REV1 = "public class Main {\n"
			+ "\tpublic static void main (String[] args) {\n"
			+ "\t\tSystem.out.println(\"Hello World!\");\n"
			+ "\t\tSystem.out.println(\"Good bye!\");\n" + "\t}\n" + "}";
	private final String REV2 = "public class Main {\n"
			+ "\tpublic static void main (String[] args) {\n"
			+ "\t\tint x = 1;\n"
			+ "\t\tSystem.out.println(\"Hello World!\");\n"
			+ "\t\tSystem.out.println(\"Good bye!\");\n" + "\t}\n" + "}";
	private File mainFile;

	private final String[] REVS = { REV0, REV1, REV2 };

	private ProjectConfiguration config = new ProjectConfiguration();

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		// set up repo
		File root = tempFolder.newFolder("repo");
		repo = Git.init().setDirectory(root).call();
		mainFile = FileUtils.getFile(root, "Main.java");
		TestUtils.setupBasicBuildScript(tempFolder);

		String target_commit = null;
		int count = 0;
		for (String rev : REVS) {
			FileUtils.writeStringToFile(mainFile, rev);
			repo.add().addFilepattern("Main.java").call();
			RevCommit t = repo.commit().setMessage("C" + count).call();
			if (count == 0) {
				target_commit = t.getId().name();
			}
			count++;
		}

		config.setRepositoryPath(TestUtils.getSimpleRepoPath(tempFolder))
				.setStartCommitId(target_commit)
				.setBuildScriptPath(TestUtils.getBuildScriptPath(tempFolder));
	}

	@After
	public void tearDown() throws Exception {
		repo.close();
	}

	@Ignore
	@Test
	public void test() throws Exception {
		Slicer ref = new Slicer(config);
		System.out.println("PRUNE ORDER: OLD_TO_NEW");
		ref.doSlicing();
		String finalContent = FileUtils.readFileToString(mainFile);

		assertEquals(finalContent, REV0);
		assertEquals(2, ref.getReducedSet().size());
	}

}
