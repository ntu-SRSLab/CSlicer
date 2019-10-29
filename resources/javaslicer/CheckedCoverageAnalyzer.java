package cslicer.coverage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.builder.BuildToolInvoker;

/*
 * #%L
 * CSlicer
 *    ______ _____  __ _                  
 *   / ____// ___/ / /(_)_____ ___   _____
 *  / /     \__ \ / // // ___// _ \ / ___/
 * / /___  ___/ // // // /__ /  __// /
 * \____/ /____//_//_/ \___/ \___//_/
 * %%
 * Copyright (C) 2014 - 2016 Department of Computer Science, University of Toronto
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

import cslicer.builder.UnitTestScope;
import cslicer.builder.maven.MavenInvoker;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.graph.Graph;
import cslicer.utils.graph.Vertex;
import de.unisb.cs.st.javaslicer.common.progress.ProgressMonitor;
import de.unisb.cs.st.javaslicer.slicing.Slicer;
import de.unisb.cs.st.javaslicer.slicing.SlicingCriterion;
import de.unisb.cs.st.javaslicer.slicing.StaticSlicingCriterion;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;

/**
 * Run Java Slicer trace reader and produce checked coverage results.
 * 
 * @author Yi Li
 *
 */
public class CheckedCoverageAnalyzer implements ICoverageAnalyzer {
	private Slicer fJavaSlicer;
	private Collection<String> slicingCreteria;
	private HashMap<String, InstructionSourceCodeEntity> map;

	private BuildToolInvoker fInvoker;
	private File fSourcePath;
	private File fClassPath;

	private List<TraceResult> traces;
	private final List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>(
			1);

	public CheckedCoverageAnalyzer(String filePath,
			Collection<String> slicingCriteria) {
		this(Collections.singleton(filePath), slicingCriteria);
	}

	public CheckedCoverageAnalyzer(Collection<String> traceFilePaths,
			Collection<String> slicingCriteria) {
		traces = new LinkedList<TraceResult>();
		for (String path : traceFilePaths) {
			try {
				traces.add(new TraceResult(new File(path)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.slicingCreteria = slicingCriteria;
		this.map = new HashMap<String, InstructionSourceCodeEntity>();
	}

	public CheckedCoverageAnalyzer(final BuildToolInvoker invoker,
			String filePath, Collection<String> slicingCriteria)
			throws BuildScriptInvalidException, CoverageControlIOException {
		this(filePath, slicingCriteria);
		initializeInvoker(invoker);
	}

	private void initializeInvoker(final BuildToolInvoker invoker)
			throws BuildScriptInvalidException, CoverageControlIOException {
		this.fInvoker = invoker;
		fClassPath = invoker.getClassPath();
	}

	public CheckedCoverageAnalyzer(MavenInvoker invoker,
			Set<String> javaSlicerDumpPath, Set<String> javaSlicerCriteria)
			throws BuildScriptInvalidException, CoverageControlIOException {
		this(javaSlicerDumpPath, javaSlicerCriteria);
		initializeInvoker(invoker);
	}

	@Override
	public CoverageDatabase analyseCoverage()
			throws CoverageControlIOException, TestFailureException {

		if (!fInvoker.checkCompilation())
			throw new TestFailureException(new UnitTestScope());

		CoverageDatabase cdb = new CoverageDatabase();

		Long threadId = null;

		// about param : sc
		List<SlicingCriterion> sc = null;

		for (TraceResult trace : traces) {

			for (String slicingCriterionString : this.slicingCreteria) {
				try {
					sc = StaticSlicingCriterion.parseAll(slicingCriterionString,
							trace.getReadClasses());
				} catch (IllegalArgumentException e) {
					PrintUtils.print("Error parsing slicing criterion: "
							+ e.getMessage(), TAG.WARNING);
					// System.exit(-1);
					// return null;
				}

				// about param : tracing
				List<ThreadId> threads = trace.getThreads();
				if (threads.size() == 0) {
					System.err.println(
							"The trace file contains no tracing information.");
					System.exit(-1);
				}

				ThreadId tracing = null;
				for (ThreadId t : threads) {
					if (threadId == null) {
						if ("main".equals(t.getThreadName()) && (tracing == null
								|| t.getJavaThreadId() < tracing
										.getJavaThreadId()))
							tracing = t;
					} else if (t.getJavaThreadId() == threadId.longValue()) {
						tracing = t;
					}
				}

				if (tracing == null) {
					System.err.println(threadId == null
							? "Couldn't find the main thread."
							: "The thread you specified was not found.");
					System.exit(-1);
					return null;
				}

				// about param : multithread
				boolean multithreaded;
				multithreaded = Runtime.getRuntime().availableProcessors() > 1;

				GenerateGraphVisitor visitor = new GenerateGraphVisitor();

				fJavaSlicer = new Slicer(trace);
				for (ProgressMonitor mon : this.progressMonitors) {
					fJavaSlicer.addProgressMonitor(mon);
				}
				fJavaSlicer.addSliceVisitor(visitor);

				try {
					if (sc != null) {
						fJavaSlicer.process(tracing, sc, multithreaded);
					}
				} catch (InterruptedException e) {
					if (fJavaSlicer == null) {
						System.out.println("fJavaSlicer = null");
					}
					// if (sc == null) {
					// System.out.println("sc == null");
					// }
				}

				Graph<InstructionSourceCodeEntity> graph = visitor.getGraph();

				for (Vertex<InstructionSourceCodeEntity> v : graph
						.getVerticies()) {

					if (this.map.containsKey(v.getName())) {
						InstructionSourceCodeEntity entity = this.map
								.get(v.getName());
						HashSet<Integer> lineNumberSet = entity
								.getLineNumberSet();
						for (int number : v.getData().getLineNumberSet()) {
							lineNumberSet.add(number);
						}
					} else {
						this.map.put(v.getName(), v.getData());
					}
				}
			}
		}

		for (String key : map.keySet()) {
			cdb.fullyCoveredMethodEntity.add(map.get(key));

			String className = map.get(key).getClassName();
			SourceRange range = null;
			SourceCodeEntity classEntity = new SourceCodeEntity(className,
					JavaEntityType.CLASS, 0, range);
			cdb.fullyCoveredClassEntity.add(classEntity);
		}

		return cdb;
	}

	@Override
	public CoverageDatabase analyseCoverage(UnitTestScope scope)
			throws CoverageControlIOException, TestFailureException {
		boolean success = fInvoker.runSingleTest(scope);

		try {
			fInvoker.restoreBuildFile();
		} catch (IOException e) {
			throw new CoverageControlIOException(
					"Error occour when restoring original POM file.", e);
		}

		if (!success)
			throw new TestFailureException(scope);

		return this.analyseCoverage();
	}

	@Override
	public COVERAGE_TYPE analysisType() {
		return COVERAGE_TYPE.CHECKED;
	}

}
