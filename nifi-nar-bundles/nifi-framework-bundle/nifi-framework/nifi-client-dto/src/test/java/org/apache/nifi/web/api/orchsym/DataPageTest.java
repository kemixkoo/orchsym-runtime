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

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author GU Guoqiang
 *
 */
public class DataPageTest {

    private static List<Integer> data = new ArrayList<>();

    @BeforeClass
    public static void init() {
        for (int i = 0; i < 50; i++) {
            data.add(i);
        }
    }

    @Test
    public void test_1pageSize() {
        DataPage<Integer> page = new DataPage<Integer>(data, 1, 1);

        assertEquals(50, page.getTotalSize());

        assertEquals(1, page.getCurrentPage());
        assertEquals(1, page.getPageSize());

        // 共50页， 每页1条
        assertEquals(50, page.getTotalPage());
        assertEquals(1, page.getResults().size());
    }

    @Test
    public void test_2pageSize() {
        DataPage<Integer> page;

        // page 1
        page = new DataPage<Integer>(data, 2, 1);

        assertEquals(50, page.getTotalSize());

        assertEquals(1, page.getCurrentPage());
        assertEquals(2, page.getPageSize());

        // 共25页， 每页2条
        assertEquals(25, page.getTotalPage());
        assertEquals(2, page.getResults().size());

        // page 2
        page = new DataPage<Integer>(data, 2, 2);

        assertEquals(50, page.getTotalSize());

        assertEquals(2, page.getCurrentPage());
        assertEquals(2, page.getPageSize());

        // 共25页， 每页2条
        assertEquals(25, page.getTotalPage());
        assertEquals(2, page.getResults().size());
    }

    @Test
    public void test_3pageSize() {
        DataPage<Integer> page;

        // page 2
        page = new DataPage<Integer>(data, 3, 16);

        assertEquals(50, page.getTotalSize());

        assertEquals(16, page.getCurrentPage());
        assertEquals(3, page.getPageSize());

        // 共17页， 每页3条
        assertEquals(17, page.getTotalPage());
        assertEquals(3, page.getResults().size());

        // last page
        page = new DataPage<Integer>(data, 3, 17);

        assertEquals(50, page.getTotalSize());

        assertEquals(17, page.getCurrentPage());
        assertEquals(3, page.getPageSize());

        // 共17页， 最后一页2条
        assertEquals(17, page.getTotalPage());
        assertEquals(2, page.getResults().size());

        // wrong page
        page = new DataPage<Integer>(data, 3, 18);

        assertEquals(50, page.getTotalSize());

        assertEquals(18, page.getCurrentPage());
        assertEquals(3, page.getPageSize());

        assertEquals(17, page.getTotalPage());
        assertEquals(0, page.getResults().size());
    }

    @Test
    public void test_25pageSize() {
        DataPage<Integer> page;

        // page 2
        page = new DataPage<Integer>(data, 25, 1);

        assertEquals(50, page.getTotalSize());

        assertEquals(1, page.getCurrentPage());
        assertEquals(25, page.getPageSize());

        // 共2页， 每页25条
        assertEquals(2, page.getTotalPage());
        assertEquals(25, page.getResults().size());

        // last page
        page = new DataPage<Integer>(data, 25, 2);

        assertEquals(50, page.getTotalSize());

        assertEquals(2, page.getCurrentPage());
        assertEquals(25, page.getPageSize());

        // 共2页， 最后一页
        assertEquals(2, page.getTotalPage());
        assertEquals(25, page.getResults().size());

        // wrong page
        page = new DataPage<Integer>(data, 25, 3);

        assertEquals(50, page.getTotalSize());

        assertEquals(3, page.getCurrentPage());
        assertEquals(25, page.getPageSize());

        assertEquals(2, page.getTotalPage());
        assertEquals(0, page.getResults().size());
    }

    @Test
    public void test_49pageSize() {
        DataPage<Integer> page;

        // page 1
        page = new DataPage<Integer>(data, 49, 1);

        assertEquals(50, page.getTotalSize());

        assertEquals(1, page.getCurrentPage());
        assertEquals(49, page.getPageSize());

        // 共2页， 第1页49
        assertEquals(2, page.getTotalPage());
        assertEquals(49, page.getResults().size());

        // last page
        page = new DataPage<Integer>(data, 49, 2);

        assertEquals(50, page.getTotalSize());

        assertEquals(2, page.getCurrentPage());
        assertEquals(49, page.getPageSize());

        // 共2页， 最后一页
        assertEquals(2, page.getTotalPage());
        assertEquals(1, page.getResults().size());

        // wrong page
        page = new DataPage<Integer>(data, 49, 3);

        assertEquals(50, page.getTotalSize());

        assertEquals(3, page.getCurrentPage());
        assertEquals(49, page.getPageSize());

        assertEquals(2, page.getTotalPage());
        assertEquals(0, page.getResults().size());
    }

    @Test
    public void test_50pageSize() {
        DataPage<Integer> page;

        // page 1
        page = new DataPage<Integer>(data, 50, 1);

        assertEquals(50, page.getTotalSize());

        assertEquals(1, page.getCurrentPage());
        assertEquals(50, page.getPageSize());

        // 共1页， 第1页50
        assertEquals(1, page.getTotalPage());
        assertEquals(50, page.getResults().size());

        // wrong page
        page = new DataPage<Integer>(data, 50, 3);

        assertEquals(50, page.getTotalSize());

        assertEquals(3, page.getCurrentPage());
        assertEquals(50, page.getPageSize());

        assertEquals(1, page.getTotalPage());
        assertEquals(50, page.getResults().size());
    }

    @Test
    public void test_60pageSize() {
        DataPage<Integer> page;

        // page 1
        page = new DataPage<Integer>(data, 60, 1);

        assertEquals(50, page.getTotalSize());

        assertEquals(1, page.getCurrentPage());
        assertEquals(60, page.getPageSize());

        // 共1页， 第1页50
        assertEquals(1, page.getTotalPage());
        assertEquals(50, page.getResults().size());

        // wrong page
        page = new DataPage<Integer>(data, 60, 3);

        assertEquals(50, page.getTotalSize());

        assertEquals(3, page.getCurrentPage());
        assertEquals(60, page.getPageSize());

        assertEquals(1, page.getTotalPage());
        assertEquals(50, page.getResults().size());
    }

    @Test
    public void test_wrongPage() {
        DataPage<Integer> page;

        // page 1
        page = new DataPage<Integer>(data, -1, -1);

        assertEquals(50, page.getTotalSize());

        assertEquals(1, page.getCurrentPage());
        assertEquals(50, page.getPageSize());

        // 共1页， 第1页50
        assertEquals(1, page.getTotalPage());
        assertEquals(50, page.getResults().size());

        // wrong page
        page = new DataPage<Integer>(data, -1, 3);

        assertEquals(50, page.getTotalSize());

        assertEquals(3, page.getCurrentPage());
        assertEquals(50, page.getPageSize());

        assertEquals(1, page.getTotalPage());
        assertEquals(50, page.getResults().size());

    }
}
