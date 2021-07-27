/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.edgegallery.developer.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

	private FileUtil() {

	}

	/**
	 * Read file content<br>
	 * 
	 * @param filePath file path
	 * @return file content
	 */
	public static String readFileContent(String filePath) {
		Path path = Path.of(filePath);
		try {
			return Files.readString(path);
		} catch (IOException ex) {
			LOGGER.error("read file {} occur exception {}", filePath, ex.getMessage());
			return "error";
		}
	}

	/**
	 * Get all files in the directory.<br>
	 * @param dir file directory
	 * @return all files in the directory
	 */
	public static List<String> getAllFilePath(File dir) {
		if (dir == null || !dir.isDirectory()) {
			return Collections.emptyList();
		}

		List<String> listLocal = new ArrayList<>();
		try (Stream<Path> walk = Files.walk(dir.toPath())) {
			List<Path> allPaths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
			for (Path path : allPaths) {
				try {
					listLocal.add(path.toFile().getCanonicalPath());
				} catch (IOException e) {
					LOGGER.error("getCanonicalPath {} occur exception {}", path, e.getMessage());
				}
			}
		} catch (IOException ex) {
			LOGGER.error("Files.walk dir {} occur exception {}", dir, ex.getMessage());
		}

		return listLocal;
	}
}
