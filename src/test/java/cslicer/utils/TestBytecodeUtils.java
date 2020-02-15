package cslicer.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

public class TestBytecodeUtils {

	@Test
	public void test() {
		PrintUtils.print(
				BytecodeUtils.getQualifiedClassName("[[Ljava/lang/Thread;"));
	}

	@Test
	public void testGenerics() {
		String method1 = "org.apache.commons.math4.ode.nonstiff.DormandPrince853FieldStepInterpolator.computeInterpolatedStateAndDerivatives(Rers<T>,T,T,T)";

		String method2 = "org.apache.commons.math4.ode.nonstiff.DormandPrince853FieldStepInterpolator.computeInterpolatedStateAndDerivatives(Rers<Rers>,Rers,Rers,Rers)";

		assertTrue(BytecodeUtils.matchWithGenericType(method1, method2));

		String method3 = "org.apache.commons.math4.ode.nonstiff.ThreeEighthesFieldStepInterpolator.ThreeEighthesFieldStepInterpolator(Field<T>,boolean,T[][],FieldODEStateAndDerivative<T>,FieldODEStateAndDerivative<T>,FieldODEStateAndDerivative<T>,FieldODEStateAndDerivative<T>,FieldEquationsMapper<T>)";
		String method4 = "org.apache.commons.math4.linear.RealVector.2.mapSubtract(double)";
		String method5 = "org.apache.commons.math4.ode.nonstiff.ThreeEighthesFieldStepInterpolator.ThreeEighthesFieldStepInterpolator(Field,boolean,T[][],FieldODEStateAndDerivative,FieldODEStateAndDerivative,FieldODEStateAndDerivative,FieldODEStateAndDerivative,FieldEquationsMapper)";

		assertTrue(BytecodeUtils.isMethodName(method4));
		assertTrue(BytecodeUtils.isMethodName(method3));
		assertTrue(BytecodeUtils.isMethodName(method5));

		assertTrue(BytecodeUtils.matchWithGenericType(method3, method5));

		String field1 = "lala.lala.x : List<T>";
		String field2 = "lala.lala.x : List<Integer>";

		assertTrue(BytecodeUtils.matchWithGenericType(field1, field2));

		String field3 = "lala.lala.x : K";
		String field4 = "lala.lala.x : Object";

		assertTrue(BytecodeUtils.matchWithGenericType(field3, field4));

		String method6 = "org.apache.commons.math4.ode.nonstiff.DormandPrince853FieldStepInterpolator.DormandPrince853FieldStepInterpolator(DormandPrince54FieldStepInterpolator)";
		String method7 = "org.apache.commons.math4.ode.nonstiff.DormandPrince853FieldStepInterpolator.DormandPrince853FieldStepInterpolator(Field<T>,boolean,T[][],FieldODEStateAndDerivative<T>,FieldODEStateAndDerivative<T>,FieldODEStateAndDerivative<T>,FieldODEStateAndDerivative<T>,FieldEquationsMapper<T>)";

		assertFalse(BytecodeUtils.matchWithGenericType(method6, method7));
	}

}
