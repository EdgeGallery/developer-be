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

package org.edgegallery.developer.model.handler;

import com.google.gson.Gson;
import com.google.json.JsonSanitizer;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class JsonTypeHandler<T extends Object> extends BaseTypeHandler<T> {

    Gson gson = new Gson();

    private Class<T> clazz;

    /**
     * handle json.
     */
    public JsonTypeHandler(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.clazz = clazz;
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Object o, JdbcType jdbcType)
        throws SQLException {
        preparedStatement.setString(i, gson.toJson(o));
    }

    @Override
    public T getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String text = resultSet.getString(s);
        return gson.fromJson(text, clazz);
    }

    @Override
    public T getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String text = resultSet.getString(i);
        return gson.fromJson(text, clazz);
    }

    @Override
    public T getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String text = callableStatement.getString(i);
        text = JsonSanitizer.sanitize(text);
        return gson.fromJson(text, clazz);
    }
}
