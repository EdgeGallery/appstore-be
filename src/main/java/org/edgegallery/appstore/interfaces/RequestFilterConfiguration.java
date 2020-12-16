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

package org.edgegallery.appstore.interfaces;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class RequestFilterConfiguration {

    /**
     * Register log filter.
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> authFilterRegistrationBean() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HttpTraceLogFilter());
        registration.addUrlPatterns("/mec/appstore/v1/*");
        registration.setName("HttpTraceLogFilter");
        registration.setOrder(0);
        return registration;
    }

    /**
     * Registers json request size filter.
     *
     * @return instance of filter registration bean
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> jsonRequestSizeFilterRegistrationBean() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JsonRequestSizeLimitFilter());
        registration.addUrlPatterns("/mec/appstore/v1/*");
        registration.setName("JsonRequestSizeLimitFilter");
        registration.setOrder(1);
        return registration;
    }
}
