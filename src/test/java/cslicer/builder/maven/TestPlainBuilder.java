package cslicer.builder.maven;

import java.util.Arrays;

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

import cslicer.builder.plain.PlainBuilder;
import cslicer.utils.PrintUtils;

public class TestPlainBuilder {

	@Test
	public void test() throws Exception {
		MavenInvoker invoker = new MavenInvokerDefault(
				"/home/liyi/bit/elasticsearch/pom.xml");

		PlainBuilder builder = new PlainBuilder(
				"/home/liyi/bit/elasticsearch/pom.xml");
		builder.fClassPath = invoker.getCompileClassPath();
		builder.fSourcePath = "/home/liyi/bit/elasticsearch/src/main/java:";
		builder.fTestClassPath = invoker.getTestClassPath();
		builder.fClassOutputPath = "/home/liyi/bit/elasticsearch/target/classes";
		builder.fTestClassOutputPath = "/tmp/test-classes";
		builder.fSourceFiles = Arrays.asList(new String[] {
				"/home/liyi/bit/elasticsearch/src/main/java/org/elasticsearch/script/groovy/GroovySandboxExpressionChecker.java" });

		PrintUtils.print(builder.checkCompilation());
	}

}
