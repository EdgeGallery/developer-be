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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {

    public String doRequest(String location, String method, Map<String, String> header, String contentType,
                            String params) throws IOException {
        boolean outPut = false;
        if (method.equals("PUT") || method.equals("POST")) {
            outPut = true;
        }
        return execute(location, params, header, method, contentType, outPut);
    }

    private String execute(String location, String params, Map<String, String> header, String method,
                           String contentType, boolean outPut) throws IOException {
        URL url = new URL(location);
        HttpURLConnection connection = connection(header, method, outPut, url, contentType);

        if (params != null && params.length() > 1) {
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(params.getBytes());
            outputStream.flush();
            outputStream.close();
        }
        return getResponse(connection);
    }

    private HttpURLConnection connection(Map<String, String> header, String method, boolean outPut, URL url,
                                         String contentType) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setDoInput(true);
        connection.setDoOutput(outPut);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Connection", "Keep-Alive");
        if (!header.isEmpty()) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return connection;
    }

    private String getResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    StandardCharsets.UTF_8));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            return builder.toString();

        } else {
            return responseCode + " : " + connection.getResponseMessage();
        }
    }
}
