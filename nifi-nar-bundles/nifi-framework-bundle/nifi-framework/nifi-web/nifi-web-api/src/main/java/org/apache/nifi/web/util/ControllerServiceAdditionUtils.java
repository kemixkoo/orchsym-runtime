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

import org.apache.nifi.web.ResourceNotFoundException;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author weiwei.zhan
 */
public class ControllerServiceAdditionUtils {
    public static final String KEY_IS_CONTROLLER_SERVICE_DELETED = "KEY_IS_CONTROLLER_SERVICE_DELETED";

    public static void logicalDeletionCheck(final ControllerServiceEntity entity) {
        Objects.requireNonNull(entity);
        if (entity.getComponent().getAdditions().get(KEY_IS_CONTROLLER_SERVICE_DELETED) != null) {
            throw new ResourceNotFoundException(String.format("Unable to find Controller Service '%s'", entity.getId()));
        }
    }

    // Return true if the Controller Service has already been deleted logically
    public static Predicate<ControllerServiceEntity> CONTROLLER_SERVICE_NOT_DELETED = controllerServiceEntity ->
            controllerServiceEntity.getComponent().getAdditions().get(KEY_IS_CONTROLLER_SERVICE_DELETED) == null;
}
