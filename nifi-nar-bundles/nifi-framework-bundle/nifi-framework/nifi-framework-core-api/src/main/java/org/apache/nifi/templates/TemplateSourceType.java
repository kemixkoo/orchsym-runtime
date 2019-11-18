package org.apache.nifi.templates;

/**
 * 模板来源
 * @author liuxun
 */
public enum TemplateSourceType {
    /**
     * 0: 来源类型是上传
     */
    UPLOADED_TYPE(0),
    /**
     * 1: 来源类型是官方内置
     */
    OFFICIAL_TYPE(1),
    /**
     * 2: 来源类型是另存为(一键下载类型)
     */
    SAVE_AS_TYPE(2),
    ;
    private int value = 0;
    private TemplateSourceType(int value) {    //    必须是private的，否则编译错误
        this.value = value;
    }

    public static TemplateSourceType valueOf(int value) {    //    手写的从int到enum的转换函数
        switch (value) {
            case 0:
                return UPLOADED_TYPE;
            case 1:
                return OFFICIAL_TYPE;
            case 2:
                return SAVE_AS_TYPE;
            default:
                return null;
        }
    }

    public int value() {
        return this.value;
    }


}
