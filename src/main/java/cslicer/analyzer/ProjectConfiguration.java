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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import cslicer.builder.UnitTestScope;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

public class ProjectConfiguration {

  public enum BUILD_SYSTEM {
		MAVEN, ANT
	}

	// default configurations
	public static final String DEFAULT_JDK = "1.7";
	public static final int DEFAULT_HISTORY_LENGTH = -1;
	public static final BUILD_SYSTEM DEFAULT_BUILD_SYSTEM = BUILD_SYSTEM.MAVEN;
	public static boolean DEFAULT_MAVEN_OUTPUT_ENABLED = false;
	public static boolean DEFAULT_OUTPUT_HUNK_GRAPH = false;
	public static final UnitTestScope DEFAULT_TEST_SCOPE = new UnitTestScope();
	public static final String DEFAULT_REPO_PATH = null;
	public static final String DEFAULT_START_COMMIT = null;
	public static final String DEFAULT_END_COMMIT = null;
	public static final String DEFAULT_BUILD_PATH = null;
	public static final String DEFAULT_SUB_BUILD_PATH = null;
	public static final String DEFAULT_JACOCO_PATH = null;
	public static final Set<String> DEFAULT_JAVA_SLICER_PATH = Collections
			.emptySet();
	public static final Set<String> DEFAULT_SOURCE_ROOT = Collections
			.emptySet();
	public static final String DEFAULT_CLASS_ROOT = null;
	public static final String DEFAULT_TOUCH_SET_PATH = null;
	public static final String DEFAULT_CALL_GRAPH_PATH = null;
	public static final Set<String> DEFAULT_EXCLUDED_PATH = Collections
			.emptySet();
	public static final Set<String> DEFAULT_SLICING_CRITERIA = Collections
			.emptySet();
	private static final boolean DEFAULT_SKIP_PICKING = false;
	private static final String DEFAULT_DAIKON_CONFIG_PATH = null;
	private static final String DEFAULT_DAIKON_JAR_PATH = null;
	private static final String DEFAULT_CHIVORY_JAR_PATH = null;
	private static final Set<String> DEFAULT_DAIKON_INCLUDES = Collections
			.emptySet();
	private static final boolean DEFAULT_ENABLE_INVARIANT = false;
	private static final boolean DEFAULT_ENABLE_INIT_RANK = true;
	private static final boolean DEFAULT_ENABLE_LEARNING = true;
	private static final boolean DEFAULT_ENABLE_COMP_CHECK = true;
	private static final boolean DEFAULT_ENABLE_PROB = true;

	private static final boolean DEFAULT_ENABLE_INTERSETION = false;
	private static final boolean DEFAULT_ENABLE_JSON = false;
	private static final String DEFAULT_JSON_PATH = null;
	private static final String DEFAULT_TEST_CLASS_ROOT = null;

	private static final Set<String> DEFAULT_CHERRY_PICK_COMMITS = Collections.emptySet();

	// private fields
	private String fRepoPath = DEFAULT_REPO_PATH;
	private String fStartId = DEFAULT_START_COMMIT;
	private String fEndId = DEFAULT_END_COMMIT; // optional - default HEAD
	private String fBuildPath = DEFAULT_BUILD_PATH;
	private String fSubBuildPath = DEFAULT_SUB_BUILD_PATH;
	private boolean fOutputHunkGraph = DEFAULT_OUTPUT_HUNK_GRAPH;
	private boolean fSkipPicking = DEFAULT_SKIP_PICKING;
	private String fSubModuleBuildPath = null; // optional
	private String fProjectPath = null; // optional
	private UnitTestScope fTestScope = DEFAULT_TEST_SCOPE; // optional
	private BUILD_SYSTEM fBuildSystem = DEFAULT_BUILD_SYSTEM; // optional
	private boolean fMavenOutputEnabled = DEFAULT_MAVEN_OUTPUT_ENABLED; // optional
	private String fJDKVersion = DEFAULT_JDK; // optional. default: 1.7

