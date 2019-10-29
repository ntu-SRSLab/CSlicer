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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import cslicer.CSlicer;
import cslicer.analyzer.Refiner.SCHEME;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.callgraph.ClassPathInvalidException;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.coverage.TestFailureException;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CheckoutBranchFailedException;
import cslicer.jgit.CheckoutFileFailedException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.StatsUtils;

/**
 * Main entry for the GitRef tool.
 * 
 * @author Yi Li
 *
 */
public class Main {

	public static void main(String[] args) {

		System.out.println("===== Git History Slicing Toolkit =====");
		System.out.println(CSlicer.CSLICER_LOGO);
		System.out.println("=======================================");

		// check environment
		if (!checkEnvVars()) {
			PrintUtils.print("Environment not setup properly. Abort.", TAG.WARNING);
			System.exit(1);
		}

		// help and info options
		Options options1 = new Options();
		options1.addOption("h", "help", false, "Print help messages.");
		options1.addOption("i", "info", false, "Display version information.");

		// required options
		Options options2 = new Options();
		Option c = new Option("c", "config", true, "Path to project configuration file.");
		Option e = new Option("e", "engine", true, "Select slicing engine: [slicer|refiner|delta|srr|hunker].");
		c.setRequired(true);
		e.setRequired(true);
		options2.addOption(c);
		options2.addOption(e);

		// optional options
		Option d = new Option("d", "diff", true, "Show AST diff of a commit.");
		Option p = new Option("p", "print", false, "Output hunk dependency graph.");
		Option n = new Option("n", "nopick", false, "Skip cherry-picking.");
		Option t = new Option("t", "test", false, "Verify picking and/or testing result.");
		Option s = new Option("s", "short", false, "Try history slice shorten.");
		Option q = new Option("q", "quiet", false, "No debug output.");
		Option v = new Option("v", "verbose", false, "Verbose mode.");
		Option l = new Option("l", "learn", true,
				"Select significance learning schemes: [default|noinv|nolearn|noprob|noinit|nocomp|combined|low3|neg|nonpos].");
		Option i = new Option("i", "intersection", false, "Show the intersection result.");
		Option j = new Option("j", "savetojson", false, "Save result to json file.");

		d.setRequired(false);
		p.setRequired(false);
		n.setRequired(false);
		t.setRequired(false);
		s.setRequired(false);
		q.setRequired(false);
		v.setRequired(false);
		l.setRequired(false);
		i.setRequired(false);
		j.setRequired(false);

		options2.addOption(d);
		options2.addOption(t);
		options2.addOption(p);
		options2.addOption(s);
		options2.addOption(q);
		options2.addOption(v);
		options2.addOption(n);
		options2.addOption(l);
		options2.addOption(i);
		options2.addOption(j);

		try {
			CommandLine c1 = new DefaultParser().parse(options1, args, true);

			if (c1.getOptions().length > 0) {
				if (c1.hasOption("info")) {
					displayVersionInfo();
				}

				if (c1.hasOption("help")) {
					// automatically generate the help statement
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("cslicer -c <CONFIG_FILE> -e <SLICING_ENGINE>", options2);
				}

			} else {
				CommandLine line = new DefaultParser().parse(options2, args);

				Path configPath = Paths.get(line.getOptionValue("config"));

				if (!FileUtils.fileExists(configPath.toString())
						|| !FilenameUtils.getExtension(configPath.toString()).equals("properties")) {
					PrintUtils.print("The specified project configuration file path is not valid!", TAG.WARNING);
					System.exit(1);
				}

				if (line.hasOption("quiet"))
					PrintUtils.supressDebugMessages();

				StatsUtils.resume("total.time");

				ProjectConfiguration config = new ProjectConfiguration(configPath);
				config.setOutputHunkGraph(line.hasOption("print"));
				config.setEnableBuilderOutput(line.hasOption("verbose"));
				config.setSkipPicking(line.hasOption("nopick"));

				config.setEnableIntersection(line.hasOption("intersection"));
				config.setEnableJson(line.hasOption("savetojson"));

				if (line.getOptionValue("engine").equals("slicer")) {
					invokeSlicer(line, config);
				} else if (line.getOptionValue("engine").equals("refiner")) {
					invokeRefiner(line, config);
				} else if (line.getOptionValue("engine").equals("delta")) {
					invokePlainDD(line, config);
				} else if (line.getOptionValue("engine").equals("srr")) {
					invokeSRR(line, config);
				} else if (line.getOptionValue("engine").equals("hunker")) {
					invokeHunker(line, config);
				} else if (line.getOptionValue("engine").equals("metrics")) {
					invokeMetricsCollector(line, config);
				} else {
					PrintUtils.print("Invalid engine name!");
					System.exit(1);
				}

				StatsUtils.stop("total.time");
				StatsUtils.print();
			}

		} catch (ParseException | IOException e1) {
			PrintUtils.print(e1.getMessage(), TAG.WARNING);
			System.exit(0);
		}
	}

