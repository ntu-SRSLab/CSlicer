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

import daikon.VarInfoName;
import daikon.VarInfoName.Field;
import daikon.VarInfoName.Slice;
import daikon.VarInfoName.Subscript;

public class DaikonFieldVisitor extends VarInfoName.AbstractVisitor<Field> {

	String field;

	public String getFieldName() {
		return field;
	}

	@Override
	public Field visitSubscript(Subscript o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field visitSlice(Slice o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field visitField(Field f) {
		field = f.field;
		return f;
	}

}