	/*
	 * optional. default -1. the number of commits to trace back from end if
	 * jacoco exec is provided then no need for build and module path, or tests
	 */
	private int fHistoryLength = DEFAULT_HISTORY_LENGTH;
	private String fTestJacocoExecPath = DEFAULT_JACOCO_PATH; // optional
	private Set<String> fSourceRootPath = DEFAULT_SOURCE_ROOT; // optional
	private String fClassRootPath = DEFAULT_CLASS_ROOT; // optional
	private String fTestClassRootPath = DEFAULT_TEST_CLASS_ROOT; // optional
	private Set<String> fExcludedPath = DEFAULT_EXCLUDED_PATH;
	private String fTouchSetPath = DEFAULT_TOUCH_SET_PATH; // optional
	private String fCallGraphPath = DEFAULT_CALL_GRAPH_PATH; // optional

	private Set<String> fJavaSlicerDumpPath = DEFAULT_JAVA_SLICER_PATH; // optional
	private Set<String> fJavaSlicerCriteria = DEFAULT_SLICING_CRITERIA; // optional
	private String fDaikonConfigPath = DEFAULT_DAIKON_CONFIG_PATH; // required
																	// by
																	// Refiner
	private String fDaikonJarPath = DEFAULT_DAIKON_JAR_PATH;
	private String fChicoryJarPath = DEFAULT_CHIVORY_JAR_PATH;
	private Set<String> fDaikonIncludes = DEFAULT_DAIKON_INCLUDES;
	private boolean fEnableInvariant = DEFAULT_ENABLE_INVARIANT;
	private boolean fEnableInitRank = DEFAULT_ENABLE_INIT_RANK;
	private boolean fEnableLearning = DEFAULT_ENABLE_LEARNING;
	private boolean fEnableCompCheck = DEFAULT_ENABLE_COMP_CHECK;
	private boolean fEnableProbablistic = DEFAULT_ENABLE_PROB;

	private boolean fEnableIntersection = DEFAULT_ENABLE_INTERSETION;
	private boolean fEnableJson = DEFAULT_ENABLE_JSON;
	private String fJsonPath = DEFAULT_JSON_PATH;

	private Set<String> fCherryPickCommits = DEFAULT_CHERRY_PICK_COMMITS;

	public ProjectConfiguration() {
	}

	public ProjectConfiguration(Path configPath) {
		Properties config = new Properties();
		try {
			// load configurations from property file
			config.load(FileUtils.openInputStream(configPath.toFile()));

			this.setDaikonConfigPath(config.getProperty("daikonConfig",
					DEFAULT_DAIKON_CONFIG_PATH));
			this.setChivoryJarPath(config.getProperty("chicoryPath",
					DEFAULT_CHIVORY_JAR_PATH));
			this.setDaikonJarPath(
					config.getProperty("daikonJar", DEFAULT_DAIKON_JAR_PATH));
			this.setExcludedPaths(config.getProperty("excludePaths", null));

			this.setRepositoryPath(
					config.getProperty("repoPath", DEFAULT_REPO_PATH));
			this.setJacocoExecPath(
					config.getProperty("execPath", DEFAULT_JACOCO_PATH));
			this.setJavaSlicerDumpPath(config.getProperty("dumpPath", null));
			this.setJavaSlicerCriteria(config.getProperty("criteria", null));
			this.setStartCommitId(
					config.getProperty("startCommit", DEFAULT_START_COMMIT));
			this.setEndCommitId(
					config.getProperty("endCommit", DEFAULT_END_COMMIT));
			this.setSourceRootPath(config.getProperty("sourceRoot", null));
			this.setClassRootPath(
					config.getProperty("classRoot", DEFAULT_CLASS_ROOT));
			this.setTestClassRootPath(config.getProperty("testClassRoot",
					DEFAULT_TEST_CLASS_ROOT));
			this.setBuildScriptPath(
					config.getProperty("buildScriptPath", DEFAULT_BUILD_PATH));
			this.setSubModuleBuildScriptPath(config.getProperty("subModulePath",
					DEFAULT_SUB_BUILD_PATH));
			this.setTestCases(
					new UnitTestScope(config.getProperty("testScope", null)));
			this.setTouchSetPath(
					config.getProperty("touchSetPath", DEFAULT_TOUCH_SET_PATH));
			this.setCallGraphPath(config.getProperty("callGraphPath",
					DEFAULT_CALL_GRAPH_PATH));
			this.setAnalysisLength(
					Integer.parseInt(config
							.getProperty("historyLength",
									String.valueOf(DEFAULT_HISTORY_LENGTH))
							.trim()));
			this.setProjectJDKVersion(
					config.getProperty("jdkVersion", DEFAULT_JDK));

			this.setJsonPath(config.getProperty("jsonPath", null));
			this.setCherryPickCommits(
							config.getProperty("cherryPick", null));

		} catch (IOException e) {
			PrintUtils.print("Error loading project configuration file at: "
					+ configPath, TAG.WARNING);
			e.printStackTrace();
		}
	}

