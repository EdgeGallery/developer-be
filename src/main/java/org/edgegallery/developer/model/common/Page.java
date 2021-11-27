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

package org.edgegallery.developer.model.common;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Page<T> {
    private List<T> results;
    private int limit;
    private long offset;
    private long total;

    /**
     * Constructor of Page.
     */
    public Page(List<T> results, int limit, long offset, long total) {
        this.results = results;
        this.limit = limit;
        this.offset = offset;
        this.total = total;
    }

    public List<T> getResults() {
        return results;
    }

    public int getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }

    public long getTotal() {
        return total;
    }

    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return new Page<>(getList(converter), limit, offset, total);
    }

    private <U> List<U> getList(Function<? super T, ? extends U> converter) {
        if (converter == null) {
            throw new IllegalArgumentException("function must not be null");
        }
        return this.results.stream().map(converter).collect(Collectors.toList());
    }

}
