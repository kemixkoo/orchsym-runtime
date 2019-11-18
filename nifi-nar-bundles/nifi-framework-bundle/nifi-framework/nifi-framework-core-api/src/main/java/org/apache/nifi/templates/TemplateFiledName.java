package org.apache.nifi.templates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 负责封装Template 增强过程中持久化字段的名称
 *
 * @author liuxun
 */
public class TemplateFiledName {
    public static final String CREATED_USER = "CREATED_USER";
    public static final String CREATED_TIME = "CREATED_TIME";
    public static final String UPLOADED_USER = "UPLOADED_USER";
    public static final String UPLOADED_TIME = "UPLOADED_TIME";
    public static final String MODIFIED_USER = "MODIFIED_USER";
    public static final String MODIFIED_TIME = "MODIFIED_TIME";
    public static final String SOURCE_TYPE = "SOURCE_TYPE";

    private static Set<String> FIELDNAMES = new HashSet<>();

    static {
        FIELDNAMES.addAll(Arrays.asList(CREATED_USER, CREATED_TIME, UPLOADED_USER, UPLOADED_TIME, MODIFIED_USER, MODIFIED_TIME, SOURCE_TYPE));
    }

    public Boolean containsFieldName(String  fieldName){
        return FIELDNAMES.contains(fieldName);
    }
}
