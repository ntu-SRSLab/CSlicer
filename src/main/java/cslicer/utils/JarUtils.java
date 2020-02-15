package cslicer.utils;

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
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;

/**
 * Utils for operations on Java .jar files.
 * 
 * @author Yi Li
 *
 */
public final class JarUtils {

	private JarUtils() {
	}

	/**
	 * Unzip .jar file and maintain directory structure.O.
	 * 
	 * @param jarPath
	 *            path to .jar file
	 * @param destinationDir
	 *            {@link File} object representing destination directory
	 * @throws IOException
	 *             if I\O exception occurs during jar extraction
	 */
	public static void unzipJar(String jarPath, File destinationDir)
			throws IOException {
		JarFile jar = new JarFile(FileUtils.getFile(jarPath));

		// fist get all directories,
		// then make those directory on the destination Path
		for (Enumeration<JarEntry> enums = jar.entries(); enums
				.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();

			if (entry.isDirectory()) {
				FileUtils.getFile(destinationDir, entry.getName()).mkdirs();
			}
		}

		// now create all files
		for (Enumeration<JarEntry> enums = jar.entries(); enums
				.hasMoreElements();) {
			JarEntry entry = (JarEntry) enums.nextElement();

			if (!entry.isDirectory()) {
				InputStream is = jar.getInputStream(entry);
				FileUtils.copyInputStreamToFile(is,
						FileUtils.getFile(destinationDir, entry.getName()));
				is.close();
			}
		}

		// close jar
		jar.close();
	}
}
