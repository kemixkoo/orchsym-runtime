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
package org.apache.nifi.web.api.dto.dbcp;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author weiwei.zhan
 *
 *         Details for a DBCP status.
 */
public class DbcpMetadataDTO {
    private String id;

    private String driverClassName;
    private String dbUser;
    private String dbPassword;
    private String connectionUrl;
    private String[] driverLocations;
    private Map<String, String> connectionProperties;

    private String state;
    private String status;

    /**
     * Database service id.
     * 
     * @return The database service id.
     */
    @ApiModelProperty(value = "The Database service id.")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Database connection url.
     * 
     * @return The database connection url.
     */
    @ApiModelProperty(value = "The database connection url.")
    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    /**
     * Database user.
     * 
     * @return The database user.
     */
    @ApiModelProperty(value = "The database user.")
    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    /**
     * Database password.
     * 
     * @return The database password.
     */
    @ApiModelProperty(value = "The database password.")
    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    /**
     * Database driver class name.
     * 
     * @return The database driver class name.
     */
    @ApiModelProperty(value = "The database Driver class name.")
    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * Database driver locations.
     * 
     * @return The database driver locations.
     */
    @ApiModelProperty(value = "The database driver locations.")
    public String[] getDriverLocations() {
        return driverLocations;
    }

    public void setDriverLocations(String[] driverLocations) {
        this.driverLocations = driverLocations;
    }

    /**
     * Database connection properties
     * 
     * @return The database connection properties.
     */
    @ApiModelProperty(value = "The database connection properties.")
    public Map<String, String> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Map<String, String> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    /**
     * 
     * @return The status of database service to connect.
     */
    @ApiModelProperty(value = "The status of database service to connect.", allowableValues = "CONNECTED, DISCONNECTED")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The state of this controller service. Possible values are ENABLED, ENABLING, DISABLED, DISABLING
     */
    @ApiModelProperty(value = "The state of the controller service.", allowableValues = "ENABLED, ENABLING, DISABLED, DISABLING")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
