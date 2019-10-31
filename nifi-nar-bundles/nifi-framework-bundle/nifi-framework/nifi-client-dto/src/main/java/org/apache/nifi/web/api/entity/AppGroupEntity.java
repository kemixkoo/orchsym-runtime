package org.apache.nifi.web.api.entity;

/**
 * @apiNote 所有的一级group
 */
public class AppGroupEntity {
    private String id;
    private String name;
    private String comments;
    private Long createdTime;
    private Long modifiedTime;

    public AppGroupEntity() {
    }

    public AppGroupEntity(String id, String name, String comments, Long createdTime, Long modifiedTime) {
        this.id = id;
        this.name = name;
        this.comments = comments;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AppGroupEntity){
            AppGroupEntity app = (AppGroupEntity) obj;
            return this.id.equals(app.getId());
        }
        return false;
    }
}
