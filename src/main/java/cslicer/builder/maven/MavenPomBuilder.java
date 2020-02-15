package cslicer.builder.maven;

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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import cslicer.builder.BuildScriptInvalidException;

/**
 * Modify Maven POM file for coverage data collection.
 * 
 * @author Yi Li
 *
 */
public class MavenPomBuilder {

	private Model pomModel;
	private final File targetPath;
	private final File pomFilePath;
	private File savedPomFile;
	private static final String JACOCO_GROUP_ID = "org.jacoco";
	private static final String JACOCO_ARTIFACT_ID = "jacoco-maven-plugin";
	private static final String JACOCO_EXECUTION_GOAL = "prepare-agent";
	private static final String JACOCO_EXECUTION_ID = "default-prepare-agent";
	private static final String JACOCO_VERSION = "0.7.3.201502191951";
	private static final String JUNIT_GROUP_ID = "junit";
	private static final String JUNIT_ARTIFACT_ID = "junit";
	private static final String JUNIT_VERSION = "4.11";
	private static final String SOURCE_GROUP_ID = "org.apache.maven.plugins";
	private static final String SOURCE_ARTIFACT_ID = "maven-source-plugin";
	private static final String SOURCE_VERSION = "2.2.1";
	private static final String ATTACH_SOURCE_GOAL = "jar";
	private static final String ATTACH_JAR_NAME = "gitref-test";
	private static final String ATTACH_SOURCE_ID = "attach-sources";
	private static final String ATTACH_SOURCE_PHASE = "test";
	private static final String SUREFIRE_GROUP_ID = "org.apache.maven.plugins";
	private static final String SUREFIRE_ARTIFACT_ID = "maven-surefire-plugin";
	private static final String SUREFIRE_VERSION = "2.21.0";
	private static final String RAT_ARTIFACT_ID = "apache-rat-plugin";
	private static final String RAT_GROUP_ID = "org.apache.rat";

	public MavenPomBuilder(File pomFilePath, File targetPath)
			throws BuildScriptInvalidException, IOException {
		this.targetPath = targetPath == null
				? Files.createTempDirectory("gitref").toFile() : targetPath;
		this.pomFilePath = pomFilePath;

		MavenXpp3Reader modelReader = new MavenXpp3Reader();

		try {
			FileReader reader = new FileReader(pomFilePath);
			pomModel = modelReader.read(reader);
			reader.close();
		} catch (IOException | XmlPullParserException e) {
			throw new BuildScriptInvalidException();
		}
		checkIntegrity();

		// save original pom file
		this.savedPomFile = FileUtils.createTempFile("gitref", ".txt",
				targetPath);
		this.savedPomFile.deleteOnExit();
	}

	public MavenPomBuilder(File pomFilePath)
			throws BuildScriptInvalidException, IOException {
		this(pomFilePath, null);
	}

	public String getProjectArtifactId() {
		return pomModel.getArtifactId();
	}

	private void checkIntegrity() throws BuildScriptInvalidException {
		if (pomModel.getBuild() == null) {
			throw new BuildScriptInvalidException(
					"No build rule found in the POM file!");
		}

		List<Dependency> deps = pomModel.getDependencies();
		if (deps == null)
			throw new BuildScriptInvalidException(
					"Required dependencies not found in the POM file!");

		pomModel.getBuild().setDirectory(this.targetPath.getAbsolutePath());
	}

	/**
	 * Add Junit plugin in the Pom file.
	 */
	public final void addJunitPlugin() {
		List<Dependency> deps = pomModel.getDependencies();
		for (Dependency dep : deps) {
			if (dep.getArtifactId().equals(JUNIT_ARTIFACT_ID)
					&& dep.getGroupId().equals(JUNIT_GROUP_ID))
				return;
		}

		Dependency junit = new Dependency();
		junit.setArtifactId(JUNIT_ARTIFACT_ID);
		junit.setGroupId(JUNIT_GROUP_ID);
		junit.setVersion(JUNIT_VERSION);
		junit.setScope("test");
		pomModel.addDependency(junit);
	}

	public final void disableRatCheck() {
		Plugin rat = new Plugin();
		rat.setArtifactId(RAT_ARTIFACT_ID);
		rat.setGroupId(RAT_GROUP_ID);
		pomModel.getBuild().getPluginManagement().removePlugin(rat);
		pomModel.getBuild().removePlugin(rat);
	}

