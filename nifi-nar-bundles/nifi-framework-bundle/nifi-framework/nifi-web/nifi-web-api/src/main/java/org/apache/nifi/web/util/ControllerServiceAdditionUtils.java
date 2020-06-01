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
package org.apache.nifi.web.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.nifi.additions.TypeAdditions;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.util.ProcessUtil;
import org.apache.nifi.web.ResourceNotFoundException;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.orchsym.addition.AdditionConstants;

/**
 * @author weiwei.zhan
 */
public class ControllerServiceAdditionUtils {

    public static void logicalDeletionCheck(final ControllerServiceEntity entity) {
        Objects.requireNonNull(entity);
        boolean deleted = ProcessUtil.getAdditionBooleanValue(entity.getComponent().getAdditions(), AdditionConstants.KEY_IS_DELETED, AdditionConstants.KEY_IS_DELETED_DEFAULT);
        if (deleted) {
            throw new ResourceNotFoundException(String.format("The service '%s' has been deleted", entity.getId()));
        }
    }

    // Return false if the Controller Service has already been deleted logically
    public static Predicate<ControllerServiceEntity> CONTROLLER_SERVICE_NOT_DELETED = controllerServiceEntity -> !ProcessUtil
            .getAdditionBooleanValue(controllerServiceEntity.getComponent().getAdditions(), AdditionConstants.KEY_IS_DELETED, AdditionConstants.KEY_IS_DELETED_DEFAULT);

    // Return true if the Controller Service has already been deleted logically
    public static Predicate<ControllerServiceEntity> CONTROLLER_SERVICE_DELETED = CONTROLLER_SERVICE_NOT_DELETED.negate();

    public static void onCreate(final ControllerServiceDTO dto) {
        Map<String, String> additions = dto.getAdditions();
        if (Objects.isNull(additions)) {
            additions = new HashMap<>();
            dto.setAdditions(additions);
        } else {
            additions = new HashMap<>(additions);
        }

        additions.put(AdditionConstants.KEY_CREATED_USER, NiFiUserUtils.getNiFiUserIdentity());
        if (!additions.containsKey(AdditionConstants.KEY_CREATED_TIMESTAMP)) {
            additions.put(AdditionConstants.KEY_CREATED_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        }
    }

    public static void onUpdate(final ControllerServiceDTO dto) {
        Map<String, String> additions = dto.getAdditions();
        if (Objects.isNull(additions)) {
            additions = new HashMap<>();
            dto.setAdditions(additions);
        } else {
            additions = new HashMap<>(additions);
        }
        additions.put(AdditionConstants.KEY_MODIFIED_USER, NiFiUserUtils.getNiFiUserIdentity());
        if (!additions.containsKey(AdditionConstants.KEY_MODIFIED_TIMESTAMP)) {
            additions.put(AdditionConstants.KEY_MODIFIED_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        }
    }

}
