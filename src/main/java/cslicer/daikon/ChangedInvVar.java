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

import daikon.PptTopLevel;
import daikon.VarInfo;
import daikon.VarInfoName.Field;

public class ChangedInvVar {
	public enum InvVarType {
		FIELD_VALUE, METHOD_PRECOND, METHOD_POSTCOND, OTHER_TYPES
	}

	private InvVarType type;
	private String name;
	private VarInfo varInfo;
	private PptTopLevel ppt;
	private int argNum = -1;

	public ChangedInvVar(VarInfo varInfo, PptTopLevel ppt) {
		this.varInfo = varInfo;
		this.type = this.judgeVarType(varInfo);
		this.ppt = ppt;
		this.name = varInfo.str_name();
		if(this.name.startsWith("arg"))
		{
			this.argNum = Integer.parseInt(this.name.replace("arg", ""));
		}
	}

	public InvVarType judgeVarType(VarInfo varInfo) {
		PptTopLevel ppt = varInfo.ppt;
		if (ppt.ppt_name.isEnterPoint()) {
			if (varInfo.get_VarInfoName() instanceof Field
					|| varInfo.str_name().equals("this")) {
				return InvVarType.OTHER_TYPES;
			}
			return InvVarType.METHOD_PRECOND;
		} else if (ppt.ppt_name.isCombinedExitPoint()) {
			if (varInfo.get_VarInfoName().toString().startsWith("Field{")
					|| varInfo.str_name().equals("this")) {
				return InvVarType.OTHER_TYPES;
			}
			return InvVarType.METHOD_POSTCOND;
		} else if (ppt.ppt_name.isObjectInstanceSynthetic()
				|| ppt.ppt_name.isClassStaticSynthetic()) {
			return InvVarType.FIELD_VALUE;
		} else {
			return InvVarType.OTHER_TYPES;
		}
	}
	
	public boolean isArg()
	{
		if(this.argNum != -1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isReturn() {
		if (this.varInfo.get_VarInfoName().toString().equals("return")) {
			return true;
		}
		return false;
	}

	public int getArgNum() {
		return argNum;
	}

	public void setArgNum(int argNum) {
		this.argNum = argNum;
	}

	// if not postcondition, return -1
	// if is postcondition, return line number, or return -1 if there is no line
	// number in ppt.
	public int getExitLine() {
		if (this.type == InvVarType.METHOD_POSTCOND) {
			if (this.varInfo.ppt.toString().split(":::EXIT").length > 1) {
				int exitLine = Integer.parseInt(
						this.varInfo.ppt.toString().split(":::EXIT")[1]);
				return exitLine;
			}
		}
		return -1;
	}

	public PptTopLevel getPpt() {
		return ppt;
	}

	public void setPpt(PptTopLevel ppt) {
		this.ppt = ppt;
	}

	public VarInfo getVarInfo() {
		return varInfo;
	}

	public void setVarInfo(VarInfo varInfo) {
		this.varInfo = varInfo;
	}

	public InvVarType getType() {
		return type;
	}

	public void setType(InvVarType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.ppt + "\n" + this.varInfo.repr() + "\n" + this.getName()
				+ "\n" + this.getType();
	}
}
