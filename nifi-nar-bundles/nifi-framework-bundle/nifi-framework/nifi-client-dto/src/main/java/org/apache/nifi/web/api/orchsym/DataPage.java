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
package org.apache.nifi.web.api.orchsym;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页类
 * @author liuxun
 * @param <T> 结果列表的数据类型
 */
public class DataPage<T>{
    private Integer pageSize = 10;
    private Integer totalSize;
    private Integer totalPage;
    private Integer currentPage = 1;
    private List<T> results ;

    public DataPage() {
    }

    public DataPage(List<T> list, Integer pageSize, Integer currentPage){
        this.setPageSize(pageSize);
        this.setCurrentPage(currentPage);
        this.setTotalSize(list.size());
        this.setTotalPage((this.totalSize + this.pageSize - 1) / pageSize);
        int index = (currentPage - 1) * pageSize;
        List<T> resultList = null;
        if (index >= totalSize) {
            resultList = new ArrayList<>();
        } else {
            int endIndex = Math.min(index + this.pageSize, this.totalSize);
            resultList = list.subList(index, endIndex);
        }
        this.setResults(resultList);
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
