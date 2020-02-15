package cslicer.callgraph;

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

import java.util.Arrays;

import org.junit.Test;

public class StaticCallGraphTest {

	@Test
	public void testWrite() throws ClassPathInvalidException {
		StaticCallGraphBuilder builder = new BcelStaticCallGraphBuilder(
				Arrays.asList("/home/liyi/bit/gitref/target/classes"), null);

		builder.buildCallGraph();
		builder.getCallGraph().printCallGraph();
		// builder.saveCallGraph("/tmp/callgraph.ser");
	}

	@Test
	public void testRead() throws Exception {
		// StaticCallGraphBuilder builder = new SootStaticCallGraphBuilder(
		// "/home/liyi/bit/gitref/target/classes");
		// builder.loadCallGraph("/tmp/lastCallGraph.ser");
		// builder.getCallGraph().printCallGraph();
	}
}
