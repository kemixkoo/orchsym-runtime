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
 * 模板来源
 * @author liuxun
 */
public enum TemplateSourceType {
    /**
     * 0: 来源类型是上传
     */
    UPLOADED_TYPE(0),
    /**
     * 1: 来源类型是官方内置
     */
    OFFICIAL_TYPE(1),
    /**
     * 2: 来源类型是另存为(一键下载类型)
     */
    SAVE_AS_TYPE(2),
    ;
    private int value = 0;
    private TemplateSourceType(int value) {    //    必须是private的，否则编译错误
        this.value = value;
    }

    public static TemplateSourceType valueOf(int value) {    //    手写的从int到enum的转换函数
        switch (value) {
            case 0:
                return UPLOADED_TYPE;
            case 1:
                return OFFICIAL_TYPE;
            case 2:
                return SAVE_AS_TYPE;
            default:
                return null;
        }
    }

    public int value() {
        return this.value;
    }


}
