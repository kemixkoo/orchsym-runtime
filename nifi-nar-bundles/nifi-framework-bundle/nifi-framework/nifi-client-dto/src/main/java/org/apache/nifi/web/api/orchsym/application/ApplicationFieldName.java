/**
 * 
 */
package org.apache.nifi.web.api.orchsym.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;

/**
 * @author GU Guoqiang
 *
 */
public class ApplicationFieldName {

    public static Map<String, String> getCreatingAdditions(Map<String, String> originalAdditions, String userId) {
        Map<String, String> additions = new HashMap<>();
        additions.put(AdditionConstants.KEY_CREATED_USER, userId);
        additions.put(AdditionConstants.KEY_CREATED_TIMESTAMP, Long.toString(System.currentTimeMillis()));
        if (!Objects.isNull(originalAdditions)) {
            // 保留原始创建者，或者将原应用创建变为原始创建者
            if (!originalAdditions.containsKey(AdditionConstants.KEY_ORIGINAL_CREATED_USER) && originalAdditions.containsKey(AdditionConstants.KEY_CREATED_USER)
                    && StringUtils.isNotBlank(originalAdditions.get(AdditionConstants.KEY_CREATED_USER))) {
                additions.put(AdditionConstants.KEY_ORIGINAL_CREATED_USER, originalAdditions.get(AdditionConstants.KEY_CREATED_USER));
            }
            if (!originalAdditions.containsKey(AdditionConstants.KEY_ORIGINAL_CREATED_TIMESTAMP) && originalAdditions.containsKey(AdditionConstants.KEY_CREATED_TIMESTAMP)
                    && StringUtils.isNotBlank(additions.get(AdditionConstants.KEY_CREATED_TIMESTAMP))) {
                additions.put(AdditionConstants.KEY_ORIGINAL_CREATED_TIMESTAMP, originalAdditions.get(AdditionConstants.KEY_CREATED_TIMESTAMP));
            }
        }

        return additions;
    }
}
