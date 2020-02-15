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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONWriter;
import org.junit.Test;

public class TestJson {

	@Test
	public void test() throws IOException {
		List<String> commitList = new ArrayList<String>();
		commitList.add("c1");
		commitList.add("c2");
		commitList.add("c3");
		Map<String, Double> entSigMap = new TreeMap<String, Double>();
		entSigMap.put("e1", 0.1d);
		entSigMap.put("e2", 0.2d);
		entSigMap.put("e3", 0.3d);

		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"/home/chenguang/Desktop/ICSE_TOOL/opt-branch/json"));
		JSONWriter jw = new JSONWriter(bw);
		jw.object();
		jw.key("simple");
		jw.array();
		for (String c : commitList) {
			jw.value(c);
		}
		jw.endArray();

		jw.key("full");
		jw.array();
		for (String key : entSigMap.keySet()) {
			jw.array();
			jw.value(key);
			jw.value(entSigMap.get(key) + "");
			jw.endArray();
		}
		jw.endArray();

		jw.endObject();
		bw.close();
	}

}
