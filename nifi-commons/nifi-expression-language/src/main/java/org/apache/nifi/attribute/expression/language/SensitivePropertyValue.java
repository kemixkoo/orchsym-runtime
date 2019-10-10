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
package org.apache.nifi.attribute.expression.language;

import java.util.Map;

import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.controller.ControllerServiceLookup;
import org.apache.nifi.expression.AttributeValueDecorator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.registry.VariableRegistry;

import com.orchsym.util.SensitiveUtil;

/**
 * 
 * @author Kemix koo
 *
 */
public class SensitivePropertyValue extends StandardPropertyValue {

    public SensitivePropertyValue(String rawValue, ControllerServiceLookup serviceLookup, PreparedQuery preparedQuery, VariableRegistry variableRegistry) {
        super(rawValue, serviceLookup, preparedQuery, variableRegistry);
    }

    public SensitivePropertyValue(String rawValue, ControllerServiceLookup serviceLookup, VariableRegistry variableRegistry) {
        super(rawValue, serviceLookup, variableRegistry);
    }

    public SensitivePropertyValue(String rawValue, ControllerServiceLookup serviceLookup) {
        super(rawValue, serviceLookup);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PropertyValue evaluateAttributeExpressions(FlowFile flowFile, Map<String, String> additionalAttributes, AttributeValueDecorator decorator, Map<String, String> stateValues)
            throws ProcessException {
        if (rawValue == null || preparedQuery == null) {
            return this;
        }

        final ValueLookup lookup = new ValueLookup(variableRegistry, flowFile, additionalAttributes);
        final String evaluated = preparedQuery.evaluateExpressions(lookup, decorator, stateValues);

        return new SensitivePropertyValue(evaluated, serviceLookup, new EmptyPreparedQuery(evaluated), null);
    }

    @Override
    public String getValue(boolean encrypt) {
        final String rawValue = getValue();
        try {
            // if rawValue is contain vars, return the raw value always
            if (null == preparedQuery || false == isExpressionLanguagePresent()) {
                if (encrypt) {
                    return SensitiveUtil.encrypt(rawValue);
                } else {
                    return SensitiveUtil.decrypt(rawValue);
                }
            }
        } catch (Throwable e) {
            //
        }
        return rawValue;
    }

}
