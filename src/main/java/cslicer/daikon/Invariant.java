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


public class Invariant {
	private String fullName;
	private String parentName;
	private String parentFullName;

	private int type;
	private int parentType;

	private int rank;

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getParentFullName() {
		return parentFullName;
	}

	public void setParentFullName(String parentFullName) {
		this.parentFullName = parentFullName;
	}

	public int getParentType() {
		return parentType;
	}

	public void setParentType(int parentType) {
		this.parentType = parentType;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != Invariant.class) {
			return false;
		}
		return this.fullName.equals(((Invariant) obj).getFullName())
				&& this.parentFullName
						.equals(((Invariant) obj).getParentFullName());
	}

	@Override
	public int hashCode() {
		return 100;
	}
}
