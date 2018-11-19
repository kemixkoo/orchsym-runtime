/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.web.api.dto;

import io.swagger.annotations.ApiModelProperty;
import org.apache.nifi.web.api.dto.util.DateTimeAdapter;
import org.apache.nifi.web.api.dto.util.LocaleAdapter;
import org.apache.nifi.web.api.dto.util.LocalDateAdapter;
import org.apache.nifi.web.api.dto.util.TimezoneAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.Locale;
import java.time.LocalDate;

/**
 * Contains details about this NiFi including the title and version.
 */
@XmlType(name = "about")
public class AboutDTO {

    private String title;
    private String version;

    private String uri;
    private String contentViewerUrl;
    private Date timezone;

    private String buildTag;
    private String buildRevision;
    private String buildBranch;
    private Date buildTimestamp;
    
    private Locale locale;
    private String productVersion;
    private LocalDate buildDate;

    /* getters / setters */
    /**
     * The title to be used on the page and in the About dialog.
     *
     * @return The title
     */
    @ApiModelProperty(
            value = "The title to be used on the page and in the about dialog."
    )
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The version of this NiFi.
     *
     * @return The version.
     */
    @ApiModelProperty(
            value = "The version of this NiFi."
    )
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return URI for this NiFi controller
     */
    @ApiModelProperty(
        value = "The URI for the NiFi."
    )
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the URL for the content viewer if configured
     */
    @ApiModelProperty(
        value = "The URL for the content viewer if configured."
    )
    public String getContentViewerUrl() {
        return contentViewerUrl;
    }

    public void setContentViewerUrl(String contentViewerUrl) {
        this.contentViewerUrl = contentViewerUrl;
    }

    /**
     * @return the timezone of the NiFi instance
     */
    @XmlJavaTypeAdapter(TimezoneAdapter.class)
    @ApiModelProperty(
            value = "The timezone of the NiFi instance.",
            readOnly = true,
            dataType = "string"
    )
    public Date getTimezone() {
        return timezone;
    }

    public void setTimezone(Date timezone) {
        this.timezone = timezone;
    }

    @ApiModelProperty(
            value = "Build tag"
    )
    public String getBuildTag() {
        return buildTag;
    }

    public void setBuildTag(String buildTag) {
        this.buildTag = buildTag;
    }

    @ApiModelProperty(
            value = "Build revision or commit hash"
    )
    public String getBuildRevision() {
        return buildRevision;
    }

    public void setBuildRevision(String buildRevision) {
        this.buildRevision = buildRevision;
    }

    @ApiModelProperty(
            value = "Build branch"
    )
    public String getBuildBranch() {
        return buildBranch;
    }

    public void setBuildBranch(String buildBranch) {
        this.buildBranch = buildBranch;
    }

    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @ApiModelProperty(
            value = "Build timestamp",
            dataType = "string"
    )
    public Date getBuildTimestamp() {
        return buildTimestamp;
    }

    public void setBuildTimestamp(Date buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    @XmlJavaTypeAdapter(LocaleAdapter.class)
    @ApiModelProperty(
            value = "Build Platform default Locale",
            dataType = "string"
    )
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @ApiModelProperty(
            value = "Product Version"
    )
    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @ApiModelProperty(
            value = "Build Date",
            dataType = "string"
    )
    public LocalDate getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(LocalDate buildDate) {
        this.buildDate = buildDate;
    }
    
}
