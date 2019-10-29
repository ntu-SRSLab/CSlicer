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

import cslicer.utils.BytecodeUtils;

public class MethodNode extends CGNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MethodNode(String name, String signature, String className) {
		fQuantifiedName = BytecodeUtils.getQualifiedMethodName(className, name,
				signature);
	}

	@Override
	public String toString() {
		return "METHOD: " + fQuantifiedName;
	}

	@Override
	public boolean equals(Object node) {
		boolean same = false;

		if (node != null && node instanceof MethodNode) {
			same = this.toString().equals(node.toString());
		}
		return same;
	}
}
