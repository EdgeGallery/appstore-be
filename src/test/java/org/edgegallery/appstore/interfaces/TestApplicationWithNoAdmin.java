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

package org.edgegallery.appstore.interfaces;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.edgegallery.appstore.domain.constants.Consts;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.OncePerRequestFilter;

@SpringBootApplication(scanBasePackages = "org.edgegallery.appstore", exclude = {SecurityAutoConfiguration.class})
@MapperScan(basePackages = {"org.edgegallery.appstore.infrastructure.persistence"})
@EnableScheduling
@EnableServiceComb
public class TestApplicationWithNoAdmin {
    private static final String USERID = "d0f8fa57-2f4c-4182-be33-0a508964d04a";
    private static final String USERNAME = "TestUser";

    public static void main(String[] args) {
        SpringApplication.run(TestApplicationWithNoAdmin.class, args);
    }

    @Bean
    public OncePerRequestFilter accessTokenFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
                request.setAttribute(Consts.USERID, USERID);
                request.setAttribute(Consts.USERNAME, USERNAME);
                filterChain.doFilter(request, response);
            }
        };
    }
}