	/**
	 * Add Jacoco build plugin in the Pom file.
	 * 
	 * @return path to the Jacoco execution report file
	 */
	public final String addJacocoBuildPlugin() {
		// a temporary file to store coverage information
		File jacocoExecFile = org.apache.commons.io.FileUtils
				.getFile(targetPath.getPath(), "jacoco.exec");
		// .createTempFile("jacoco_", ".exec", targetPath);
		jacocoExecFile.deleteOnExit();

		Map<String, Plugin> plugins = pomModel.getBuild().getPluginsAsMap();
		String jacocoKey = Plugin.constructKey(JACOCO_GROUP_ID,
				JACOCO_ARTIFACT_ID);

		PluginExecution preTest = preTestJacocoExecution(
				jacocoExecFile.getAbsolutePath());

		Plugin jacoco = new Plugin();
		jacoco.addExecution(preTest);
		jacoco.setArtifactId(JACOCO_ARTIFACT_ID);
		jacoco.setGroupId(JACOCO_GROUP_ID);
		jacoco.setVersion(JACOCO_VERSION);
		plugins.put(jacocoKey, jacoco);
		// }

		pomModel.getBuild().setPlugins(new ArrayList<Plugin>(plugins.values()));
		return jacocoExecFile.getAbsolutePath();
	}

	public void addSurefireBuildPlugin() {
		Map<String, Plugin> plugins = pomModel.getBuild().getPluginsAsMap();
		String surefireKey = Plugin.constructKey(SUREFIRE_GROUP_ID,
				SUREFIRE_ARTIFACT_ID);

		PluginExecution defaultTest = defaultTestSurefireExecution();

		Plugin surefire = new Plugin();
		surefire.addExecution(defaultTest);
		surefire.setArtifactId(SUREFIRE_ARTIFACT_ID);
		surefire.setGroupId(SUREFIRE_GROUP_ID);
		surefire.setVersion(SUREFIRE_VERSION);
		plugins.put(surefireKey, surefire);

		pomModel.getBuild().setPlugins(new ArrayList<Plugin>(plugins.values()));
	}

	private PluginExecution defaultTestSurefireExecution() {
		PluginExecution surefire = new PluginExecution();
		surefire.setPhase("test");
		surefire.setId("default-test");
		surefire.addGoal("test");

		Xpp3Dom configDom = new Xpp3Dom("configuration");
		addDom(configDom, "argLine", "${argLine}");
		surefire.setConfiguration(configDom);

		return surefire;
	}

	/**
	 * Add a surefire plugin equipped with Daikon runtime tracing.
	 * 
	 * @param daikonJarPath
	 *            path to Daikon jar
	 * @param chicoryJarPath
	 *            path to the Chicory jar
	 * @param includePatterns
	 *            class include patterns
	 * @return path to the generated Daikon trace file
	 */
	public final String addDaikonSurefireBuild(String daikonJarPath,
			String chicoryJarPath, Set<String> includePatterns) {
		// a temporary file to store coverage information
		File daikonTraceFile = org.codehaus.plexus.util.FileUtils
				.createTempFile("daikon_", ".dtrace.gz", targetPath);
		daikonTraceFile.deleteOnExit();

		Map<String, Plugin> plugins = pomModel.getBuild().getPluginsAsMap();
		String surefireKey = Plugin.constructKey(SUREFIRE_GROUP_ID,
				SUREFIRE_ARTIFACT_ID);

		Plugin surefire = new Plugin();
		surefire.setArtifactId(SUREFIRE_ARTIFACT_ID);
		surefire.setGroupId(SUREFIRE_GROUP_ID);
		surefire.setVersion(SUREFIRE_VERSION);

		// PluginExecution execution = new PluginExecution();
		// execution.addGoal("test");
		// execution.setPhase("test");
		// execution.setId("default-test");

		Xpp3Dom daikonConfig = new Xpp3Dom("configuration");
		Xpp3Dom daikonJar = new Xpp3Dom("additionalClasspathElements");
		addDom(daikonJar, "additionalClasspathElement", daikonJarPath);
		daikonConfig.addChild(daikonJar);
		// construct Daikon argline
		StringBuffer buffer = new StringBuffer();
		buffer.append("-javaagent:" + chicoryJarPath + "=--dtrace-file="
				+ daikonTraceFile.getName());
		for (String include : includePatterns) {
			buffer.append(",--ppt-select-pattern=" + include);
		}
		// ignore test classes
		buffer.append(",--ppt-omit-pattern=.*Test.*");

		addDom(daikonConfig, "argLine", buffer.toString());
		// other surefire settings
		addDom(daikonConfig, "workingDirectory", targetPath.getAbsolutePath());
		addDom(daikonConfig, "forkCount", "1");
		surefire.setConfiguration(daikonConfig);

		// surefire.addExecution(execution);

		plugins.put(surefireKey, surefire);

		pomModel.getBuild().setPlugins(new ArrayList<Plugin>(plugins.values()));

		return daikonTraceFile.getAbsolutePath();
	}