	private static void invokeSRR(CommandLine line, ProjectConfiguration config) {
		try {
			SCHEME partitionScheme = SCHEME.COMBINED;
			config.setEnableLearning(true);

			RRefiner srr = new RRefiner(config);
			List<RevCommit> res = srr.refineSlice(partitionScheme);

			if (line.hasOption("test"))
				srr.verifyOneMinimal(res);

			srr.cleanUp();
		} catch (RepositoryInvalidException | CommitNotFoundException | BuildScriptInvalidException
				| CoverageControlIOException | AmbiguousEndPointException | ProjectConfigInvalidException
				| BranchNotFoundException | CoverageDataMissingException | CheckoutBranchFailedException
				| ClassPathInvalidException | CheckoutFileFailedException | IOException | CompilationFailureException
				| TestFailureException e) {
			e.printStackTrace();
		}
	}

	private static void invokeHunker(CommandLine line, ProjectConfiguration config) {
		try {
			Hunker debugger = new Hunker(config);
			PrintUtils.print("COMPUTING HUNK FACTS..."); // dependency facts
			PrintUtils.print(debugger.generateHunkDependencyFacts());
			PrintUtils.print("COMPUTING DEPS FACTS..."); // dependency facts
			debugger.generateDependencyFacts(); // dependency facts
			PrintUtils.print("COMPUTING DIFF FACTS..."); // diff facts
			debugger.generateDifferentialFacts();
		} catch (IOException | RepositoryInvalidException | CommitNotFoundException | BuildScriptInvalidException
				| CoverageControlIOException | AmbiguousEndPointException | ProjectConfigInvalidException
				| BranchNotFoundException | CoverageDataMissingException | ClassPathInvalidException e) {
			e.printStackTrace();
		}
	}

	private static void invokeMetricsCollector(CommandLine line, ProjectConfiguration config) {
		try {
			MetricsCollector collector = new MetricsCollector(config);
			SlicingResult result = collector.doSlicing();

		} catch (IOException | RepositoryInvalidException | CommitNotFoundException | BuildScriptInvalidException
				| CoverageControlIOException | AmbiguousEndPointException | ProjectConfigInvalidException
				| BranchNotFoundException | CoverageDataMissingException e) {
			e.printStackTrace();
		}
	}

	private static void invokePlainDD(CommandLine line, ProjectConfiguration config) {
		try {
			DeltaDebugger debugger = new DeltaDebugger(config);
			List<RevCommit> res = debugger.doSlicing(SCHEME.COMBINED);
		} catch (IOException | RepositoryInvalidException | CommitNotFoundException | BuildScriptInvalidException
				| CoverageControlIOException | AmbiguousEndPointException | ProjectConfigInvalidException
				| BranchNotFoundException | CoverageDataMissingException | CheckoutBranchFailedException e) {
			e.printStackTrace();
		}
	}

