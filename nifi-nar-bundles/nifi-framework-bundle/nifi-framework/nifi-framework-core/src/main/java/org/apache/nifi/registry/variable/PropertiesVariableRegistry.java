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
package org.apache.nifi.registry.variable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.registry.VariableDescriptor;
import org.apache.nifi.registry.VariableRegistry;
import org.apache.nifi.util.NiFiProperties;

/**
 * @author GU Guoqiang
 *
 */
public class PropertiesVariableRegistry implements VariableRegistry {
    private final Map<VariableDescriptor, String> map;

    private final static VariableDescriptor UPLOAD_REPO_PATH_VAR = new VariableDescriptor(NiFiProperties.UPLOAD_REPO_PATH_VAR);

    public PropertiesVariableRegistry(final VariableRegistry variableRegistry, final NiFiProperties properties) {
        final Map<VariableDescriptor, String> newMap = new HashMap<>(variableRegistry.getVariableMap());

        //
        String uploadRepoisotryPath = properties.getUploadRepoisotryPath();
        if (StringUtils.isNotBlank(uploadRepoisotryPath)) {
            newMap.put(UPLOAD_REPO_PATH_VAR, new File(uploadRepoisotryPath).getAbsolutePath());
        }

        map = Collections.unmodifiableMap(newMap);
    }

    @Override
    public Map<VariableDescriptor, String> getVariableMap() {
        return map;
    }

}
