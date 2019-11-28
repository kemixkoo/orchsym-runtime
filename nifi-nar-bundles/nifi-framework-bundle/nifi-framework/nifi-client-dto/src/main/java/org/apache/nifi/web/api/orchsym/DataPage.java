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
 * 
 * @author liuxun
 * @param <T>
 *            结果列表的数据类型
 */
public class DataPage<T> {
    private int currentPage = 1; // 当前页码
    private int totalSize; // 全部数量
    private int pageSize = 10; // 每页大小
    private int totalPage; // 分页页数

    private List<T> results; // 当前页结果集

    public DataPage() {
    }

    public DataPage(List<T> list, int pageSize, int currentPage) {
        if (currentPage < 1) { // 必须从1开始，否则强制第1页
            currentPage = 1;
        }
        final int totalSize = list.size();
        if (pageSize < 1) {// 当每页大小为-1，表示不分页，即只有1页
            pageSize = totalSize;
        }

        this.setCurrentPage(currentPage);
        this.setTotalSize(totalSize);
        this.setPageSize(pageSize);

        this.setTotalPage((totalSize + pageSize - 1) / pageSize);

        int index = (currentPage - 1) * pageSize;
        List<T> resultList = null;
        if (index >= totalSize) {
            resultList = new ArrayList<>();
        } else {
            int endIndex = Math.min(index + pageSize, totalSize);
            resultList = list.subList(index, endIndex);
        }
        this.setResults(resultList);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

}
