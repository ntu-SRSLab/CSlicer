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

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;

import cslicer.builder.BuildScriptInvalidException;

public class MavenInvokerDefault extends MavenInvoker {

	private String fTestClassDirPath;

	public MavenInvokerDefault(String script)
			throws BuildScriptInvalidException {
		super(script);
	}

	public MavenInvokerDefault(String script, boolean enableOutput)
			throws BuildScriptInvalidException {
		super(script, enableOutput);
	}

	public String getTestClassDirPath() {
		return fTestClassDirPath;
	}

	@Override
	public void initializeBuild(File targetPath)
			throws BuildScriptInvalidException, IOException {
		fBuilder = new MavenPomBuilder(scriptPath.toFile(), targetPath);
		fBuilder.addJunitPlugin();
		fBuilder.addSurefireBuildPlugin();
		fClassDirPath = fBuilder.redirectBuildOutput();
		fTestClassDirPath = fBuilder.redirectTestOutput();
		fBuilder.writeToPomFile(scriptPath.toString());
	}

	@Override
	public void initializeBuild(File targetPath, String subPomPath)
			throws BuildScriptInvalidException, IOException {
		fBuilder = new MavenPomBuilder(FileUtils.getFile(subPomPath),
				targetPath);
		fBuilder.addJunitPlugin();
		fBuilder.addSurefireBuildPlugin();
		fClassDirPath = fBuilder.redirectBuildOutput();
		fTestClassDirPath = fBuilder.redirectTestOutput();
		fBuilder.writeToPomFile(scriptPath.toString());
	}

}