	/**
	 * Redirect the project build output to a temporary folder.
	 * 
	 * @return path to the redirected build output directory
	 */
	public final String redirectBuildOutput() {
		// a temporary folder to store compiled classes
		File classesFolder = org.apache.commons.io.FileUtils.getFile(targetPath,
				"classes");

		pomModel.getBuild().setOutputDirectory(classesFolder.getAbsolutePath());

		return classesFolder.getAbsolutePath();
	}

	public final String redirectTestOutput() {
		File testClassesFolder = org.apache.commons.io.FileUtils
				.getFile(targetPath, "test-classes");
		pomModel.getBuild()
				.setTestOutputDirectory(testClassesFolder.getAbsolutePath());

		return testClassesFolder.getAbsolutePath();
	}

	/**
	 * Generate a jar collection of source files in the current directory.
	 * 
	 * @return path to the generated jar file
	 */
	public final String generageSourceJar() {
		Map<String, Plugin> plugins = pomModel.getBuild().getPluginsAsMap();
		String sourceKey = Plugin.constructKey(SOURCE_GROUP_ID,
				SOURCE_ARTIFACT_ID);

		PluginExecution attachSource = attachSourceExecution(
				this.targetPath.getAbsolutePath());
		Plugin source = new Plugin();
		source.addExecution(attachSource);
		source.setArtifactId(SOURCE_ARTIFACT_ID);
		source.setGroupId(SOURCE_GROUP_ID);
		source.setVersion(SOURCE_VERSION);
		plugins.put(sourceKey, source);

		pomModel.getBuild().setPlugins(new ArrayList<Plugin>(plugins.values()));
		return org.apache.commons.io.FileUtils
				.getFile(this.targetPath, ATTACH_JAR_NAME + "-sources.jar")
				.getAbsolutePath();
	}

	/**
	 * Write the Pom model to file.
	 * 
	 * @param outputPomFilePath
	 *            path of the Pom file to write to
	 * @throws IOException
	 *             if an I/O exception occurs when writing Pom file
	 */
	public final void writeToPomFile(final String outputPomFilePath)
			throws IOException {
		FileUtils.copyFile(pomFilePath, savedPomFile);

		FileWriter writer = new FileWriter(outputPomFilePath);
		MavenXpp3Writer modelWriter = new MavenXpp3Writer();
		modelWriter.write(writer, pomModel);
		writer.close();
	}

	public void restorePomFile() throws IOException {
		FileUtils.copyFile(this.savedPomFile, this.pomFilePath);
	}

	private PluginExecution preTestJacocoExecution(final String execFilePath) {
		PluginExecution preTest = new PluginExecution();
		preTest.setGoals(Arrays.asList(JACOCO_EXECUTION_GOAL));
		preTest.setId(JACOCO_EXECUTION_ID);

		Xpp3Dom configDom = new Xpp3Dom("configuration");
		addDom(configDom, "destFile", execFilePath);
		preTest.setConfiguration(configDom);

		return preTest;
	}

	private PluginExecution attachSourceExecution(final String sourceDirPath) {
		PluginExecution source = new PluginExecution();
		source.setGoals(Arrays.asList(ATTACH_SOURCE_GOAL));
		source.setId(ATTACH_SOURCE_ID);
		source.setPhase(ATTACH_SOURCE_PHASE);

		Xpp3Dom configDom = new Xpp3Dom("configuration");
		addDom(configDom, "outputDirectory", sourceDirPath);
		addDom(configDom, "finalName", ATTACH_JAR_NAME);
		addDom(configDom, "attach", "false");
		source.setConfiguration(configDom);

		return source;
	}

	private void addDom(final Xpp3Dom parent, final String childName,
			final String childValue) {
		if (StringUtils.isNotEmpty(childValue)) {
			parent.addChild(newDom(childName, childValue));
		}
	}

	private Xpp3Dom newDom(final String name, final String value) {
		Xpp3Dom dom = new Xpp3Dom(name);
		dom.setValue(value);
		return dom;
	}

	public void cleanUp() {
		org.apache.commons.io.FileUtils.deleteQuietly(targetPath);
	}
}
