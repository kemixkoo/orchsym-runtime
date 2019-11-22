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
 * 模板类型 包括 应用类型和非应用类型
 *
 * @author liuxun
 */
public enum TemplateType {
    /**
     * 0: 普通类型
     */
    NORMAL,
    /**
     * 1: 应用类型
     */
    APPLICATION;

    public int value() {
        return this.ordinal();
    }

    public boolean is(Map<String, String> additions) {
        if (Objects.isNull(additions)) {
            return false;
        }
        return this.equals(match(additions.get(TemplateFiledName.TEMPLATE_TYPE)));
    }

    public boolean not(Map<String, String> additions) {
        return !is(additions);
    }

    public static TemplateType match(String value) {
        if (!Objects.isNull(value)) {
            for (TemplateType t : TemplateType.values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
        }
        return NORMAL;
    }

}
