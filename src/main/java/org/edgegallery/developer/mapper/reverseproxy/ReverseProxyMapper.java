/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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
package org.edgegallery.developer.mapper.reverseproxy;

import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.developer.model.reverseproxy.ReverseProxy;

import java.util.List;
import java.util.Set;

@Mapper
public interface ReverseProxyMapper {

    int createReverseProxy(ReverseProxy reverseProxy);

    int deleteProxyByDestHostId(String destHostId);

    List<ReverseProxy> getProxiesByDestHostId(String destHostId);

    Set<Integer> getAllVncProxyPorts();
}