	/**
	 * Return the length of history to be analyzed.
	 * 
	 * @return length
	 */
	public int getAnalysisLength() {
		if (fHistoryLength > 0)
			return fHistoryLength;
		else
			return 0;
	}

	/**
	 * Return path to project build script.
	 * 
	 * @return path
	 */
	public String getBuildScriptPath() {
		return fBuildPath;
	}

	public BUILD_SYSTEM getBuildSystem() {
		return fBuildSystem;
	}

	public String getCallGraphPath() {
		return fCallGraphPath;
	}

	public String getClassRootPath() {
		return fClassRootPath;
	}

	public String getDaikonConfigPath() {
		return fDaikonConfigPath;
	}

	public String getEndCommitId() {
		return fEndId;
	}

	public Set<String> getExcludedPaths() {
		return fExcludedPath;
	}

	public String getJacocoExecPath() {
		return fTestJacocoExecPath;
	}

	public Set<String> getJavaSlicerCriteria() {
		return fJavaSlicerCriteria;
	}

	public Set<String> getJavaSlicerDumpPath() {
		return fJavaSlicerDumpPath;
	}

	public boolean getOutputHunkGraph() {
		return fOutputHunkGraph;
	}

	public String getProjectJDKVersion() {
		return fJDKVersion;
	}

	/**
	 * The path to the project base directory. Use the root directory of the
	 * repository path by default.
	 * 
	 * @return a {@code String} representation of the project path
	 */
	public String getProjectPath() {
		if (fProjectPath == null)
			return FilenameUtils.getFullPath(
					FilenameUtils.normalizeNoEndSeparator(fRepoPath));
		return fProjectPath;
	}

	public String getRepositoryPath() {
		return fRepoPath;
	}

	public boolean getSkipPicking() {
		return fSkipPicking;
	}

	public Set<String> getSourceRootPath() {
		return fSourceRootPath;
	}

	public String getStartCommitId() {
		return fStartId;
	}

	public String getSubModuleBuildScriptPath() {
		if (fSubModuleBuildPath == null)
			return fBuildPath;
		return fSubModuleBuildPath;
	}

	/**
	 * Return the names of interested test cases.
	 * 
	 * @return {@code null} if test suite is not set, then by default the whole
	 *         set is used
	 */
	public UnitTestScope getTestCases() {
		return fTestScope;
	}

	/**
	 * Return path to touch set dump file.
	 * 
	 * @return path
	 */
	public String getTouchSetPath() {
		return fTouchSetPath;
	}

	/**
	 * Get the project output setting.
	 * 
	 * @return {@code true} if output is enabled
	 */
	public boolean isBuilderOutputEnabled() {
		return this.fMavenOutputEnabled;
	}

	public boolean isClassRootPathSet() {
		return fClassRootPath != null;
	}

	public boolean isConsistent() {
		// repository path and start commit are required
		if (fRepoPath == null || (fStartId == null && fHistoryLength < 0))
			return false;

		if (fOutputHunkGraph)
			return true;

		if (fBuildPath == null)
			return false;

		if (fTestClassRootPath != null && fClassRootPath != null)
			return true;

		if ((fTestJacocoExecPath == null) != (fSourceRootPath.size() == 0)
				|| (fTestJacocoExecPath == null) != (fClassRootPath == null)
				|| (fClassRootPath == null) != (fSourceRootPath.size() == 0))
			return false;

		return true;
	}

