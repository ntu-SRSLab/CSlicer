package cslicer.builder;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public abstract class BuildToolInvoker {
	protected final Path scriptPath;
	protected String fClassDirPath;
	protected String fSourceJarPath;

	public BuildToolInvoker(String script) throws BuildScriptInvalidException {
		scriptPath = Paths.get(script);
		if (!(Files.exists(scriptPath) && Files.isRegularFile(scriptPath)))
			throw new BuildScriptInvalidException();
	}

	public abstract boolean checkCompilation();

	public abstract void cleanUp();

	public abstract void writeBuildFile() throws IOException;

	public final File getClassDirPath() {
		return FileUtils.getFile(this.fClassDirPath);
	}

	public final String getSourceJarPath() {
		return this.fSourceJarPath;
	}

	public abstract void initializeBuild(File targetPath)
			throws IOException, BuildScriptInvalidException;

	public abstract void initializeBuild(File targetPath, String subPomPath)
			throws IOException, BuildScriptInvalidException;

	public abstract boolean runSingleTest(UnitTestScope scope);

	public abstract boolean runUnitTests();

	public abstract void restoreBuildFile() throws IOException;

	public abstract void compileTests();
}
