/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.edgegallery.developer.test.util;

import java.io.File;
import java.util.List;

import org.edgegallery.developer.util.FileUtil;
import org.junit.Assert;
import org.junit.Test;

public class FileUtilTest {

	@Test
	public void testGetAllFilePath() {
		File dir = new File("src/test/java");
		List<String> results = FileUtil.getAllFilePath(dir);
		Assert.assertFalse(results.isEmpty());
	}
	
	@Test
	public void testReadFileContent() {
		String filePath = "src/test/java/org/edgegallery/developer/util/FileUtilTest.java";
		String fileContent = FileUtil.readFileContent(filePath);
		Assert.assertNotNull(fileContent);
	}
}