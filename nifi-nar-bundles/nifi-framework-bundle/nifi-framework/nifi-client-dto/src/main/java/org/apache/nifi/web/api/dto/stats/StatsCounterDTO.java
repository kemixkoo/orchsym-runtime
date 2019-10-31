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
package org.apache.nifi.web.api.dto.stats;

import java.util.List;

/**
 * 
 * @author GU Guoqiang
 *
 */
public class StatsCounterDTO {
    private SummaryCounterDTO summary;

    private List<ComponentCounterDTO> components;
    private List<ServiceCounterDTO> services;

    public SummaryCounterDTO getSummary() {
        return summary;
    }

    public void setSummary(SummaryCounterDTO summary) {
        this.summary = summary;
    }

    

    public List<ComponentCounterDTO> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentCounterDTO> components) {
        this.components = components;
    }

    public List<ServiceCounterDTO> getServices() {
        return services;
    }

    public void setServices(List<ServiceCounterDTO> services) {
        this.services = services;
    }

}
