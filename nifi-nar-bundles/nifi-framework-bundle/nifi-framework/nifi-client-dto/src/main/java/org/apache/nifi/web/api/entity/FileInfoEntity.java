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
public class FileInfoEntity extends Entity {
    /**
     * 文件目录/子目录
     */
    private String subDir;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件正则匹配
     */
    private String filePattern;
    /**
     * 递归包含子目录
     */
    private boolean recursive;

    public FileInfoEntity() {
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

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

}
