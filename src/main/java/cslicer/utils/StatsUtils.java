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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cslicer.utils.PrintUtils.TAG;

public final class StatsUtils {

	private StatsUtils() {
	}

	private static Map<String, Date> clocks = new HashMap<>();
	private static Map<String, Integer> counts = new HashMap<>();
	private static Map<String, Double> timeTakens = new HashMap<>();
	private static Map<String, Double> percentage = new HashMap<>();

	public static void resume(String name) {
		clocks.put(name, new Date());
	}

	public static void stop(String name) {
		assert (clocks.containsKey(name));

		if (!timeTakens.containsKey(name))
			timeTakens.put(name, 0.0);
		Date now = new Date();
		timeTakens.put(name,
				(now.getTime() - clocks.get(name).getTime()) / 1000.0);
	}

	public static void count(String name) {
		if (!counts.containsKey(name)) {
			counts.put(name, 0);
		}
		counts.put(name, counts.get(name) + 1);
	}

	public static void setCount(String name, int value) {
		counts.put(name, value);
	}

	public static int readCount(String name) {
		if (!counts.containsKey(name))
			return 0;
		return counts.get(name);
	}

	public static void setPercentage(String name, double value) {
		percentage.put(name, value);
	}

	public static void print() {
		PrintUtils.print("************** Stats **************",
				PrintUtils.TAG.STATS);
		for (String name : timeTakens.keySet()) {
			PrintUtils.print(name + " : " + timeTakens.get(name), TAG.STATS);
		}
		for (String name : counts.keySet()) {
			PrintUtils.print(name + " : " + counts.get(name), TAG.STATS);
		}
		for (String name : percentage.keySet()) {
			PrintUtils.print(name + " : " + percentage.get(name), TAG.STATS);
		}
		PrintUtils.print("***********************************",
				PrintUtils.TAG.STATS);
	}
}
