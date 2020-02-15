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
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;

import cslicer.builder.BuildScriptInvalidException;

public class MavenInvokerDaikon extends MavenInvoker {

	private String fDaikonTrace;
	private String fDaikonJarPath;
	private String fChivoryJarPath;
	private Set<String> fDaikonIncludes;

	private String fTestClassDirPath;

	public MavenInvokerDaikon(String script, String daikonJar,
			String chivoryJar, Set<String> includes, boolean enableOutput)
			throws BuildScriptInvalidException {
		super(script, enableOutput);

		if (!FileUtils.fileExists(daikonJar))
			throw new BuildScriptInvalidException("Daikon Jar Not Found!");
		if (!FileUtils.fileExists(chivoryJar))
			throw new BuildScriptInvalidException("Chivory Jar Not Found!");

		fDaikonJarPath = daikonJar;
		fChivoryJarPath = chivoryJar;
		fDaikonIncludes = includes;
	}

	public File getDaikonTrace() {
		return FileUtils.getFile(fDaikonTrace);
	}

	public String getTestClassDirPath() {
		return fTestClassDirPath;
	}

	@Override
	public void initializeBuild(File targetPath)
			throws BuildScriptInvalidException, IOException {
		fBuilder = new MavenPomBuilder(scriptPath.toFile(), targetPath);
		fBuilder.addJunitPlugin();
		// fBuilder.addSurefireBuildPlugin();
		// add daikon argline
		fDaikonTrace = fBuilder.addDaikonSurefireBuild(fDaikonJarPath,
				fChivoryJarPath, fDaikonIncludes);
		fClassDirPath = fBuilder.redirectBuildOutput();
		fTestClassDirPath = fBuilder.redirectTestOutput();
		// fSourceJarPath = fBuilder.generageSourceJar();
		fBuilder.writeToPomFile(scriptPath.toString());

		// for common compress test
		if (fBuilder.getProjectArtifactId().equals("commons-compress"))
			commonCompressTestResources();
	}

	@Override
	public void initializeBuild(File targetPath, String subPomPath)
			throws BuildScriptInvalidException, IOException {
		fBuilder = new MavenPomBuilder(FileUtils.getFile(subPomPath),
				targetPath);
		fBuilder.addJunitPlugin();
		// fBuilder.addSurefireBuildPlugin();
		// add daikon argLine
		fDaikonTrace = fBuilder.addDaikonSurefireBuild(fDaikonJarPath,
				fChivoryJarPath, fDaikonIncludes);
		fClassDirPath = fBuilder.redirectBuildOutput();
		fTestClassDirPath = fBuilder.redirectTestOutput();
		// fSourceJarPath = fBuilder.generageSourceJar();
		fBuilder.writeToPomFile(subPomPath);

		// for common compress test
		if (fBuilder.getProjectArtifactId().equals("commons-compress"))
			commonCompressTestResources();
	}
}
