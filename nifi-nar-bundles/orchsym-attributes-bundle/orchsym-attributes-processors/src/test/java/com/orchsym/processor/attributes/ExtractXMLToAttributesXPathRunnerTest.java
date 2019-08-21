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
package com.orchsym.processor.attributes;

import java.util.List;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author GU Guoqiang
 * 
 *         Because need ROOT element "Data" for XML, so the result have "ALL.Data.xxx" or such
 */
public class ExtractXMLToAttributesXPathRunnerTest extends AbstractAttributesRunnerTest {

    @BeforeClass
    public static void init() throws Exception {
        testDataContent = loadContents("test.xml");
    }

    @Before
    public void before() {
        runner = TestRunners.newTestRunner(new ExtractXMLToAttributes());

    }

    protected MockFlowFile runForSuccessFlowFile() {
        runner.enqueue(testDataContent);
        return super.runForSuccessFlowFile();
    }

    @Test
    public void test_default_for_1st_fields_with_filter() throws Exception {
        runner.setProperty(AbstractExtractToAttributesProcessor.INCLUDE_FIELDS, "(.*)a(.*)");
        runner.setProperty("ALL", "/Data");
        final MockFlowFile successFlowFile = runForSuccessFlowFile();

        successFlowFile.assertAttributeEquals("ALL.name", "数聚蜂巢");
        successFlowFile.assertAttributeEquals("ALL.age", "3");
    }

    @Test
    public void test_default_for_1st_fields_with_attr() throws Exception {
        runner.setProperty(ExtractXMLToAttributes.ALLOW_XML_ATTRIBUTES, "true");
        // runner.setProperty(AbstractExtractToAttributesProcessor.INCLUDE_FIELDS, "(.*)a(.*)");
        runner.setProperty("ALL", "/Data");
        final MockFlowFile successFlowFile = runForSuccessFlowFile();

        successFlowFile.assertAttributeEquals("ALL.name", "数聚蜂巢");
        successFlowFile.assertAttributeEquals("ALL.age", "3");
        successFlowFile.assertAttributeEquals("ALL.@xmlns", "https://www.baishan.com");
    }

    @Test
    public void test_default_for_1st_fields_with_filter_with_attr() throws Exception {
        runner.setProperty(ExtractXMLToAttributes.ALLOW_XML_ATTRIBUTES, "true");
        runner.setProperty(AbstractExtractToAttributesProcessor.INCLUDE_FIELDS, "(.*)a(.*)");
        runner.setProperty("ALL", "/Data");
        final MockFlowFile successFlowFile = runForSuccessFlowFile();

        successFlowFile.assertAttributeEquals("ALL.name", "数聚蜂巢");
        successFlowFile.assertAttributeEquals("ALL.age", "3");
        // successFlowFile.assertAttributeEquals("ALL.@xmlns", "https://www.baishan.com"); //filtered
    }

    @Test
    public void test_sub_arr_custom_all_with_attr() throws Exception {
        runner.setProperty(ExtractXMLToAttributes.ALLOW_XML_ATTRIBUTES, "true");
        super.test_sub_arr_custom_all();

        final List<MockFlowFile> flowFiles = runner.getFlowFilesForRelationship(AbstractExtractToAttributesProcessor.REL_SUCCESS);
        final MockFlowFile successFlowFile = flowFiles.get(0);

        successFlowFile.assertAttributeEquals("data.@xmlns", "https://www.baishan.com");
    }

    protected void setProp_sub_arr_custom_all() {
        runner.setProperty("data", "/Data");
    }

    @Test
    public void test_sub_arr_no_name_with_attr() throws Exception {
        runner.setProperty(ExtractXMLToAttributes.ALLOW_XML_ATTRIBUTES, "true");
        super.test_sub_arr_no_name();

        final List<MockFlowFile> flowFiles = runner.getFlowFilesForRelationship(AbstractExtractToAttributesProcessor.REL_SUCCESS);
        final MockFlowFile successFlowFile = flowFiles.get(0);

        successFlowFile.assertAttributeEquals("@xmlns", "https://www.baishan.com");
    }

    protected void setProp_sub_arr_no_name() {
        runner.setProperty("ABC", "/Data");
    }

    protected void setProp_arr_dynamic() {
        runner.setProperty("info", "/Data/details");
        runner.setProperty("links", "/Data/links");
        runner.setProperty("the_name", "/Data/name");
        runner.setProperty("url", "/Data/url");
    }

    protected void setProp_sub_arr_dynamic() {
        runner.setProperty("info", "/Data/details");
        runner.setProperty("links", "/Data/links");
        runner.setProperty("the_name", "/Data/name");
        runner.setProperty("url_2", "/Data/links[1]");
    }

    protected void setProp_arr_index() {
        runner.setProperty("links", "/Data/links[1]|/Data/links[3]");
        runner.setProperty("the_name", "/Data/name");
        runner.setProperty("url_index1", "/Data/links[2]");
    }

    protected void setProp_simple_arr() {
        runner.setProperty("the_tags", "/Data/details/tags");
    }

}
