package cslicer.daikon;

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


public class InvariantParentType {
	// parent type
	public static final int CLASS = 100;
	public static final int METHOD = 101;
	public static final int OBJECT = 102;

	public static boolean isUninterestingParent(String entityString) {
		if (entityString.startsWith("org.junit."))
			return true;
		if (entityString.startsWith("com.sun."))
			return true;
		if (entityString.startsWith("junit."))
			return true;

		return false;
	}

	public static String parentTypeToString(int parentType) {
		switch (parentType) {
		case InvariantParentType.CLASS:
			return "CLASS";
		case InvariantParentType.METHOD:
			return "METHOOD";
		case InvariantParentType.OBJECT:
			return "OBJECT";
		default:
			return "OTHER";
		}
	}

	public static int getParentTypeFromFirstInvariant(String firstInvariant) {
		if (firstInvariant.endsWith("CLASS")) {
			return InvariantParentType.CLASS;
		} else if (firstInvariant.endsWith("OBJECT")) {
			return InvariantParentType.OBJECT;
		} else {
			return InvariantParentType.METHOD;
		}
	}
}
