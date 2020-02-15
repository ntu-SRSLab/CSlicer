package cslicer.utils;

import java.util.Collection;

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

/**
 * Printing utilities.
 * 
 * @author Yi Li
 *
 */
public final class PrintUtils {

	private PrintUtils() {
	}

	private static boolean debugMessageSuppressed = false;

	public enum TAG {
		DEBUG, WARNING, STATS, OUTPUT
	}

	/**
	 * Print a message with given {@link TAG}.
	 * 
	 * @param message
	 *            message to print
	 * @param tag
	 *            TAG
	 */
	public static void print(final String message, final TAG tag) {
		if (tag != TAG.DEBUG || !debugMessageSuppressed) {
			if (tag == TAG.WARNING) {
				System.err.println("[" + tag + "] " + message);
			} else {
				System.out.println("[" + tag + "] " + message);
			}
		}
	}

	/**
	 * Print a message with given {@link TAG}.
	 * 
	 * @param message
	 *            message to print
	 * @param tag
	 *            {@link TAG}
	 */
	public static void print(final Object message, final TAG tag) {
		if (message == null)
			return;

		print(message.toString(), tag);
	}

	/**
	 * Print a message with given indentation level and {@link TAG}.
	 * 
	 * @param message
	 *            message to print
	 * @param indent
	 *            indentation level
	 * @param tag
	 *            {@link TAG}
	 */
	public static void print(final Object message, int indent, final TAG tag) {
		if (message == null)
			return;

		StringBuilder b = new StringBuilder();
		for (; indent > 0; indent--)
			b.append("\t");
		print(b.toString() + message.toString(), tag);
	}

	/**
	 * Print a message with given indentation level.
	 * 
	 * @param message
	 *            message to print
	 * @param indent
	 *            indentation level
	 */
	public static void print(final Object message, int indent) {
		print(message, indent, TAG.DEBUG);
	}

	/**
	 * Print a message with default indentation.
	 * 
	 * @param message
	 *            message to print
	 */
	public static void print(final Object message) {
		print(message, 0); // default
	}

	/**
	 * Print a collection of objects of type T.
	 * 
	 * @param collection
	 *            given collection of objects
	 * @param <T>
	 *            type of objects in collection
	 */
	public static <T> void print(Collection<T> collection) {
		StringBuilder b = new StringBuilder();
		b.append("{");
		int i = 0;
		for (T e : collection) {
			b.append(e.toString());
			if (++i < collection.size())
				b.append("\n");
		}
		b.append("}");
		print(b.toString());
	}

	/**
	 * Suppress debug messages in standard output.
	 */
	public static void supressDebugMessages() {
		debugMessageSuppressed = true;
	}

	/**
	 * Print a progress bar in standard output.
	 * 
	 * @param message
	 *            message to print
	 * @param percent
	 *            progress percentage
	 */
	public static void printProgress(final String message, final int percent) {
		// disable progress bar if in debug mode
		if (!debugMessageSuppressed)
			return;

		int boundP = Math.min(Math.max(0, percent), 100);

		StringBuilder bar = new StringBuilder("[");

		for (int i = 0; i < 50; i++) {
			if (i < (boundP / 2)) {
				bar.append("=");
			} else if (i == (boundP / 2)) {
				bar.append(">");
			} else {
				bar.append(" ");
			}
		}

		bar.append("]   " + boundP + "%");
		System.out.print("\r" + "[OUTPUT] " + message + bar.toString());
	}

	/**
	 * Output a single line break.
	 */
	public static void breakLine() {
		System.out.println();
	}
}
