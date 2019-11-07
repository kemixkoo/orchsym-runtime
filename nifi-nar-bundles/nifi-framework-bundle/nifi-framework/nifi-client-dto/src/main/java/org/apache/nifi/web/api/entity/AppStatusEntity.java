package org.apache.nifi.web.api.entity;

/**
 * @author liuxun
 */
public class AppStatusEntity {
    private Boolean isEnabled;
    private Boolean isRecover;

    public AppStatusEntity() {
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public Boolean getRecover() {
        return isRecover;
    }

    public void setRecover(Boolean recover) {
        isRecover = recover;
    }
}
