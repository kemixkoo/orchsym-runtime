package org.apache.nifi.web.api.orchsym.template;

import java.util.Objects;

/**
 * 模板收藏实体类
 * @author liuxun
 */
public class TemplateFavority implements Comparable<TemplateFavority>{
    private String templateId;
    private Long createdTime;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemplateFavority)) {
            return false;
        }
        TemplateFavority that = (TemplateFavority) o;
        return templateId.equals(that.templateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateId);
    }


    @Override
    public int compareTo(TemplateFavority o) {
        return this.getCreatedTime().compareTo(o.getCreatedTime());
    }
}