	public boolean isJacocoExecPathSet() {
		return fTestJacocoExecPath != null;
	}

	public boolean isJavaSlicerDumpPathSet() {
		return fJavaSlicerDumpPath.size() > 0;
	}

	public boolean isSourceRootPathSet() {
		return fSourceRootPath != null;
	}

	public boolean isSubModuleSet() {
		return this.fSubModuleBuildPath != null;
	}

	public ProjectConfiguration setAnalysisLength(int len) {
		fHistoryLength = Math.max(0, len);
		return this;
	}

	public ProjectConfiguration setBuildScriptPath(String buildPath) {
		if (buildPath != null)
			fBuildPath = FilenameUtils.normalize(buildPath);
		return this;
	}

	public ProjectConfiguration setBuildSystem(BUILD_SYSTEM buildSys) {
		this.fBuildSystem = buildSys;
		return this;
	}

	public ProjectConfiguration setCallGraphPath(String callPath) {
		if (callPath != null)
			fCallGraphPath = FilenameUtils.normalize(callPath);
		return this;
	}

	public ProjectConfiguration setClassRootPath(String classPath) {
		fClassRootPath = FilenameUtils.normalize(classPath);
		return this;
	}

	public ProjectConfiguration setDaikonConfigPath(String path) {
		if (path != null)
			fDaikonConfigPath = FilenameUtils.normalize(path.trim());
		return this;
	}

	public ProjectConfiguration setEnableBuilderOutput(boolean enableOutput) {
		this.fMavenOutputEnabled = enableOutput;
		return this;
	}

	public ProjectConfiguration setEndCommitId(String commitId) {
		fEndId = commitId == null ? null : commitId.trim();
		return this;
	}

	public ProjectConfiguration setExcludedPaths(String excludePaths) {
		if (excludePaths == null)
			return this;

		fExcludedPath = new HashSet<String>();
		for (String s : Arrays.asList(excludePaths.split(","))) {
			fExcludedPath.add(FilenameUtils.normalize(s.trim()));
		}
		return this;
	}

	public ProjectConfiguration setJacocoExecPath(String execPath) {
		fTestJacocoExecPath = FilenameUtils.normalize(execPath);
		return this;
	}

	public ProjectConfiguration setJavaSlicerCriteria(String criteria) {
		if (criteria == null)
			return this;

		fJavaSlicerCriteria = new HashSet<String>();
		for (String c : Arrays.asList(criteria.split(","))) {
			this.fJavaSlicerCriteria.add(c.trim());
		}
		return this;
	}

	public ProjectConfiguration setJavaSlicerDumpPath(String dumpPaths) {
		if (dumpPaths == null)
			return this;

		fJavaSlicerDumpPath = new HashSet<String>();
		for (String path : Arrays.asList(dumpPaths.split(","))) {
			fJavaSlicerDumpPath.add(FilenameUtils.normalize(path.trim()));
		}
		return this;
	}

	public ProjectConfiguration setOutputHunkGraph(boolean enable) {
		fOutputHunkGraph = enable;
		return this;
	}

	public ProjectConfiguration setProjectJDKVersion(String version) {
		version = version == null ? null : version.trim();
		assert version.equals("1.6") || version.equals("1.7");
		fJDKVersion = version;
		return this;
	}

	public ProjectConfiguration setProjectPath(String projectPath) {
		this.fProjectPath = FilenameUtils.normalize(projectPath);
		return this;
	}

	public ProjectConfiguration setRepositoryPath(String repoPath) {
		fRepoPath = FilenameUtils.normalize(repoPath);
		return this;
	}

	public ProjectConfiguration setSkipPicking(boolean skipPick) {
		fSkipPicking = skipPick;
		return this;
	}

	public ProjectConfiguration setSourceRootPath(String sourcePath) {
		if (sourcePath == null)
			return this;

		fSourceRootPath = new HashSet<String>();
		for (String s : Arrays.asList(sourcePath.split(",")))
			fSourceRootPath.add(FilenameUtils.normalize(s.trim()));
		return this;
	}

