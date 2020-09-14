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

package org.edgegallery.sample;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpPostFileRequest {
    private static final String DEFAULT_CHARSET_NAME = "UTF-8";

    public String doPostFileRequest(String url, Map<String, String> headerMap, Map<String, String> paramMap,
                                    Map<String, File> fileMap) throws IOException {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String line = "\r\n";
        HttpURLConnection connection = null;
        DataOutputStream dataOutStream = null;
        try {
            URL ur = new URL(url);
            connection = (HttpURLConnection) ur.openConnection();
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", DEFAULT_CHARSET_NAME);
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            dataOutStream = new DataOutputStream(connection.getOutputStream());

            if (headerMap != null) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    dataOutStream.writeBytes(entry.getKey() + ":" + entry.getValue());
                    dataOutStream.writeBytes(line);
                }
            }

            if (paramMap != null) {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    dataOutStream.writeBytes("--" + boundary);
                    dataOutStream.writeBytes(line);

                    dataOutStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"");
                    dataOutStream.writeBytes(line);

                    dataOutStream.writeBytes(line);
                    dataOutStream.writeBytes(URLEncoder.encode(entry.getValue(), DEFAULT_CHARSET_NAME));
                    dataOutStream.writeBytes(line);
                }
            }

            if (fileMap != null) {
                for (Map.Entry<String, File> fileEntry : fileMap.entrySet()) {
                    String fileName = fileEntry.getValue().getName();

                    dataOutStream.writeBytes("--" + boundary);
                    dataOutStream.writeBytes(line);

                    dataOutStream.writeBytes(
                        "Content-Disposition: form-data;name=\"" + fileEntry.getKey() + "\"; filename=\"" + fileName
                            + "\"");
                    dataOutStream.writeBytes(line);

                    dataOutStream.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(fileName));
                    dataOutStream.writeBytes(line);
                    dataOutStream.writeBytes("Content-Transfer-Encoding: 8bit");
                    dataOutStream.writeBytes(line);
                    dataOutStream.writeBytes(line);

                    InputStream iStream = null;

                    iStream = new FileInputStream(fileEntry.getValue());
                    byte[] buffer = new byte[8 * 1024];
                    int bytesRead;
                    while ((bytesRead = iStream.read(buffer)) != -1) {
                        dataOutStream.write(buffer, 0, bytesRead);
                    }
                    iStream.close();
                    dataOutStream.writeBytes(line);
                }
            }

            dataOutStream.writeBytes("--" + boundary + "--");
            dataOutStream.writeBytes(line);
            dataOutStream.flush();
            dataOutStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                        StandardCharsets.UTF_8));
                String lineRead;
                StringBuilder builder = new StringBuilder();
                while ((lineRead = reader.readLine()) != null) {
                    builder.append(lineRead);
                }
                reader.close();
                return builder.toString();
            } else {
                System.out.println("rest connect error: " + responseCode);
                return connection.getResponseMessage();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}