package cslicer;

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

import java.util.ResourceBundle;

/**
 * Static meta information about GitRef.
 *
 * @author Yi Li
 */
public final class CSlicer {

	/**
	 * Project name.
	 */
	public static final String PROJECT_NAME;

	/**
	 * Project version.
	 */
	public static final String PROJECT_VERSION;

	/**
	 * Time stamp at the time of current build.
	 */
	public static final String BUILD_TIMESTAMP;

	/**
	 * Detected system JAVA_HOME environment variable.
	 */
	public static final String SYSTEM_JAVA_HOME;

	/**
	 * Detected system MAVEN_HOME environment variable.
	 */
	public static final String SYSTEM_MAVEN_HOME;

	/**
	 * OS name.
	 */
	public static final String OS_NAME;

	/**
	 * OS version.
	 */
	public static final String OS_VERSION;

	/**
	 * OS architecture name.
	 */
	public static final String OS_ARCH;

	/**
	 * Build number. The Git revision number of HEAD.
	 */
	public static final String BUILD_NUMBER;

	/**
	 * Java version.
	 */
	public static final String JAVA_VERSION;

	/**
	 * Java vender.
	 */
	public static final String JAVA_VENDER;

	/**
	 * CSlicer logo.
	 */
	public static final String CSLICER_LOGO;

	/**
	 * Junit path.
	 */
	public static String JUNIT_JAR_PATH;

	static {
		final ResourceBundle bundle = ResourceBundle.getBundle("gitref");

		PROJECT_NAME = bundle.getString("project.name");
		PROJECT_VERSION = bundle.getString("project.version");
		BUILD_TIMESTAMP = bundle.getString("build.timestamp");
		SYSTEM_MAVEN_HOME = System.getenv("M2_HOME");
		SYSTEM_JAVA_HOME = System.getenv("JAVA_HOME");
		OS_NAME = bundle.getString("os.name");
		OS_VERSION = bundle.getString("os.version");
		OS_ARCH = bundle.getString("os.arch");
		BUILD_NUMBER = bundle.getString("build.number");
		JAVA_VERSION = bundle.getString("java.version");
		JAVA_VENDER = bundle.getString("java.vender");
		JUNIT_JAR_PATH = bundle.getString("junit.path");
		CSLICER_LOGO = "   ______ _____  __ _                  \n"
				+ "  / ____// ___/ / /(_)_____ ___   _____\n"
				+ " / /     \\__ \\ / // // ___// _ \\ / ___/\n"
				+ "/ /___  ___/ // // // /__ /  __// /\n"
				+ "\\____/ /____//_//_/ \\___/ \\___//_/\n";
	}
}