	public ProjectConfiguration setStartCommitId(String commitId) {
		fStartId = commitId == null ? null : commitId.trim();
		return this;
	}

	public ProjectConfiguration setSubModuleBuildScriptPath(String buildPath) {
		if (buildPath != null)
			fSubModuleBuildPath = FilenameUtils.normalize(buildPath.trim());
		return this;
	}

	public ProjectConfiguration setTestCases(UnitTestScope scope) {
		this.fTestScope = scope;
		return this;
	}

	public ProjectConfiguration setTouchSetPath(String path) {
		if (path != null)
			fTouchSetPath = FilenameUtils.normalize(path.trim());
		return this;
	}

	public String getDaikonJarPath() {
		return fDaikonJarPath;
	}

	public ProjectConfiguration setDaikonJarPath(String fDaikonJarPath) {
		if (fDaikonJarPath != null)
			this.fDaikonJarPath = FilenameUtils
					.normalize(fDaikonJarPath.trim());
		return this;
	}

	public String getChicoryJarPath() {
		return fChicoryJarPath;
	}

	public ProjectConfiguration setChivoryJarPath(String fChivoryJarPath) {
		if (fChivoryJarPath != null)
			this.fChicoryJarPath = FilenameUtils
					.normalize(fChivoryJarPath.trim());
		return this;
	}

	public Set<String> getDaikonIncludes() {
		return fDaikonIncludes;
	}

	public ProjectConfiguration setDaikonIncludes(String includes) {
		if (includes == null)
			return this;

		fDaikonIncludes = new HashSet<String>();
		for (String s : Arrays.asList(includes.split(",")))
			fDaikonIncludes.add(FilenameUtils.normalize(s.trim()));
		return this;
	}

	public ProjectConfiguration setEnableInvariant(boolean hasOption) {
		this.fEnableInvariant = hasOption;
		return this;
	}

	public boolean getEnableInvariant() {
		return fEnableInvariant;
	}

	public ProjectConfiguration setEnableInitRank(boolean b) {
		this.fEnableInitRank = b;
		return this;
	}

	public boolean getEnableInitRank() {
		return fEnableInitRank;
	}

	public ProjectConfiguration setEnableLearning(boolean b) {
		this.fEnableLearning = b;
		return this;
	}

	public boolean getEnableLearning() {
		return fEnableLearning;
	}

	public ProjectConfiguration setEnableCompCheck(boolean b) {
		this.fEnableCompCheck = b;
		return this;
	}

	public boolean getEnableCompCheck() {
		return fEnableCompCheck;
	}

	public ProjectConfiguration setEnableProbablistic(boolean b) {
		this.fEnableProbablistic = b;
		return this;
	}

	public boolean getEnableProbablistic() {
		return fEnableProbablistic;
	}

	public boolean getEnableIntersection() {
		return fEnableIntersection;
	}

	public ProjectConfiguration setEnableIntersection(boolean b) {
		this.fEnableIntersection = b;
		return this;
	}

	public boolean getEnableJson() {
		return fEnableJson;
	}

	public ProjectConfiguration setEnableJson(boolean b) {
		this.fEnableJson = b;
		return this;
	}

	public String getJsonPath() {
		return fJsonPath;
	}

	public ProjectConfiguration setJsonPath(String p) {
		this.fJsonPath = p;
		return this;
	}

	public ProjectConfiguration setCherryPickCommits(String picks) {
		if (picks == null)
			return this;

		fCherryPickCommits = new HashSet<String>();
		for (String s : Arrays.asList(picks.split(",")))
			fCherryPickCommits.add(FilenameUtils.normalize(s.trim()));
		return this;
	}

	public Set<String> getCherryPickCommits() { return fCherryPickCommits; }

	/**
	 * @return the fTestClassRootPath
	 */
	public String getTestClassRootPath() {
		return fTestClassRootPath;
	}

	/**
	 * @param testClassRootPath
	 *            the fTestClassRootPath to set
	 * @return the configuration object itself
	 */
	public ProjectConfiguration setTestClassRootPath(String testClassRootPath) {
		this.fTestClassRootPath = FilenameUtils.normalize(testClassRootPath);
		return this;
	}

}
