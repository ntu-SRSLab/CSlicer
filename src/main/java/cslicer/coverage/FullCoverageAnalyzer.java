package cslicer.coverage;

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
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.BuildToolInvoker;
import cslicer.builder.UnitTestScope;
import cslicer.builder.maven.MavenInvokerJacoco;
import cslicer.utils.JarUtils;
import cslicer.utils.PrintUtils;

/**
 * Run Jacoco coverage Maven plugin and collect FULL coverage data.
 * 
 * @author Yi Li
 *
 */
public class FullCoverageAnalyzer implements ICoverageAnalyzer {

	private ExecutionDataStore fExecData;
	private File fExecFile;
	private File fExecFileExclude;
	private BuildToolInvoker fInvoker;
	private Collection<File> fSourcePath;
	private File fAutoGenSourcePath; // generated sources
	private File fClassPath;

	private boolean isExecFileProvided = false;
	private boolean isExecFileExcluded = false;

	/**
	 * @param invoker
	 *            used for Maven invocation
	 * @throws BuildScriptInvalidException
	 *             if build script is found invalid
	 * @throws CoverageControlIOException
	 *             if I/O exception occurs during coverage analysis
	 */
	public FullCoverageAnalyzer(final BuildToolInvoker invoker)
			throws BuildScriptInvalidException, CoverageControlIOException {
		fInvoker = invoker;
		fClassPath = invoker.getClassDirPath();

		assert invoker instanceof MavenInvokerJacoco;

		fExecFile = ((MavenInvokerJacoco) invoker).getExecFile();
	}

	/**
	 * Constructor.
	 * 
	 * @param execFile
	 *            path to Jacoco exec file
	 * @param sourcePath
	 *            path to source directory
	 * @param classPath
	 *            path to class directory
	 * @throws CoverageDataMissingException
	 *             if coverage data cannot be located
	 */
	public FullCoverageAnalyzer(final String execFile,
			final Collection<String> sourcePath, final String classPath)
			throws CoverageDataMissingException {
		fSourcePath = new HashSet<File>();
		for (String s : sourcePath) {
			if (!org.codehaus.plexus.util.FileUtils.fileExists(s))
				throw new CoverageDataMissingException("Invalid source path!");
			fSourcePath.add(FileUtils.getFile(s));
		}

		if (!org.codehaus.plexus.util.FileUtils.fileExists(execFile))
			throw new CoverageDataMissingException(
					"Coverage data is not found in the given path!");

		if (!org.codehaus.plexus.util.FileUtils.fileExists(classPath))
			throw new CoverageDataMissingException("Invalid class path!");

		fExecFile = FileUtils.getFile(execFile);
		fInvoker = null;
		fExecData = null;
		fClassPath = FileUtils.getFile(classPath);
		isExecFileProvided = true;
	}

	/**
	 * Constructor with two exec data dump files provided.
	 * 
	 * @param execFile1
	 *            path to first exec file
	 * @param execFile2
	 *            path to second exec file
	 * @param sourcePath
	 *            path to source directory
	 * @param classPath
	 *            path to class directory
	 * @throws CoverageDataMissingException
	 *             if coverage data cannot be located
	 */
	// public FullCoverageAnalyzer(final String execFile1, final String
	// execFile2,
	// final String sourcePath, final String classPath)
	// throws CoverageDataMissingException {
	// if (!org.codehaus.plexus.util.FileUtils.fileExists(sourcePath)
	// || !org.codehaus.plexus.util.FileUtils.fileExists(execFile1)
	// || !org.codehaus.plexus.util.FileUtils.fileExists(execFile2)
	// || !org.codehaus.plexus.util.FileUtils.fileExists(classPath))
	// throw new CoverageDataMissingException(
	// "Coverage data is not found in the given path!");
	//
	// fExecFile = FileUtils.getFile(execFile1);
	// fExecFileExclude = FileUtils.getFile(execFile2);
	// fInvoker = null;
	// fSourcePath = FileUtils.getFile(sourcePath);
	// fExecData = null;
	// fClassPath = FileUtils.getFile(classPath);
	// isExecFileProvided = true;
	// isExecFileExcluded = true;
	// }

