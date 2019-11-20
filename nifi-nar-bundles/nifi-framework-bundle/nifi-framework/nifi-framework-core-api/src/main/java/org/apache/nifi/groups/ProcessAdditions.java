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
package org.apache.nifi.groups;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public interface ProcessAdditions {
    String ADDITIONS_NAME = "additions";
    String ADDITION_NAME = "addition";

    String ADDITION_KEY_NAME = "name";
    String ADDITION_VALUE_NAME = "value";

    Map<String, String> getAdditions();

    void setAdditions(Map<String, String> additions);

    default String checkAdditionName(String name) {
        return name.toUpperCase();
    }

    default boolean hasAddition(String name) {
        if (Objects.isNull(name)) {
            return false;
        }
        final Map<String, String> additions = getAdditions();
        if (Objects.isNull(additions) || additions.isEmpty())
            return false;
        return additions.containsKey(checkAdditionName(name));
    }

    default boolean hasAdditionValue(String name) {
        return hasAddition(name) //
                && StringUtils.isNotBlank(getAddition(name));

    }

    default String getAddition(String name) {
        if (Objects.isNull(name)) {
            return null;
        }
        final Map<String, String> additions = getAdditions();
        if (Objects.isNull(additions) || additions.isEmpty())
            return null;
        return additions.get(checkAdditionName(name));
    }

    default String setAddition(String name, Object value) {
        if (Objects.isNull(name)) {
            return null;
        }
        final String checkAdditionName = checkAdditionName(name);

        final String oldValue = getAddition(checkAdditionName);

        Map<String, String> newAdditions = new HashMap<>();
        final Map<String, String> additions = getAdditions();
        if (!Objects.isNull(additions)) {
            newAdditions.putAll(additions);
        }
        newAdditions.put(checkAdditionName, Objects.isNull(value) ? "" : value.toString());
        setAdditions(newAdditions);

        return oldValue;
    }

    default String removeAddition(String name) {
        if (Objects.isNull(name)) {
            return null;
        }
        final String checkAdditionName = checkAdditionName(name);

        final String oldValue = getAddition(checkAdditionName);

        Map<String, String> newAdditions = new HashMap<>();
        final Map<String, String> additions = getAdditions();
        if (!Objects.isNull(additions)) {
            newAdditions.putAll(additions);
        }
        newAdditions.remove(checkAdditionName);
        setAdditions(newAdditions);

        return oldValue;
    }

}
