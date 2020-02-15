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

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import cslicer.daikon.ChangedInvVar;
import cslicer.daikon.DaikonFieldVisitor;

public class BytecodeUtils {

	private final static String GENERIC_FILTER = "<[\\p{L}][\\p{L}\\p{N}]*>";
	private final static String CLASS_PATTERN = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*";
	private final static String METHOD_PATTERN = CLASS_PATTERN
			+ "\\(([a-zA-Z_$][a-zA-Z\\d_$]*\\,)*[a-zA-Z_$][a-zA-Z\\d_$]*\\)";
	private final static String METHOD_PATTERN_NOP = CLASS_PATTERN + "\\(\\)";
	private final static String JAVA_LIB_EXCLUDE = "java.*|org.junit.*";
	private final static String SUN_LIB_EXCLUDE = "com.sun.*|sun.*";

	public static String getQualifiedClassName(final String vmname) {
		Type object = Type.getObjectType(vmname);
		return object.getClassName().replace('/', '.').replace('$', '.');
	}

	public static String getQualifiedFieldName(String fieldName,
			String className, String fieldType) {
		return getQualifiedClassName(className) + "." + fieldName + " : "
				+ getShortClassName(fieldType);
	}

	public static String getQualifiedMethodName(final String vmclassname,
			final String vmmethodname, final String vmdesc) {
		return getQualifiedClassName(vmclassname) + "."
				+ getMethodName(vmclassname, vmmethodname, vmdesc, false);
	}

	public static String filterGenericType(String astName) {
		return astName.replaceAll(GENERIC_FILTER, StringUtils.EMPTY);
	}

	/**
	 * Test if two signature matches considering generic.
	 * 
	 * @param sig1
	 *            signature possibly with generic types
	 * @param sig2
	 *            signature without generic types
	 * @return {@code true} if two signature matches
	 */
	public static boolean matchWithGenericType(final String sig1,
			final String sig2) {
		if (sig1.equals(sig2))
			return true;

		String sigcp1 = filterGenericType(sig1);
		String sigcp2 = filterGenericType(sig2);

		if (sigcp1.equals(sigcp2))
			return true;

		if (isFieldName(sigcp1) && isFieldName(sigcp2)) {
			String cn1 = sigcp1.substring(0, sigcp1.indexOf(":")).trim();
			String cn2 = sigcp2.substring(0, sigcp2.indexOf(":")).trim();

			return cn1.equals(cn2);
		}

		if (isMethodName(sigcp1) && isMethodName(sigcp2)) {
			String cn1 = sigcp1.substring(0, sigcp1.indexOf("("));
			String cn2 = sigcp2.substring(0, sigcp2.indexOf("("));

			if (!cn1.equals(cn2))
				return false;

			String par1 = sigcp1.substring(sigcp1.indexOf("("));
			String par2 = sigcp2.substring(sigcp2.indexOf("("));

			return par1.split(",").length == par2.split(",").length;
		}

		return false;
	}

	private static boolean isFieldName(String sigcp1) {
		return sigcp1.contains(" : ");
	}

	public static boolean isClassName(String key) {
		return key.matches(CLASS_PATTERN);
	}

	public static boolean isMethodName(String key) {
		// return key.matches(METHOD_PATTERN) ||
		// key.matches(METHOD_PATTERN_NOP);
		return key.indexOf("(") > 0 && key.indexOf(")") > 0;
	}

	public static boolean matchExclude(String key) {
		if (key.matches(JAVA_LIB_EXCLUDE))
			return true;
		if (key.matches(SUN_LIB_EXCLUDE))
			return true;
		return false;
	}

	public static String getQualifiedMethodNameFromDaikon(ChangedInvVar var) {

		String daikonMethodSignature = var.getVarInfo().ppt.toString();
		daikonMethodSignature = daikonMethodSignature.replace(" ", "");
		String methodName = daikonMethodSignature.split(":::")[0];
		methodName = methodName.substring(0, methodName.indexOf("("));
		String argString = daikonMethodSignature.substring(
				daikonMethodSignature.indexOf("(") + 1,
				daikonMethodSignature.indexOf(")"));
		String[] args = argString.split(",");
		String newSignature = methodName + "(";
		for (int i = 0; i < args.length; i++) {
			String arg = getShortClassName(args[i]);
			newSignature += arg;
			if (i == args.length - 1) {
				newSignature += ")";
			} else {
				newSignature += ",";
			}
		}
		// System.out.println(var.getVarInfo().repr());
		// System.out.println("[MEYHOD]: " + newSignature);
		return newSignature;
	}

