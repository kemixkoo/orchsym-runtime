package com.baishancloud.orchsym.processors.sap.option;

import java.util.Arrays;

import org.apache.nifi.components.AllowableValue;

import com.baishancloud.orchsym.processors.sap.i18n.Messages;

/**
 * @author GU Guoqiang
 *
 */
public enum ContainerOption {

    ARRAY("array", Messages.getString("ContainerOption.array")), //$NON-NLS-1$ //$NON-NLS-2$
    NONE("none", Messages.getString("ContainerOption.none")), //$NON-NLS-1$ //$NON-NLS-2$
    ;
    private String value;
    private String displayName;

    private ContainerOption(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ContainerOption get(String value) {
        if (value != null) {
            for (ContainerOption op : ContainerOption.values()) {
                if (op.getValue().equalsIgnoreCase(value)) {
                    return op;
                }
            }
        }
        return ContainerOption.NONE;
    }

    public static AllowableValue[] getAll() {
        return Arrays.asList(ContainerOption.values()).stream().map(c -> new AllowableValue(c.value, c.displayName)).toArray(AllowableValue[]::new);
    }

}
