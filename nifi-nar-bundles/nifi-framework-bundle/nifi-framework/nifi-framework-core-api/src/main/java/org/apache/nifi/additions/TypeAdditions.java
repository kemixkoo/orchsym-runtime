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
package org.apache.nifi.additions;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public interface TypeAdditions {
    String ADDITIONS_NAME = "additions";
    String ADDITION_NAME = "addition";

    String ADDITION_KEY_NAME = "name";
    String ADDITION_VALUE_NAME = "value";

    Map<String, String> values();

    void set(Map<String, String> additions);

    String setValue(String name, Object value);

    String remove(String name);

    /**
     * 统一用大写名字
     */
    default String unifyName(String name) {
        return name.toUpperCase();
    }

    /**
     * 含有相应名字，就返回true
     */
    default boolean has(String name) {
        if (Objects.isNull(name)) {
            return false;
        }
        final Map<String, String> additions = values();
        if (Objects.isNull(additions) || additions.isEmpty()) {
            return false;
        }
        return additions.containsKey(unifyName(name));
    }

    /**
     * 
     * 无论值是否为空，只要设置该名字，都将返回true
     */
    default boolean hasValue(String name) {
        return has(name) //
                && StringUtils.isNotBlank(getValue(name));

    }

    /**
     * 不存在相应名字，将返回null；如果存在则直接返回对应值
     */
    default String getValue(String name) {
        if (Objects.isNull(name) || has(name)) {
            return null;
        }
        return values().get(unifyName(name));
    }

}
