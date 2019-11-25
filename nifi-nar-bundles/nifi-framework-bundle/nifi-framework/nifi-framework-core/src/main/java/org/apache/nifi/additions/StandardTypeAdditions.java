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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author GU Guoqiang
 *
 */
public class StandardTypeAdditions implements TypeAdditions {
    private volatile Map<String, String> additions = new HashMap<>();

    @Override
    public Map<String, String> values() {
        return this.additions;
    }

    @Override
    public void set(Map<String, String> additions) {
        if (Objects.isNull(additions)) {
            this.additions = Collections.emptyMap();
        } else {
            final Map<String, String> newAdditions = new HashMap<>(values());
            for (final Map.Entry<String, String> entry : additions.entrySet()) {
                if (entry.getValue() == null) {
                    newAdditions.remove(entry.getKey());
                } else {
                    newAdditions.put(entry.getKey(), entry.getValue());
                }
            }
            this.additions = Collections.unmodifiableMap(newAdditions);
        }
    }

    @Override
    public String setValue(String name, Object value) {
        return setValue(name, value, false);
    }

    @Override
    public String remove(String name) {
        return setValue(name, null, true);
    }

    private String setValue(String name, Object value, boolean remove) {
        if (Objects.isNull(name)) {
            return null;
        }
        final String checkedAdditionName = unifyName(name);

        final String oldValue = getValue(checkedAdditionName);

        Map<String, String> newAdditions = new HashMap<>(values());
        if (remove) {
            newAdditions.remove(checkedAdditionName);
        } else {
            newAdditions.put(checkedAdditionName, Objects.isNull(value) ? "" : value.toString());
        }

        set(newAdditions);

        return oldValue;
    }

}