	/**
	 * @param invoker
	 *            used for Maven invocation
	 * @param subPomFilePath
	 *            path to the sub-module POM file
	 * @throws BuildScriptInvalidException
	 *             if build script is found invalid
	 * @throws CoverageControlIOException
	 *             if I\O exception occurs during coverage analysis
	 */
	public FullCoverageAnalyzer(final BuildToolInvoker invoker,
			final String subPomFilePath)
			throws BuildScriptInvalidException, CoverageControlIOException {
		this.fInvoker = invoker;
		fClassPath = invoker.getClassDirPath();

		assert invoker instanceof MavenInvokerJacoco;

		fExecFile = ((MavenInvokerJacoco) invoker).getExecFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cslicer.coverage.ICoverageAnalyzer#analyseCoverage()
	 */
	@Override
	public final CoverageDatabase analyseCoverage()
			throws CoverageControlIOException, TestFailureException {
		if (isExecFileProvided) {
			return processCoverageResults();
		} else {
			// run unit tests
			boolean success = fInvoker.runUnitTests();

			try {
				fInvoker.restoreBuildFile();
			} catch (IOException e) {
				throw new CoverageControlIOException(
						"Error occour when restoring original POM file.", e);
			}

			if (!success)
				throw new TestFailureException(new UnitTestScope());

			return processCoverageResultsFromSourceJar(
					fInvoker.getSourceJarPath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cslicer.coverage.ICoverageAnalyzer#analyseCoverage(cslicer.builder.
	 * UnitTestScope)
	 */
	@Override
	public final CoverageDatabase analyseCoverage(final UnitTestScope scope)
			throws CoverageControlIOException, TestFailureException {
		if (scope.includeAllTest())
			return analyseCoverage();

		if (isExecFileProvided) {
			return processCoverageResults();
		}

		//fInvoker.checkCompilation();
		//fInvoker.compileTests();
		boolean success = fInvoker.runSingleTest(scope);

		try {
			fInvoker.restoreBuildFile();
		} catch (IOException e) {
			throw new CoverageControlIOException(
					"Error occour when restoring original POM file.", e);
		}

		if (!success)
			throw new TestFailureException(scope);

		return processCoverageResultsFromSourceJar(fInvoker.getSourceJarPath());
	}

	/**
	 * Process coverage results. Map line number to AST nodes.
	 * 
	 * @return {@link FullCoverageExtractor}
	 * @throws CoverageControlIOException
	 *             if I\O exception occurs during coverage analysis
	 */
	private CoverageDatabase processCoverageResultsFromSourceJar(String jarPath)
			throws CoverageControlIOException {
		extractSourceFromJar(jarPath);
		return processCoverageResults();
	}

	private CoverageDatabase processCoverageResults()
			throws CoverageControlIOException {
		ExecutionDataStore execData1 = loadExecutionData(fExecFile);
		// ExecutionDataStore execData2 = loadExecutionData(fExecFileExclude);

		FullCoverageExtractor res = computeCoverage(execData1);
		// res.exclude(computeCoverage(execData2));
		return res.getCoverageDatabase();
	}

	private FullCoverageExtractor computeCoverage(ExecutionDataStore execData)
			throws CoverageControlIOException {
		// analyze coverage data
		final CoverageBuilder builder = new CoverageBuilder();
		FullCoverageExtractor res = null;

		try {
			final Analyzer analyzer = new Analyzer(execData, builder);
			int numberOfClasses = analyzer.analyzeAll(fClassPath);
			PrintUtils.print(numberOfClasses + " classes have been analyzed.");

			Injector injector = Guice
					.createInjector(new CoverageDatabaseModule());
			res = injector.getInstance(FullCoverageExtractor.class);

			if (!isExecFileProvided)
				res.buildCoverageData(builder.getBundle("GITREFCOVERAGE"),
						fAutoGenSourcePath);
			else
				res.buildCoverageData(builder.getBundle("GITREFCOVERAGE"),
						fSourcePath);

		} catch (IOException e) {
			throw new CoverageControlIOException("Error occured when analyzing "
					+ fClassPath.getAbsolutePath(), e);
		} finally {
			if (!isExecFileProvided)
				// clean up if sources are auto-gen
				FileUtils.deleteQuietly(fAutoGenSourcePath);
		}

		return res;
	}

	// private CoverageDatabase computeCoverage2()
	// throws CoverageControlIOException {
	// CoverageDatabase res = null;
	// final CoverageBuilder builder = new CoverageBuilder();
	//
	// try {
	// final Analyzer analyzer = new Analyzer(fExecData, builder);
	// int numberOfClasses = analyzer.analyzeAll(fClassPath);
	// PrintUtils.print(numberOfClasses + " classes have been analyzed.");
	//
	// Injector injector = Guice
	// .createInjector(new CoverageDatabaseModule());
	// res = injector.getInstance(CoverageDatabase.class);
	// res.buildCoverageData(builder.getSourceFiles());
	// } catch (IOException e) {
	// throw new CoverageControlIOException(
	// "Error occured when analyzing "
	// + fClassPath.getAbsolutePath(), e);
	// } finally {
	// if (!isExecFileProvided)
	// // clean up if sources are auto-gen
	// FileUtils.deleteQuietly(fSourcePath);
	// }
	//
	// return res;
	// }

	private void extractSourceFromJar(String jarPath)
			throws CoverageControlIOException {
		// extract source jar file to a temporary folder
		try {
			fAutoGenSourcePath = org.eclipse.jgit.util.FileUtils
					.createTempDir("gitref", "-source-extracted", null);
			JarUtils.unzipJar(jarPath, fAutoGenSourcePath);
		} catch (IOException e2) {
			throw new CoverageControlIOException(
					"Source jar file in not found in "
							+ fInvoker.getSourceJarPath(),
					e2);
		}
	}

	private ExecutionDataStore loadExecutionData(File execFile)
			throws CoverageControlIOException {
		// load execution results
		ExecutionDataStore execData = null;
		try {
			final ExecFileLoader loader = new ExecFileLoader();
			loader.load(execFile);
			execData = loader.getExecutionDataStore();
		} catch (IOException e1) {
			throw new CoverageControlIOException(
					"Jacoco .exec file is not found!", e1);
		}

		return execData;
	}

	@Override
	public COVERAGE_TYPE analysisType() {
		return COVERAGE_TYPE.FULL;
	}
}
