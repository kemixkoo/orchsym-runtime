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
package org.apache.nifi.web.api.entity;

import org.apache.nifi.web.api.entity.Entity;

/**
 * 集群间文件同步对象
 */
public class FileContentEntity extends Entity {
    /**
     * 文件目录/子目录
     */
    private String subDir;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件二进制数据
     */
    private byte[] fileBytes;
    /**
     * 是否覆盖
     */
    private boolean overwrite;

    public FileContentEntity() {
    }

    public FileContentEntity(String subDir, String fileName, byte[] fileBytes, boolean overwrite) {
        this.subDir = subDir;
        this.fileName = fileName;
        this.fileBytes = fileBytes;
        this.overwrite = overwrite;
    }

    public String getSubDir() {
        return subDir;
    }

    public void setSubDir(String subDir) {
        this.subDir = subDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

}
