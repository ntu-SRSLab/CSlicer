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

import cslicer.utils.graph.EdgeLabel;

public enum CGEdgeType implements EdgeLabel {
	FIELD_READ("FRd"), FIELD_WRITE("FWt"), STATIC_READ("SRd"), STATIC_WRITE(
			"SWt"), FIELD_REFERENCE("FRf"), CLASS_REFERENCE("CRf"), CLASS_FIELD(
			"C-F"), CLASS_METHOD("C-M"), INVOKE_STATIC("ISt"), INVOKE_VIRTUAL(
			"IVi"), INVOKE_SPECIAL("ISp"), INVOKE_INTERFACE("IIn"), DEFAULT("default");

	private String fName;

	private CGEdgeType(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}
}
