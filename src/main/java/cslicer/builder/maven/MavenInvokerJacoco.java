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
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;

import cslicer.builder.BuildScriptInvalidException;

public class MavenInvokerJacoco extends MavenInvoker {

	private String execFilePath;

	public MavenInvokerJacoco(String script)
			throws BuildScriptInvalidException {
		super(script);
	}

	public MavenInvokerJacoco(String script, boolean enableOutput)
			throws BuildScriptInvalidException {
		super(script, enableOutput);
	}

	public final File getExecFile() {
		return FileUtils.getFile(this.execFilePath);
	}

	@Override
	public void initializeBuild(File targetPath)
			throws BuildScriptInvalidException, IOException {
		this.fBuilder = new MavenPomBuilder(scriptPath.toFile(), targetPath);
		// add jacoco and junit plugin and surefire plugin
		execFilePath = fBuilder.addJacocoBuildPlugin();
		fBuilder.addJunitPlugin();
		fBuilder.addSurefireBuildPlugin();
		// redirect build output
		fClassDirPath = fBuilder.redirectBuildOutput();
		// copy source files
		fSourceJarPath = fBuilder.generageSourceJar();
		// disable rat check plugin
		// builder.disableRatCheck();
		// write modifications to pom file
		fBuilder.writeToPomFile(scriptPath.toString());
	}

	public void initializeBuild(File targetPath, String subPomPath)
			throws BuildScriptInvalidException, IOException {
		this.fBuilder = new MavenPomBuilder(FileUtils.getFile(subPomPath),
				targetPath);
		execFilePath = fBuilder.addJacocoBuildPlugin();
		fBuilder.addJunitPlugin();
		fBuilder.addSurefireBuildPlugin();
		fClassDirPath = fBuilder.redirectBuildOutput();
		fSourceJarPath = fBuilder.generageSourceJar();
		// builder.disableRatCheck();

		fBuilder.writeToPomFile(subPomPath);

		// MavenPomBuilder rootPomBuilder = new MavenPomBuilder(
		// this.scriptPath.toFile());
		// rootPomBuilder.disableRatCheck();
		// rootPomBuilder.writeToPomFile(this.scriptPath.toString());
	}
}
