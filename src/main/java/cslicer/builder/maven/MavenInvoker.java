package cslicer.builder.maven;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;

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
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;

import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.BuildToolInvoker;
import cslicer.builder.UnitTestScope;
import cslicer.utils.PrintUtils;

public abstract class MavenInvoker extends BuildToolInvoker {
	protected final Invoker fInvoker;
	protected MavenPomBuilder fBuilder;

	// private final Path workingDirectory;
	public MavenInvoker(String script) throws BuildScriptInvalidException {
		this(script, false);
	}

	public MavenInvoker(String script, boolean enableOutput)
			throws BuildScriptInvalidException {
		super(script);

		this.fInvoker = new DefaultInvoker();
		if (!enableOutput)
			this.fInvoker.setOutputHandler(null);
	}

	/**
	 * Check if a target project compiles successfully.
	 * 
	 * @return {@code true} if target project compiles
	 */
	@Override
	public boolean checkCompilation() {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(scriptPath.toFile());
		request.setInteractive(false);
		request.setGoals(Arrays.asList("compiler:compile"));

		try {
			InvocationResult res = fInvoker.execute(request);

			if (res.getExitCode() != 0) {
				PrintUtils.print("Compilation failed");
				return false;
			} else {
				PrintUtils.print("Compilation is successful");
				return true;
			}
		} catch (MavenInvocationException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/**
	 * Clean up build files.
	 */
	@Override
	public void cleanUp() {
		// cleaning up
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(scriptPath.toFile());
		request.setGoals(Arrays.asList("clean"));

		try {
			fInvoker.execute(request);
		} catch (MavenInvocationException e1) {
			e1.printStackTrace();
		}

		if (fBuilder != null)
			fBuilder.cleanUp();
	}

	public void compileTests() {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(scriptPath.toFile());
		request.setInteractive(false);
		request.setGoals(Arrays.asList("compiler:testCompile"));
		try {
			fInvoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}
	}

	protected void commonCompressTestResources() {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(scriptPath.toFile());
		request.setInteractive(false);
		request.setGoals(
				Arrays.asList("-P", "run-zipit", "process-test-resources"));
		try {
			fInvoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}
	}

	private String getProjectClassPath(String scope) {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(scriptPath.toFile());
		request.setInteractive(false);
		request.setGoals(Arrays.asList("dependency:build-classpath",
				"-DincludeScope=" + scope));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStreamHandler output = new PrintStreamHandler(
				new PrintStream(baos), true);
		request.setOutputHandler(output);

		try {
			fInvoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}

		String res = "";
		// class path not start with [INFO]
		try (BufferedReader buf = new BufferedReader(
				new StringReader(baos.toString()))) {
			while ((res = buf.readLine()) != null) {
				if (!res.startsWith("[INFO]"))
					return res;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}

	public String getTestClassPath() {
		return getProjectClassPath("test");
	}

	public String getCompileClassPath() {
		return getProjectClassPath("compile");
	}

	@Override
	public abstract void initializeBuild(File targetPath)
			throws BuildScriptInvalidException, IOException;

	@Override
	public abstract void initializeBuild(File targetPath, String subPomPath)
			throws BuildScriptInvalidException, IOException;

	@Override
	public void restoreBuildFile() throws IOException {
		if (fBuilder == null)
			return;
		fBuilder.restorePomFile();
	}

	public void writeBuildFile() throws IOException {
		if (fBuilder == null)
			return;
		fBuilder.writeToPomFile(scriptPath.toString());
	}

	@Override
	public boolean runSingleTest(UnitTestScope scope) {
		InvocationRequest request = new DefaultInvocationRequest();
		InvocationRequest request1 = new DefaultInvocationRequest();

		request1.setPomFile(scriptPath.toFile()).setInteractive(false).setGoals(
				Arrays.asList("test", scope.getMavenTestArguments(true)));
		// System.out.println(scope.getMavenTestArguments());
		request.setPomFile(scriptPath.toFile()).setInteractive(false)
				.setGoals(Arrays.asList("surefire:test",
						scope.getMavenTestArguments(true)));

		try {
			InvocationResult res1 = fInvoker.execute(request1);
			InvocationResult res = fInvoker.execute(request);

			if (res1.getExitCode() != 0 || res.getExitCode() != 0) {
				PrintUtils.print("Test failed");
				return false;
			} else {
				PrintUtils.print("Test succeeded");
				return true;
			}
		} catch (MavenInvocationException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/**
	 * Run all default unit tests.
	 * 
	 * @return {@code true} if run successfully
	 */
	@Override
	public boolean runUnitTests() {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(scriptPath.toFile());
		request.setInteractive(false);
		request.setGoals(Arrays.asList("test"));

		try {
			InvocationResult res = fInvoker.execute(request);

			if (res.getExitCode() != 0) {
				PrintUtils.print("Test failed");
				return false;
			} else {
				PrintUtils.print("Test succeeded");
				return true;
			}
		} catch (MavenInvocationException e1) {
			e1.printStackTrace();
		}
		return false;
	}
}
