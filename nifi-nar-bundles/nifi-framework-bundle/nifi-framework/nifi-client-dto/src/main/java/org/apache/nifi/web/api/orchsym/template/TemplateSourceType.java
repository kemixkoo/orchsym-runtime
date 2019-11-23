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

import java.util.Map;
import java.util.Objects;

/**
 * 模板来源
 * 
 * @author liuxun
 */
public enum TemplateSourceType {
    /**
     * 0: 来源类型是上传
     */
    UNKOWN,
    /**
     * 1: 来源类型是上传
     */
    UPLOADED,
    /**
     * 1: 来源类型是官方内置
     */
    OFFICIAL,
    /**
     * 2: 来源类型是另存为(一键下载类型)
     */
    SAVE_AS;

    public int value() {
        return this.ordinal();
    }

    public boolean is(Map<String, String> additions) {
        if (Objects.isNull(additions)) {
            return false;
        }
        return this.equals(match(additions.get(TemplateFieldName.SOURCE_TYPE)));
    }

    public boolean not(Map<String, String> additions) {
        return !is(additions);
    }

    public static TemplateSourceType match(String value) {
        if (!Objects.isNull(value)) {
            for (TemplateSourceType t : TemplateSourceType.values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
        }
        return TemplateSourceType.UNKOWN;
    }

}
