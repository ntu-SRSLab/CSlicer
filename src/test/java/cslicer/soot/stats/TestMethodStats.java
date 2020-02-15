package cslicer.soot.stats;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

import org.junit.Test;

import cslicer.soot.impact.LocalChangeImpactAnalysis;

public class TestMethodStats {

	@Test
	public void testElastic() throws Exception {
		String classPath = "/home/liyi/bit/gitref/target/classes:/home/liyi/.m2/repository/org/eclipse/jgit/org.eclipse.jgit/4.2.0.201601211800-r/org.eclipse.jgit-4.2.0.201601211800-r.jar:/home/liyi/.m2/repository/com/jcraft/jsch/0.1.53/jsch-0.1.53.jar:/home/liyi/.m2/repository/com/googlecode/javaewah/JavaEWAH/0.7.9/JavaEWAH-0.7.9.jar:/home/liyi/.m2/repository/org/apache/httpcomponents/httpclient/4.3.6/httpclient-4.3.6.jar:/home/liyi/.m2/repository/org/apache/httpcomponents/httpcore/4.3.3/httpcore-4.3.3.jar:/home/liyi/.m2/repository/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar:/home/liyi/.m2/repository/commons-codec/commons-codec/1.6/commons-codec-1.6.jar:/home/liyi/.m2/repository/org/slf4j/slf4j-api/1.7.2/slf4j-api-1.7.2.jar:/home/liyi/.m2/repository/commons-cli/commons-cli/1.3.1/commons-cli-1.3.1.jar:/home/liyi/.m2/repository/ch/uzh/ifi/seal/changedistiller/0.0.1-SNAPSHOT/changedistiller-0.0.1-SNAPSHOT.jar:/home/liyi/.m2/repository/com/google/inject/extensions/guice-assistedinject/3.0/guice-assistedinject-3.0.jar:/home/liyi/.m2/repository/com/google/inject/guice/3.0/guice-3.0.jar:/home/liyi/.m2/repository/javax/inject/javax.inject/1/javax.inject-1.jar:/home/liyi/.m2/repository/aopalliance/aopalliance/1.0/aopalliance-1.0.jar:/home/liyi/.m2/repository/org/eclipse/jdt/core/compiler/ecj/4.2.2/ecj-4.2.2.jar:/home/liyi/.m2/repository/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:/home/liyi/.m2/repository/commons-io/commons-io/2.4/commons-io-2.4.jar:/home/liyi/.m2/repository/org/apache/maven/shared/maven-invoker/2.2/maven-invoker-2.2.jar:/home/liyi/.m2/repository/org/codehaus/plexus/plexus-component-annotations/1.6/plexus-component-annotations-1.6.jar:/home/liyi/.m2/repository/org/apache/maven/maven-model/3.3.9/maven-model-3.3.9.jar:/home/liyi/.m2/repository/org/codehaus/plexus/plexus-utils/3.0.22/plexus-utils-3.0.22.jar:/home/liyi/.m2/repository/org/jacoco/org.jacoco.report/0.7.3.201502191951/org.jacoco.report-0.7.3.201502191951.jar:/home/liyi/.m2/repository/org/jacoco/org.jacoco.core/0.7.3.201502191951/org.jacoco.core-0.7.3.201502191951.jar:/home/liyi/.m2/repository/org/ow2/asm/asm-debug-all/5.0.1/asm-debug-all-5.0.1.jar:/home/liyi/.m2/repository/org/apache/bcel/bcel/5.2/bcel-5.2.jar:/home/liyi/.m2/repository/jakarta-regexp/jakarta-regexp/1.4/jakarta-regexp-1.4.jar:/home/liyi/.m2/repository/com/google/code/findbugs/jsr305/3.0.1/jsr305-3.0.1.jar:/home/liyi/.m2/repository/soot/soot/2.5.0/soot-2.5.0.jar:/home/liyi/.m2/repository/org/slf4j/slf4j-simple/1.7.19/slf4j-simple-1.7.19.jar:/home/liyi/.m2/repository/de/unisb/cs/st/javaslicer-core/1.1.1-SNAPSHOT/javaslicer-core-1.1.1-SNAPSHOT.jar:/home/liyi/.m2/repository/de/unisb/cs/st/javaslicer-traceReader/1.1.1-SNAPSHOT/javaslicer-traceReader-1.1.1-SNAPSHOT.jar:/home/liyi/.m2/repository/de/unisb/cs/st/javaslicer-common/1.1.1-SNAPSHOT/javaslicer-common-1.1.1-SNAPSHOT.jar:/home/liyi/.m2/repository/de/unisb/cs/st/sequitur/1.0/sequitur-1.0.jar:/home/liyi/.m2/repository/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar:/home/liyi/.m2/repository/de/hammacher/utilities/1.2.10/utilities-1.2.10.jar:/home/liyi/.m2/repository/soot/jasmin/2.5.0/jasmin-2.5.0.jar:/home/liyi/.m2/repository/soot/polyglot/1.3.5/polyglot-1.3.5.jar:/home/liyi/.m2/repository/org/apache/commons/commons-collections4/4.1/commons-collections4-4.1.jar:/home/liyi/.m2/repository/com/google/guava/guava/19.0/guava-19.0.jar:/home/liyi/.m2/repository/daikon/daikon/5.2.26/daikon-5.2.26.jar:/home/liyi/.m2/repository/plume/plume/1.0.0/plume-1.0.0.jar:/home/liyi/.m2/repository/getopt/getopt/1.0.8/getopt-1.0.8.jar";
		String classDirectory = "/home/liyi/bit/gitref/target/classes";

		LocalChangeImpactAnalysis lcia = new LocalChangeImpactAnalysis(
				classDirectory, classPath,
				new HashSet<>(Arrays
						.asList(new String[] { "cslicer.utils.graph.Graph",
								"cslicer.coverage.CoverageDatabase" })));

		List<String> methodNames = Arrays.asList(new String[] {
				"<cslicer.utils.graph.Graph: boolean addEdge(cslicer.utils.graph.Vertex,cslicer.utils.graph.Vertex,cslicer.utils.graph.EdgeLabel)>" });
		List<String> callNames = Arrays.asList(new String[] {
				"<cslicer.utils.graph.Graph: boolean addEdge(cslicer.utils.graph.Vertex,cslicer.utils.graph.Vertex,cslicer.utils.graph.EdgeLabel)>" });
		List<String> fieldNames = Arrays.asList(new String[] {
				"<cslicer.coverage.CoverageDatabase: java.util.Set fullyCoveredClassEntity>",
				// "<cslicer.analyzer.SlicingResult: int fFUNCCount>"
		});

		lcia.searchAnalysisTargets(callNames, methodNames, fieldNames);
		lcia.computeImpactAtCallSites();
	}
}
