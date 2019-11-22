/*
 * Licensed to the Orchsym Runtime under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * this file to You under the Orchsym License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://github.com/orchsym/runtime/blob/master/orchsym/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.web.api.orchsym.template;

/**
 * 模板类型 包括 应用类型和非应用类型
 *
 * @author liuxun
 */
public enum TemplateType {
    /**
     * 0: 应用类型
     */
    APP_TYPE(0),

    /**
     * 1: 非应用类型
     */
    NON_APP_TYPE(1);

    private int value = 0;

    private TemplateType(int value) {    //    必须是private的，否则编译错误
        this.value = value;
    }

    public static TemplateType valueOf(int value) {    //    手写的从int到enum的转换函数
        switch (value) {
            case 0:
                return APP_TYPE;
            case 1:
                return NON_APP_TYPE;
            default:
                return null;
        }
    }

    public int value() {
        return this.value;
    }


}
