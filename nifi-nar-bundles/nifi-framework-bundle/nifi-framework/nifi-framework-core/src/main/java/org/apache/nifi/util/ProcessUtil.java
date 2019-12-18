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
import org.apache.nifi.additions.StandardTypeAdditions;
import org.apache.nifi.additions.TypeAdditions;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.groups.ProcessTags;
import org.apache.nifi.web.api.dto.ApplicationInfoDTO;
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
    public static Set<String> getTags(final Element parentElement) {
        final Set<String> tags = new HashSet<>();
        final Element tagsElement = DomUtils.getChild(parentElement, ProcessTags.TAGS_NAME);

        // FIXME maybe should do migration task for this
        tags.addAll(getOldTags(parentElement));

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

    private static Set<String> getOldTags(final Element parentElement) {
        final Set<String> tags = new HashSet<>();
        final List<Element> tagElements = DomUtils.getChildElementsByTagName(parentElement, ProcessTags.TAG_NAME);
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
    public static Map<String, String> getAdditions(final Element parentElement) {
        final Map<String, String> additions = new HashMap<>();
        final Element additionsElement = DomUtils.getChild(parentElement, TypeAdditions.ADDITIONS_NAME);

        if (null != additionsElement) {
            // FIXME maybe should do migration task for this
            additions.putAll(getOldAdditions(additionsElement));

            final List<Element> additionElements = DomUtils.getChildElementsByTagName(additionsElement, TypeAdditions.ADDITION_NAME);
            for (Element additionElement : additionElements) {
                final String name = additionElement.getAttribute(TypeAdditions.ADDITION_KEY_NAME);
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
            if (additionName.equals(TypeAdditions.ADDITION_NAME)) {
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
            final Element additionsElement = ownerDocument.createElement(TypeAdditions.ADDITIONS_NAME);
            parentElement.appendChild(additionsElement);

            for (Entry<String, String> entry : additions.entrySet()) {
                final Element additionElement = ownerDocument.createElement(TypeAdditions.ADDITION_NAME);
                additionsElement.appendChild(additionElement);

                additionElement.setAttribute(TypeAdditions.ADDITION_KEY_NAME, entry.getKey());
                additionElement.setTextContent(entry.getValue());
            }
        }
    }

    public static void fixDefaultValue(Map<String, String> additionsMap, String name, Object defaultValue) {
        if (Objects.isNull(defaultValue)) {
            return;
        }
        StandardTypeAdditions additions = new StandardTypeAdditions(additionsMap);

        if (additions.has(name)) {// 名字统一
            return;
        }
        // 名字统一，且设置值到原map
        additionsMap.put(additions.unifyName(name), defaultValue.toString());
    }

    public static boolean containAddition(Map<String, String> additionsMap, String name) {
        return new StandardTypeAdditions(additionsMap).has(name);// 名字统一
    }

    /**
     * 
     * 获取返回值
     */

    public static Boolean getAdditionBooleanValue(TypeAdditions additionParam, String name, Boolean defaultValue) {
        final String valueStr = additionParam.getValue(name); // 名字统一
        if (StringUtils.isNotBlank(valueStr)) {
            return Boolean.parseBoolean(valueStr);
        }
        return defaultValue;
    }

    public static Boolean getAdditionBooleanValue(Map<String, String> additionsMap, String name, Boolean defaultValue) {
        return getAdditionBooleanValue(new StandardTypeAdditions(additionsMap), name, defaultValue);
    }

    public static Boolean getGroupAdditionBooleanValue(ProcessGroup group, String name) {
        return getGroupAdditionBooleanValue(group, name, null);
    }

    public static Boolean getGroupAdditionBooleanValue(ProcessGroup group, String name, Boolean defaultValue) {
        return getAdditionBooleanValue(group.getAdditions(), name, defaultValue);
    }

    public static Long getAdditionLongValue(TypeAdditions additionParam, String name, Long defaultValue) {
        final String valueStr = additionParam.getValue(name); // 名字统一
        if (StringUtils.isNotBlank(valueStr)) {
            try {
                return Long.valueOf(valueStr);
            } catch (NumberFormatException e) {
                //
            }
        }
        return defaultValue;
    }

    public static Long getAdditionLongValue(Map<String, String> additionsMap, String name, Long defaultValue) {
        return getAdditionLongValue(new StandardTypeAdditions(additionsMap), name, defaultValue);
    }

    public static Long getGroupAdditionLongValue(ProcessGroup group, String name) {
        return getGroupAdditionLongValue(group, name, null);
    }

    public static Long getGroupAdditionLongValue(ProcessGroup group, String name, Long defaultValue) {
        return getAdditionLongValue(group.getAdditions(), name, defaultValue);
    }

    public static String getAdditionValue(TypeAdditions additions, String name, String defaultValue) {
        if (additions.has(name)) {// 名字统一
            return additions.getValue(name);
        }
        return defaultValue;
    }

    public static String getAdditionValue(Map<String, String> additionsMap, String name, String defaultValue) {
        return getAdditionValue(new StandardTypeAdditions(additionsMap), name, defaultValue);
    }

    /**
     * 
     * 计算应用路径信息和父模块信息
     */
    public static ApplicationInfoDTO calcApplicationInfo(ProcessGroup group, String rootId) {
        ApplicationInfoDTO appInfoDto = new ApplicationInfoDTO();
        if (isRootGroup(group, rootId)) { // 可能已经在根
            return appInfoDto;
        }
        final String curGroupId = group.getIdentifier();
        final String curGroupName = group.getName();
        appInfoDto.setParentId(curGroupId);
        appInfoDto.setParentName(curGroupName);

        // init appId
        appInfoDto.setApplicationId(curGroupId);
        appInfoDto.setApplicationName(curGroupName);
        appInfoDto.setPath(curGroupName);

        ProcessGroup parentGroup = group.getParent();
        StringBuffer route = new StringBuffer();
        route.append(curGroupName);
        if (!(isRootGroup(parentGroup, rootId))) { // 不是应用级别，group还是多级子模块
            route.insert(0, parentGroup.getName() + '/');

            ProcessGroup applicationGroup = parentGroup;

            parentGroup = parentGroup.getParent();
            while (!isRootGroup(parentGroup, rootId)) {
                route.insert(0, parentGroup.getName() + '/');
                applicationGroup = parentGroup;
                parentGroup = parentGroup.getParent();
            }
            appInfoDto.setApplicationId(applicationGroup.getIdentifier());
            appInfoDto.setApplicationName(applicationGroup.getName());
        }
        appInfoDto.setPath(route.toString());

        return appInfoDto;
    }

    private static boolean isRootGroup(ProcessGroup group, String rootId) {
        if (Objects.isNull(group) || group.getIdentifier().equals(rootId)) { // 可能已经在根
            return true;
        }
        return false;
    }

    public static boolean isAppGroup(ProcessGroup group) {
        if (!Objects.isNull(group) && !group.isRootGroup() //
                && !Objects.isNull(group.getParent()) && group.getParent().isRootGroup()) {
            return true;
        }
        return false;
    }
}
