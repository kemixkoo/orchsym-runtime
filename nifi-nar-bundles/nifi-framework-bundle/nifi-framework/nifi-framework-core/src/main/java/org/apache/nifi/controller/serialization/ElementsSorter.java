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
package org.apache.nifi.controller.serialization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.nifi.authorization.resource.ComponentAuthorizable;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.connectable.Connection;
import org.apache.nifi.connectable.Position;
import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.controller.ReportingTaskNode;
import org.apache.nifi.flowfile.FlowFilePrioritizer;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.registry.VariableDescriptor;

/**
 * 
 * @author GU Guoqiang
 *
 */
public final class ElementsSorter {

    public static abstract class EntrySorter<K, V> {
        public abstract List<Map.Entry<K, V>> sort(Set<Map.Entry<K, V>> entrySet);

        protected List<Map.Entry<K, V>> sort(Set<Map.Entry<K, V>> entrySet, Comparator<K> comparator) {
            List<Map.Entry<K, V>> sortedList = new ArrayList<>(entrySet);
            sortedList.sort(new Comparator<Map.Entry<K, V>>() {

                @Override
                public int compare(Map.Entry<K, V> entry1, Map.Entry<K, V> entry2) {
                    return comparator.compare(entry1.getKey(), entry2.getKey());
                }
            });
            return sortedList;
        }
    }

    /**
     * for String
     */
    public static final EntrySorter<String, String> STR_SORTER = new EntrySorter<String, String>() {
        public List<Map.Entry<String, String>> sort(Set<Map.Entry<String, String>> entrySet) {
            return sort(entrySet, new Comparator<String>() {

                @Override
                public int compare(String p1, String p2) {
                    return p1.compareTo(p2);
                }
            });
        }
    };
    /**
     * for PropertyDescriptor
     */
    public static final EntrySorter<PropertyDescriptor, String> PROP_SORTER = new EntrySorter<PropertyDescriptor, String>() {
        public List<Map.Entry<PropertyDescriptor, String>> sort(Set<Map.Entry<PropertyDescriptor, String>> entrySet) {
            return sort(entrySet, new Comparator<PropertyDescriptor>() {

                @Override
                public int compare(PropertyDescriptor p1, PropertyDescriptor p2) {
                    return p1.compareTo(p2);
                }
            });
        }
    };
    /**
     * for VariableDescriptor
     */
    public static final EntrySorter<VariableDescriptor, String> VAR_SORTER = new EntrySorter<VariableDescriptor, String>() {
        public List<Map.Entry<VariableDescriptor, String>> sort(Set<Map.Entry<VariableDescriptor, String>> entrySet) {
            return sort(entrySet, new Comparator<VariableDescriptor>() {

                @Override
                public int compare(VariableDescriptor p1, VariableDescriptor p2) {
                    return p1.compareTo(p2);
                }
            });
        }
    };

    /**
     * 
     * for ComponentAuthorizable
     *
     */
    public static class ComponentSorter {
        public List<? extends ComponentAuthorizable> sort(Set<? extends ComponentAuthorizable> set) {
            List<? extends ComponentAuthorizable> list = new ArrayList<>(set);
            list.sort(new Comparator<ComponentAuthorizable>() {

                @Override
                public int compare(ComponentAuthorizable o1, ComponentAuthorizable o2) {
                    return o1.getIdentifier().compareTo(o2.getIdentifier());
                }
            });
            return list;
        }
    }

    public static final ComponentSorter COMP_SORTER = new ComponentSorter();

    /**
     * for Position
     */
    public static List<Position> sortPositions(final Collection<Position> collections) {
        List<Position> list = new ArrayList<>(collections);
        list.sort(new Comparator<Position>() {
            public int compare(Position o1, Position o2) {
                if (o1.getX() != o2.getX()) {
                    return (int) (o1.getX() - o2.getX());
                }
                return (int) (o1.getY() - o2.getY());
            }
        });
        return list;
    }

    /**
     * 
     * Relationship / Connection
     */
    public static List<Relationship> sortRelationship(final Collection<Relationship> collections) {
        List<Relationship> list = new ArrayList<>(collections);
        list.sort(new Comparator<Relationship>() {

            @Override
            public int compare(Relationship o1, Relationship o2) {
                return o1.compareTo(o2);
            }
        });
        return list;
    }

    public static List<Connection> sortConnection(final Collection<Connection> collections) {
        List<Connection> list = new ArrayList<>(collections);
        list.sort(new Comparator<Connection>() {

            @Override
            public int compare(Connection o1, Connection o2) {
                return o1.getIdentifier().compareTo(o2.getIdentifier());
            }
        });
        return list;
    }

    /**
     * for ProcessorNode
     */
    public static List<ProcessorNode> sortNode(final Collection<ProcessorNode> collections) {
        List<ProcessorNode> list = new ArrayList<>(collections);
        list.sort(new Comparator<ProcessorNode>() {

            @Override
            public int compare(ProcessorNode o1, ProcessorNode o2) {
                return o1.getIdentifier().compareTo(o2.getIdentifier());
            }
        });
        return list;
    }

    /**
     * for ReportingTaskNode
     * 
     */
    public static List<ReportingTaskNode> sortTaskNode(final Collection<ReportingTaskNode> collections) {
        List<ReportingTaskNode> list = new ArrayList<>(collections);
        list.sort(new Comparator<ReportingTaskNode>() {

            @Override
            public int compare(ReportingTaskNode o1, ReportingTaskNode o2) {
                return o1.getIdentifier().compareTo(o2.getIdentifier());
            }
        });
        return list;
    }

    /**
     * for FlowFilePrioritizer
     */
    public static List<FlowFilePrioritizer> sortPriorities(final Collection<FlowFilePrioritizer> collections) {
        List<FlowFilePrioritizer> list = new ArrayList<>(collections);
        list.sort(new Comparator<FlowFilePrioritizer>() {

            @Override
            public int compare(FlowFilePrioritizer o1, FlowFilePrioritizer o2) {
                return o1.getClass().getCanonicalName().compareTo(o2.getClass().getCanonicalName());
            }
        });
        return list;
    }
}
