package cslicer;

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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.rules.TemporaryFolder;

import cslicer.analyzer.ProjectConfiguration.BUILD_SYSTEM;
import cslicer.jgit.JGit;
import cslicer.utils.DependencyCache;
import cslicer.utils.PrintUtils;

/**
 * @author Polaris
 *
 */
public class TestUtils {

	private static final String POM_FILE = "pom-test.xml";

	private static final String ANT_FILE = "ant-test.xml";

	/**
	 * Setup a simple Java project with its Maven build script
	 * 
	 * @param tempFolder
	 *            the test directory root, a {@link TemporaryFolder} object
	 * @param build
	 *            build system
	 * @throws IOException
	 */
	public static void addMainTestSuite(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);

		File src = tempFolder.newFolder("src");
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/basic/Main.java.txt"),
				FileUtils.getFile(src, "main", "java", "Main.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/basic/MainTest.java.txt"),
				FileUtils.getFile(src, "test", "java", "MainTest.java"));
	}

	private static void addFooTestSuite(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);

		File src = tempFolder.newFolder("src");
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/fourchange/c0"),
				FileUtils.getFile(src, "main", "java", "Foo.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/fourchange/test"),
				FileUtils.getFile(src, "test", "java", "TestFoo.java"));
	}

	private static void addVersionTrackingTestSuite(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);
		File src = tempFolder.newFolder("src");

		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/version/c0-1"),
				FileUtils.getFile(src, "main", "java", "Boo.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/version/c0-2"),
				FileUtils.getFile(src, "main", "java", "Bar.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/version/test"),
				FileUtils.getFile(src, "test", "java", "TestFoo.java"));
	}

	private static void addJavaSlicerTestSuite(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);
		File src = tempFolder.newFolder("src");

		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/javaslicer/c0-1"),
				FileUtils.getFile(src, "main", "java", "Management.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/javaslicer/c0-2"),
				FileUtils.getFile(src, "main", "java", "Positions.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/javaslicer/test"),
				FileUtils.getFile(src, "test", "java", "TestJavaSlicer.java"));
	}
	
	private static void addDaikonTestSuite(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);
		File src = tempFolder.newFolder("src");

		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/daikon/c0-1"),
				FileUtils.getFile(src, "main", "java", "Example.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/daikon/test"),
				FileUtils.getFile(src, "test", "java", "TestExample.java"));
	}
	
	private static void addDaikonPaperTestSuite(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);
		File src = tempFolder.newFolder("src");

		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/daikon/paperexample/c0-1"),
				FileUtils.getFile(src, "main", "java", "Example.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/daikon/paperexample/test"),
				FileUtils.getFile(src, "test", "java", "TestExample.java"));
	}
	
	private static void addDaikonPaperTestSuite2(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);
		File src = tempFolder.newFolder("src");

		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/daikon/paperexample2/c0-1"),
				FileUtils.getFile(src, "main", "java", "Example.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/daikon/paperexample2/test"),
				FileUtils.getFile(src, "test", "java", "TestExample.java"));
	}
	
	private static void addDaikonPartitionTestSuite(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);
		File src = tempFolder.newFolder("src");

		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/daikon/partitionexample/c0-1"),
				FileUtils.getFile(src, "main", "java", "Example.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/daikon/partitionexample/test"),
				FileUtils.getFile(src, "test", "java", "TestExample.java"));
	}

	/**
	 * Setup a Java project testing hierarchy changes
	 * 
	 * @param tempFolder
	 *            the test directory root, a {@link TemporaryFolder} object
	 * @param build
	 *            build system
	 * @throws IOException
	 */
	public static void addHierachyTestSuite(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		addBuildScript(tempFolder, build);

		File src = tempFolder.newFolder("src");
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/hierarchy/Main.java.txt"),
				FileUtils.getFile(src, "main", "java", "Main.java"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class
						.getResourceAsStream("/hierarchy/MainTest.java.txt"),
				FileUtils.getFile(src, "test", "java", "MainTest.java"));
	}

	public static void addBuildScript(TemporaryFolder tempFolder,
			BUILD_SYSTEM build) throws IOException {
		FileUtils.copyInputStreamToFile(
				TestUtils.class
						.getResourceAsStream(build.equals(BUILD_SYSTEM.MAVEN)
								? "/standard_build_script.xml"
								: "/ant_basic_build_script.xml"),
				FileUtils.getFile(tempFolder.getRoot(), POM_FILE));
	}

	/**
	 * Get the .git path of standard test repository.
	 * 
	 * @param tempFolder
	 *            the test directory root, a {@link TemporaryFolder} object
	 * @return canonical path for the repository .git meta folder
	 */
	public static String getStandardRepoPath(TemporaryFolder tempFolder) {
		return FileUtils.getFile(tempFolder.getRoot(), "src", ".git")
				.getAbsolutePath();
	}

	/**
	 * Get the .git path of simple test repository.
	 * 
	 * @param tempFolder
	 *            the test directory root, a {@link TemporaryFolder} object
	 * @return
	 */
	public static String getSimpleRepoPath(TemporaryFolder tempFolder) {
		return FileUtils.getFile(tempFolder.getRoot(), "repo", ".git")
				.getAbsolutePath();
	}

	/**
	 * Get the root path of standard test repository as {@link File}.
	 * 
	 * @param tempFolder
	 *            the test directory root, a {@link TemporaryFolder} object
	 * @return
	 */
	private static File getStandardRepoPathFile(TemporaryFolder tempFolder) {
		return FileUtils.getFile(tempFolder.getRoot(), "src");
	}

	/**
	 * @param tempFolder
	 *            the test directory root, a {@link TemporaryFolder} object
	 * @return canonical path for the Maven POM script
	 */
	public static String getBuildScriptPath(TemporaryFolder tempFolder) {
		return FileUtils.getFile(tempFolder.getRoot(), POM_FILE)
				.getAbsolutePath();
	}

	/**
	 * Setup a simple test Git repository with 3 commits and Maven build file.
	 * 
	 * @param tempFolder
	 *            the test directory root, a {@link TemporaryFolder} object
	 * @param buildSys
	 *            type of build system
	 * @return the initial {@link RevCommit} object
	 * @throws IOException
	 * @throws GitAPIException
	 * @see addMainTestSuite
	 */
	public static RevCommit setupShortTestRepo(TemporaryFolder tempFolder,
			BUILD_SYSTEM buildSys) throws IOException, GitAPIException {
		addMainTestSuite(tempFolder, buildSys);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		updateMainFile("/basic/Main.java-2.txt", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateMainFile("/basic/Main.java-3.txt", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		repo.close();

		return initial;
	}

	/**
	 * Setup a test repository containing five commits (the running example in
	 * ASE'15 paper).
	 * 
	 * @param tempFolder
	 *            temporary folder where the test repository is going to be
	 *            created within
	 * @param buildSys
	 *            type of build system
	 * @return the initial {@link RevCommit} in the repository
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public static RevCommit setupLongTestRepo(TemporaryFolder tempFolder,
			BUILD_SYSTEM buildSys) throws IOException, GitAPIException {
		addFooTestSuite(tempFolder, buildSys);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		updateFooFile("/fourchange/c1", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateFooFile("/fourchange/c2", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		updateFooFile("/fourchange/c3", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C3").call();

		updateFooFile("/fourchange/c4", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C4").call();

		updateFooFile("/fourchange/c5", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C5").call();

		repo.close();

		return initial;
	}

	/**
	 * Setup a test repository demonstrating the version tracking algorithm.
	 * 
	 * @param tempFolder
	 *            temporary folder where the test repository is going to be
	 *            created within
	 * @param buildSys
	 *            type of build system
	 * @return the initial {@link RevCommit} in the repository
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public static RevCommit setupVersionTrackingRepo(TemporaryFolder tempFolder,
			BUILD_SYSTEM buildSys) throws IOException, GitAPIException {
		addVersionTrackingTestSuite(tempFolder, buildSys);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		updateSourceFile("/version/c1-1", "Boo.java", tempFolder);
		updateSourceFile("/version/c1-2", "Bar.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateSourceFile("/version/c2-1", "Boo.java", tempFolder);
		updateSourceFile("/version/c2-2", "Bar.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		updateSourceFile("/version/c3-1", "Boo.java", tempFolder);
		updateSourceFile("/version/c3-2", "Bar.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C3").call();

		repo.close();

		return initial;
	}

	/**
	 * Setup a test repository demonstrating the javaslicer.
	 * 
	 * @param tempFolder
	 *            temporary folder where the test repository is going to be
	 *            created within
	 * @param buildsys
	 *            type of build system
	 * @return the initial {@link RevCommit} in the repository
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public static RevCommit setupJavaSlicerRepo(TemporaryFolder tempFolder,
			BUILD_SYSTEM buildsys) throws IOException, GitAPIException {
		addJavaSlicerTestSuite(tempFolder, buildsys);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		updateSourceFile("/javaslicer/c1-1", "Management.java", tempFolder);
		updateSourceFile("/javaslicer/c1-2", "Positions.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateSourceFile("/javaslicer/c2-1", "Management.java", tempFolder);
		updateSourceFile("/javaslicer/c2-2", "Positions.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		updateSourceFile("/javaslicer/c3-1", "Management.java", tempFolder);
		updateSourceFile("/javaslicer/c3-2", "Positions.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C3").call();

		repo.close();

		return initial;
	}
	
	public static RevCommit setupDaikonRepo(TemporaryFolder tempFolder,
			BUILD_SYSTEM buildsys) throws IOException, GitAPIException {
		addDaikonTestSuite(tempFolder, buildsys);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		updateSourceFile("/daikon/c1-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateSourceFile("/daikon/c2-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		updateSourceFile("/daikon/c3-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C3").call();
		
		updateSourceFile("/daikon/c4-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C4").call();

		repo.close();

		return initial;
	}
	
	public static RevCommit setupDaikonPaperRepo(TemporaryFolder tempFolder,
			BUILD_SYSTEM buildsys) throws IOException, GitAPIException {
		addDaikonPaperTestSuite(tempFolder, buildsys);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		updateSourceFile("/daikon/paperexample/c1-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateSourceFile("/daikon/paperexample/c2-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		updateSourceFile("/daikon/paperexample/c3-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C3").call();
		
		updateSourceFile("/daikon/paperexample/c4-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C4").call();

		repo.close();

		return initial;
	}
	
	public static RevCommit setupDaikonPaperRepo2(TemporaryFolder tempFolder,
			BUILD_SYSTEM buildsys) throws IOException, GitAPIException {
		addDaikonPaperTestSuite2(tempFolder, buildsys);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		updateSourceFile("/daikon/paperexample2/c1-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateSourceFile("/daikon/paperexample2/c2-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		updateSourceFile("/daikon/paperexample2/c3-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C3").call();
		
		updateSourceFile("/daikon/paperexample2/c4-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C4").call();
		
		updateSourceFile("/daikon/paperexample2/c5-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C5").call();

		repo.close();

		return initial;
	}
	
	public static RevCommit setupDaikonPartitionRepo(TemporaryFolder tempFolder,
			BUILD_SYSTEM buildsys) throws IOException, GitAPIException {
		addDaikonPartitionTestSuite(tempFolder, buildsys);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		updateSourceFile("/daikon/partitionexample/c1-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateSourceFile("/daikon/partitionexample/c2-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		updateSourceFile("/daikon/partitionexample/c3-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C3").call();
		
		updateSourceFile("/daikon/partitionexample/c4-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C4").call();
		
		updateSourceFile("/daikon/partitionexample/c5-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C5").call();
		
		updateSourceFile("/daikon/partitionexample/c6-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C6").call();
		
		updateSourceFile("/daikon/partitionexample/c7-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C7").call();
		
		updateSourceFile("/daikon/partitionexample/c8-1", "Example.java", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C8").call();

		repo.close();

		return initial;
	}

	private static void updateFooFile(String source, TemporaryFolder tempFolder)
			throws IOException {
		updateSourceFile(source, "Foo.java", tempFolder);
	}

	private static void updateMainFile(String source,
			TemporaryFolder tempFolder) throws IOException {
		updateSourceFile(source, "Main.java", tempFolder);
	}

	private static void updateSourceFile(String source, String target,
			TemporaryFolder tempFolder) throws IOException {
		File src = FileUtils.getFile(tempFolder.getRoot(), "src");
		FileUtils.writeStringToFile(
				FileUtils.getFile(src, "main", "java", target),
				IOUtils.toString(TestUtils.class.getResourceAsStream(source)));
	}

	public static void setupBasicBuildScript(TemporaryFolder tempFolder)
			throws IOException {
		// set up build script
		assertNotNull("Test build script file is missing",
				TestUtils.class.getResource("/basic_build_script.xml"));
		FileUtils.copyInputStreamToFile(
				TestUtils.class.getResourceAsStream("/basic_build_script.xml"),
				FileUtils.getFile(tempFolder.getRoot(), POM_FILE));
	}

	public static RevCommit setupTwoBranchRepo(TemporaryFolder tempFolder)
			throws GitAPIException, IOException {
		addMainTestSuite(tempFolder, BUILD_SYSTEM.MAVEN);

		Git repo = Git.init().setDirectory(getStandardRepoPathFile(tempFolder))
				.call();
		repo.add().addFilepattern(".").call();
		RevCommit initial = repo.commit().setMessage("C0").call();

		repo.checkout().setCreateBranch(true).setName("feature").call();

		updateMainFile("/basic/Main.java-2.txt", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C1").call();

		updateMainFile("/basic/Main.java-3.txt", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C2").call();

		repo.checkout().setName("master").call();

		updateMainFile("/basic/Main.java-2-master.txt", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C3").call();

		updateMainFile("/basic/Main.java-3-master.txt", tempFolder);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("C4").call();

		return initial;
	}

	public static Map<String, Pair<String, String>> readHadoop6581()
			throws IOException {
		String fileContents = IOUtils.toString(TestUtils.class
				.getResourceAsStream("/precision/hadoop-6581-complete"));
		Map<String, Pair<String, String>> res = new LinkedHashMap<String, Pair<String, String>>();

		for (String line : StringUtils.split(fileContents,
				System.lineSeparator())) {
			String id = line.substring(0, 40);
			String flag = line.substring(41, 42);
			String message = line.substring(43);
			res.put(id, Pair.of(flag, message));
		}

		return res;
	}

	public static List<RevCommit> computeHunkDepSet(JGit repo,
			final Collection<RevCommit> cSet, final Set<RevCommit> pool) {
		DependencyCache cache = new DependencyCache();

		int i = 0;
		for (RevCommit a : cSet) {
			if (cache.directDepsComputed(a)) {
				continue;
			} else {
				repo.findHunkDependencies(a, new HashSet<RevCommit>(pool),
						cache);
			}

			i++;
			PrintUtils.printProgress("", i * 100 / cSet.size());
		}

		PrintUtils.print(cache);

		return cache.getSortedDeps(true);
	}
}