	public static String getQualifiedFieldNameFromDaikon(ChangedInvVar var) {
		String pptName = var.getVarInfo().ppt.toString().split(":::")[0];
		String pptClassName = "";
		if (var.getVarInfo().ppt.toString().split(":::")[1].equals("CLASS")
				|| var.getVarInfo().ppt.toString().split(":::")[1]
						.equals("OBJECT")) {
			pptClassName = pptName;
		} else {
			pptClassName = pptName.substring(0, pptName.lastIndexOf('('));
			pptClassName = pptClassName.substring(0,
					pptClassName.lastIndexOf('.'));
		}

		// String daikonFieldRepr =
		// var.getVarInfo().get_VarInfoName().toString();
		// PrintUtils.print(var.getVarInfo().get_VarInfoName().identifier_name());
		// PrintUtils.print(var.getVarInfo().get_VarInfoName().name());

		DaikonFieldVisitor visitor = new DaikonFieldVisitor();
		var.getVarInfo().get_VarInfoName().accept(visitor);
		String fieldName = pptClassName + "." + visitor.getFieldName();

		// String fieldName = "";
		// if (daikonFieldRepr.equals("this")) {
		// fieldName = pptClassName + "." + daikonFieldRepr;
		// } else {
		//
		// fieldName = pptClassName + "."
		// + daikonFieldRepr.substring(
		// daikonFieldRepr.indexOf('{') + 1,
		// daikonFieldRepr.indexOf('}'));
		// }
		String fullTypeName = var.getVarInfo().type.toString();
		String shortTypeName = getShortClassName(fullTypeName);
		// System.out.println(var.getVarInfo().repr());
		// System.out.println("[FIELD]: " + fieldName + " : " + shortTypeName);
		return fieldName + " : " + shortTypeName;
	}

	private static String getMethodName(final String vmclassname,
			final String vmmethodname, final String vmdesc,
			boolean qualifiedParams) {

		if ("<clinit>".equals(vmmethodname)) {
			return "static {...}";
		}
		final StringBuilder result = new StringBuilder();
		if ("<init>".equals(vmmethodname)) {
			if (isAnonymous(vmclassname)) {
				return "{...}";
			} else {
				result.append(getShortClassName(vmclassname));
			}
		} else {
			result.append(vmmethodname);
		}
		result.append('(');
		final Type[] arguments = Type.getArgumentTypes(vmdesc);
		boolean comma = false;
		for (final Type arg : arguments) {
			if (isInnerClass(vmclassname) && arg.getClassName()
					.equals(getOutterClassName(vmclassname)))
				continue;

			if (comma) {
				result.append(",");
			} else {
				comma = true;
			}
			if (qualifiedParams) {
				result.append(getClassName(arg.getClassName()));
			} else {
				result.append(getShortTypeName(arg));
			}
		}
		result.append(')');
		return result.toString();
	}

	public static String getShortClassName(final String vmname) {
		final String name = getClassName(vmname);
		return name.substring(name.lastIndexOf('.') + 1);
	}

	private static String getClassName(final String vmname) {
		final int pos = vmname.lastIndexOf('/');
		final String name = pos == -1 ? vmname : vmname.substring(pos + 1);
		return name.replace('$', '.');
	}

	private static boolean isAnonymous(final String vmname) {
		final int dollarPosition = vmname.lastIndexOf('$');
		if (dollarPosition == -1) {
			return false;
		}
		final int internalPosition = dollarPosition + 1;
		if (internalPosition == vmname.length()) {
			// shouldn't happen for classes compiled from Java source
			return false;
		}
		// assume non-identifier start character for anonymous classes
		final char start = vmname.charAt(internalPosition);
		return !Character.isJavaIdentifierStart(start);
	}

	private static Object getOutterClassName(String vmclassname) {
		final int pos = vmclassname.lastIndexOf('$');
		final String res = pos == -1 ? getClassName(vmclassname)
				: getClassName(vmclassname.substring(0, pos));
		return res;
	}

	private static boolean isInnerClass(String vmclassname) {
		return vmclassname.lastIndexOf('$') != -1;
	}

	private static String getShortTypeName(final Type type) {
		final String name = type.getClassName();
		final int pos = name.lastIndexOf('.');
		final String shortName = pos == -1 ? name : name.substring(pos + 1);
		return shortName.replace('$', '.');
	}

	public static String filterReferenceName(String reference) {
		reference = reference.trim();
		if (reference.endsWith("[]"))
			return reference.substring(0, reference.lastIndexOf("[]"));
		return reference;
	}
}
