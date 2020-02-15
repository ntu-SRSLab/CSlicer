package cslicer.builder.plain;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.BuildToolInvoker;
import cslicer.builder.UnitTestScope;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;

public class PlainBuilder extends BuildToolInvoker {

	public String fSourcePath;
	public String fTestSourcePath;
	public String fClassPath;
	public String fTestClassPath;

	public String fClassOutputPath;
	public String fTestClassOutputPath;

	public String fRepoPath;

	public List<String> fSourceFiles;
	public List<String> fTestFiles;

	public PlainBuilder(String script) throws BuildScriptInvalidException {
		super(script);
	}

	public PlainBuilder(String script, String fRepoPath)
			throws BuildScriptInvalidException {
		super(script);
		this.fRepoPath = fRepoPath;
	}

	@Override
	public boolean checkCompilation() {
		// TODO implement
		ProcessBuilder builder = new ProcessBuilder("python",
				scriptPath.toString(), "compile",
				fRepoPath.substring(0, fRepoPath.lastIndexOf("/")));
		builder.redirectErrorStream(true);

		Process p;
		int code = -1;

		try {
			p = builder.start();
			BufferedReader r = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String line;
			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				PrintUtils.print(line, TAG.DEBUG);
			}

			code = p.waitFor();

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return code == 0;
	}

	public boolean compileSources() {
		ProcessBuilder builder = new ProcessBuilder("javac", "-d",
				fClassOutputPath, "-classpath", fClassPath, "-sourcepath",
				fSourcePath, fileListToString(fSourceFiles).trim(), "-g",
				"-nowarn", "-target", "1.7", "-source", "1.7");
		builder.redirectErrorStream(true);
		PrintUtils.print(builder.command(), TAG.DEBUG);

		Process p;
		int code = -1;

		try {
			p = builder.start();
			BufferedReader r = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String line;
			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				PrintUtils.print(line, TAG.DEBUG);
			}

			code = p.waitFor();

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return code == 0;
	}

	public void compileTests() {
		// ProcessBuilder builder = new ProcessBuilder("javac", "-d",
		// fClassOutputPath, "-classpath", fTestClassPath, "-sourcepath",
		// fTestSourcePath, fileListToString(fTestFiles), "-g", "-nowarn",
		// "-target", "1.7", "-source", "1.7");
		// builder.redirectErrorStream(true);
		// Process p;
		// try {
		// p = builder.start();
		// BufferedReader r = new BufferedReader(
		// new InputStreamReader(p.getInputStream()));
		// String line;
		// while (true) {
		// line = r.readLine();
		// if (line == null) {
		// break;
		// }
		// System.out.println(line);
		// }
		//
		// return p.exitValue() == 0;
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// return false;
		return;
	}

	private String fileListToString(List<String> list) {
		StringBuilder builder = new StringBuilder();
		if (list == null)
			return "";

		for (String s : list) {
			builder.append(s);
			builder.append(" ");
		}
		return builder.toString();
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeBuild(File targetPath)
			throws IOException, BuildScriptInvalidException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeBuild(File targetPath, String subPomPath)
			throws IOException, BuildScriptInvalidException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean runSingleTest(UnitTestScope scope) {
		ProcessBuilder builder = new ProcessBuilder("python",
				scriptPath.toString(), "test",
				fRepoPath.substring(0, fRepoPath.lastIndexOf("/")));
		builder.redirectErrorStream(true);

		Process p;
		int code = -1;

		try {
			p = builder.start();
			BufferedReader r = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String line;
			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				PrintUtils.print(line, TAG.DEBUG);
			}

			code = p.waitFor();

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return code == 0;
	}

	@Override
	public boolean runUnitTests() {
		return false;
	}

	@Override
	public void restoreBuildFile() throws IOException {
		return;
	}

	@Override
	public void writeBuildFile() throws IOException {
		return;
	}

}
