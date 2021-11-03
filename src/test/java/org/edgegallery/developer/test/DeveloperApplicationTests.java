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

package org.edgegallery.developer.test;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.edgegallery.developer.util.SpringContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.OncePerRequestFilter;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class}, scanBasePackages = "org.edgegallery.developer")
@MapperScan(basePackages = {"org.edgegallery.developer.mapper", "org.edgegallery.developer.mapper.capability",
    "org.edgegallery.developer.infrastructure.persistence"})
@EnableScheduling
@EnableServiceComb
public class DeveloperApplicationTests {

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = SpringApplication.run(DeveloperApplicationTests.class, args);
        SpringContextUtil.setApplicationContext(applicationContext);
    }

    @Bean
    public OncePerRequestFilter accessTokenFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
                filterChain.doFilter(request, response);
            }
        };
    }
}
