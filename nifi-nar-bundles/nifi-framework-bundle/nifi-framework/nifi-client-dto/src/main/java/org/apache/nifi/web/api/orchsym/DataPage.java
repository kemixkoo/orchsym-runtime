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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 分页类
 * 
 * @author liuxun
 * @param <T>
 *            结果列表的数据类型
 */
public class DataPage<T> {
    private int currentPage = 1; // 当前页码
    private int totalSize; // 结果总数
    private int pageSize = 10; // 指定分页的每页大小
    private int totalPage = 1; // 总页数，默认仅一页

    private List<T> results; // 当前页结果列表

    public DataPage() {
    }

    public DataPage(final List<T> list, final int pageSize, final int currentPage) {
        this.setTotalSize(Objects.isNull(list) ? 0 : list.size()); // 结果总数
        this.setPageSize(pageSize < 1 ? getTotalSize() : pageSize);// 当每页大小<1，表示不分页, 一般设置为-1，即只有1页,且每页大小同list大小
        this.setCurrentPage(currentPage < 1 ? 1 : currentPage); // 须从1开始，否则默认从第1页开始

        if (getTotalSize() > 0) {// 有数据时，即list>0
            if (getPageSize() >= getTotalSize()) {// 每页大小等同或高过总数，则全部返回原列表
                this.setTotalPage(1);
                this.setResults(list);// 直接返回原始列表
            } else {
                this.setTotalPage((getTotalSize() + getPageSize() - 1) / getPageSize());

                int index = (getCurrentPage() - 1) * getPageSize();
                List<T> pageList = null;
                if (index >= getTotalSize()) {
                    pageList = new ArrayList<>();
                } else {
                    int endIndex = Math.min(index + getPageSize(), getTotalSize());
                    pageList = list.subList(index, endIndex);
                }
                this.setResults(pageList);
            }
        } else {
            this.setTotalPage(1); // 无数据时，仍旧保持1页
            this.setResults(Collections.emptyList());
        }
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
