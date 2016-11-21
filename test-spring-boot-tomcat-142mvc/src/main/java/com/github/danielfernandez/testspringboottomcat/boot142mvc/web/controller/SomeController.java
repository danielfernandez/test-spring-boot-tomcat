/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package com.github.danielfernandez.testspringboottomcat.boot142mvc.web.controller;

import com.github.danielfernandez.testspringboottomcat.lib.SomeInterfaceClassClassLoaderUtils;
import com.github.danielfernandez.testspringboottomcat.lib.SomeInterfaceThreadContextClassLoaderUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SomeController {

    @RequestMapping("/")
    public String index() {
        return "Use '/threadcontext' to try 'Thread.currentThread().getContextClassLoader()',  '/class' to try 'SomeInterface.class.getClassLoader()'";
    }

    @RequestMapping("/threadcontext")
    public String threadContext() {
        final String output = SomeInterfaceThreadContextClassLoaderUtils.outputSomething();
        final String message =
                String.format(
                        "THREAD CONTEXT | Spring Boot: %s | Spring Web MVC: %s | Output: %s",
                        SpringBootVersion.getVersion(), SpringVersion.getVersion(), output);
        return message;
    }

    @RequestMapping("/class")
    public String clazz() {
        final String output = SomeInterfaceClassClassLoaderUtils.outputSomething();
        final String message =
                String.format(
                        "CLASS | Spring Boot: %s | Spring Web MVC: %s | Output: %s",
                        SpringBootVersion.getVersion(), SpringVersion.getVersion(), output);
        return message;
    }

}