	private static void invokeSlicer(CommandLine line, ProjectConfiguration config) {
		try {
			Slicer refactor = new Slicer(config);

			if (line.hasOption("diff")) {
				// show AST diff of a commit
				refactor.showASTDiff(line.getOptionValue("diff"));
			} else if (line.hasOption("test")) {
				// verify slicing results loaded from file can be
				// cherry-picked
				refactor.verifyResultPicking(refactor.loadSlicingResult().getPicks());
			} else {
				SlicingResult result = refactor.doSlicing();
				// cache slicing result to file
				refactor.saveSlicingResult();
				// try shorten the slice
				if (line.hasOption("short"))
					refactor.shortenSlice(result);
			}

			refactor.cleanUp();

		} catch (IOException e) {
			PrintUtils.print("Project configuration file is missing!", TAG.WARNING);
			e.printStackTrace();
			System.exit(3);
		} catch (ProjectConfigInvalidException e) {
			PrintUtils.print("Project configuration file is not valid!", TAG.WARNING);
			e.printStackTrace();
			System.exit(4);
		} catch (ClassNotFoundException | CommitNotFoundException | BuildScriptInvalidException
				| CoverageDataMissingException | CoverageControlIOException | RepositoryInvalidException
				| AmbiguousEndPointException | BranchNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void invokeRefiner(CommandLine line, ProjectConfiguration config) {
		try {
			SCHEME partitionScheme = SCHEME.COMBINED;

			if (line.hasOption("learn")) {
				String args = line.getOptionValue("learn");
				List<String> argSet = Arrays.asList(args.split(","));

				config.setEnableInvariant(true);
				config.setEnableLearning(true);
				config.setEnableInitRank(true);

				if (argSet.contains("noinv"))
					config.setEnableInvariant(false);
				if (argSet.contains("nolearn"))
					config.setEnableLearning(false);
				if (argSet.contains("noinit"))
					config.setEnableInitRank(false);
				if (argSet.contains("nocomp"))
					config.setEnableCompCheck(false);
				if (argSet.contains("noprob"))
					config.setEnableProbablistic(false);

				if (argSet.contains("low3"))
					partitionScheme = SCHEME.LOWER_3;
				else if (argSet.contains("neg"))
					partitionScheme = SCHEME.NEGATIVE;
				else if (argSet.contains("nonpos"))
					partitionScheme = SCHEME.NON_POSITIVE;

			}

			Refiner refiner = new Refiner(config);
			List<RevCommit> res = refiner.refineSlice(partitionScheme);

			if (line.hasOption("test"))
				refiner.verifyOneMinimal(res);

			refiner.cleanUp();

		} catch (RepositoryInvalidException | CommitNotFoundException | BuildScriptInvalidException
				| CoverageControlIOException | AmbiguousEndPointException | ProjectConfigInvalidException
				| BranchNotFoundException | CoverageDataMissingException | CheckoutBranchFailedException
				| ClassPathInvalidException | CheckoutFileFailedException | IOException | CompilationFailureException
				| TestFailureException e) {
			e.printStackTrace();
		}
	}

	private static void displayVersionInfo() throws IOException {

		System.out.println(CSlicer.PROJECT_NAME + " " + CSlicer.PROJECT_VERSION + " (" + CSlicer.BUILD_NUMBER + "; "
				+ CSlicer.BUILD_TIMESTAMP + ")");
		System.out.println("Maven home: " + CSlicer.SYSTEM_MAVEN_HOME);
		System.out.println("Java home: " + CSlicer.SYSTEM_JAVA_HOME);
		System.out.println("Built on: " + CSlicer.OS_NAME + ", " + CSlicer.OS_VERSION + ", " + CSlicer.OS_ARCH);
		System.out.println("Java version: " + CSlicer.JAVA_VERSION + ", " + CSlicer.JAVA_VENDER);
	}

	private static boolean checkEnvVars() {
		if (System.getenv("JAVA_HOME") == null) {
			PrintUtils.print("Variable 'JAVA_HOME' is not set", PrintUtils.TAG.WARNING);
			return false;
		} else if (System.getenv("M2_HOME") == null) {
			PrintUtils.print("Variable 'M2_HOME' is not set", PrintUtils.TAG.WARNING);
			return false;
		}

		return true;
	}
}
