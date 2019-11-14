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
package org.apache.nifi.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.groups.ProcessAdditions;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.groups.ProcessTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author GU Guoqiang
 *
 */
public final class ProcessUtil {

    /**
     * get the tags from flow.xml
     */
    public static Set<String> getTags(final Element processGroupElement) {
        final Set<String> tags = new HashSet<>();
        final Element tagsElement = DomUtils.getChild(processGroupElement, ProcessTags.TAGS_NAME);

        // FIXME maybe should do migration task for this
        tags.addAll(getOldTags(processGroupElement));

        if (null != tagsElement) {
            final List<Element> tagElements = DomUtils.getChildElementsByTagName(tagsElement, ProcessTags.TAG_NAME);
            for (final Element tagElement : tagElements) {
                final String value = tagElement.getTextContent();
                if (StringUtils.isNotBlank(value)) {
                    tags.add(value);
                }
            }
        }
        return tags;
    }

    private static Set<String> getOldTags(final Element processGroupElement) {
        final Set<String> tags = new HashSet<>();
        final List<Element> tagElements = DomUtils.getChildElementsByTagName(processGroupElement, ProcessTags.TAG_NAME);
        for (final Element tagElement : tagElements) {
            final String value = tagElement.getTextContent();
            if (StringUtils.isNotBlank(value)) {
                tags.add(value);
            }
        }
        return tags;
    }

    /**
     * serialize the tags to flow.xml
     */
    public static void addTags(final Element parentElement, Set<String> tags) {
        if (null != tags && tags.size() > 0) {
            final Document ownerDocument = parentElement.getOwnerDocument();
            final Element tagsElement = ownerDocument.createElement(ProcessTags.TAGS_NAME);
            parentElement.appendChild(tagsElement);

            for (String tag : tags) {
                final Element tagElement = ownerDocument.createElement(ProcessTags.TAG_NAME);
                tagsElement.appendChild(tagElement);

                tagElement.setTextContent(tag);
            }
        }
    }

    /**
     * get the additions map from flow.xml
     */
    public static Map<String, String> getAdditions(final Element processGroupElement) {
        final Map<String, String> additions = new HashMap<>();
        final Element additionsElement = DomUtils.getChild(processGroupElement, ProcessAdditions.ADDITIONS_NAME);

        if (null != additionsElement) {
            // FIXME maybe should do migration task for this
            additions.putAll(getOldAdditions(additionsElement));

            final List<Element> additionElements = DomUtils.getChildElementsByTagName(additionsElement, ProcessAdditions.ADDITION_NAME);
            for (Element additionElement : additionElements) {
                final String name = additionElement.getAttribute(ProcessAdditions.ADDITION_KEY_NAME);
                final String additionValue = additionElement.getTextContent();

                additions.put(name, additionValue);
            }
        }
        return additions;
    }

    private static Map<String, String> getOldAdditions(final Element additionsElement) {
        final Map<String, String> additions = new HashMap<>();
        final NodeList childNodes = additionsElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node node = childNodes.item(i);
            if (!(node instanceof Element)) {
                continue;
            }

            final Element child = (Element) childNodes.item(i);

            final String additionName = child.getTagName();
            if (additionName.equals(ProcessAdditions.ADDITION_NAME)) {
                continue; // ignore new tag
            }
            final String additionValue = child.getTextContent();
            if (additionName == null || additionValue == null) {
                continue;
            }

            additions.put(additionName, additionValue);
        }
        return additions;
    }

    /**
     * serialize the additions map to flow.xml
     */
    public static void addAddtions(final Element parentElement, final Map<String, String> additions) {
        if (null != additions && additions.size() > 0) {
            final Document ownerDocument = parentElement.getOwnerDocument();
            final Element additionsElement = ownerDocument.createElement(ProcessAdditions.ADDITIONS_NAME);
            parentElement.appendChild(additionsElement);

            for (Entry<String, String> entry : additions.entrySet()) {
                final Element additionElement = ownerDocument.createElement(ProcessAdditions.ADDITION_NAME);
                additionsElement.appendChild(additionElement);

                additionElement.setAttribute(ProcessAdditions.ADDITION_KEY_NAME, entry.getKey());
                additionElement.setTextContent(entry.getValue());
            }
        }
    }

    /**
     * update the value of key for additions
     */
    public static String updateGroupAdditions(ProcessGroup group, String name, Object value) {
        name = checkAdditionName(name);

        String oldValue = null;

        Map<String, String> newAdditions = new HashMap<>();
        final Map<String, String> additions = group.getAdditions();
        if (null != additions) {
            oldValue = additions.get(name);

            newAdditions.putAll(additions);
        }
        newAdditions.put(name, Objects.isNull(value) ? "" : value.toString());

        group.setAdditions(newAdditions);

        return oldValue;
    }

    /**
     * remove the key for additions
     */
    public static String removeGroupAdditions(ProcessGroup group, String name) {
        name = checkAdditionName(name);

        String oldValue = null;

        Map<String, String> newAdditions = new HashMap<>();
        final Map<String, String> additions = group.getAdditions();
        if (null != additions) {
            oldValue = additions.get(name);

            newAdditions.putAll(additions);
        }
        newAdditions.remove(name);

        group.setAdditions(newAdditions);

        return oldValue;
    }

    /**
     * check the value of key for additions
     */
    public static boolean hasValueGroupAdditions(ProcessGroup group, String name) {
        name = checkAdditionName(name);

        return hasGroupAdditions(group, name) //
                && StringUtils.isNotBlank(group.getAdditions().get(name));
    }

    /**
     * check the key for additions
     */
    public static boolean hasGroupAdditions(ProcessGroup group, String name) {

        final Map<String, String> additions = group.getAdditions();
        if (null != additions && additions.containsKey(checkAdditionName(name))) {
            return true;
        }

        return false;
    }

    public static boolean getGroupAdditionBooleanValue(ProcessGroup group, String name) {
        return getGroupAdditionBooleanValue(group, name, false);
    }

    public static boolean getGroupAdditionBooleanValue(ProcessGroup group, String name, boolean defaultValue) {
        final String valueStr = group.getAddition(checkAdditionName(name));
        if (StringUtils.isNotBlank(valueStr)) {
            return Boolean.parseBoolean(valueStr);
        }
        return defaultValue;
    }

    private static String checkAdditionName(String name) {
        return name.toUpperCase();
    }
}
