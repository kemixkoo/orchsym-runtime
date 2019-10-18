package org.apache.nifi.web.api.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.nifi.web.api.dto.stats.ServiceCounterDTO;

/**
 * A serialized representation of this class can be placed in the entity body of a response to the API. This particular entity holds a reference to the counter of controller services, processors .
 */
@XmlRootElement(name = "StatsProcessorsEntity")
public class StatsServicesEntity extends Entity {

    private List<ServiceCounterDTO> services;

    public List<ServiceCounterDTO> getServices() {
        return services;
    }

    public void setServices(List<ServiceCounterDTO> services) {
        this.services = services;
    }

}
