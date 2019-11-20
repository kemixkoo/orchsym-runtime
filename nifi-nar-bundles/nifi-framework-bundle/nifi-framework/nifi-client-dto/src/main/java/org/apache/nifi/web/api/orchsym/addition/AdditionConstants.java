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
package org.apache.nifi.web.api.orchsym.addition;

/**
 * @author GU Guoqiang
 *
 */
public interface AdditionConstants {
    String KEY_IS_DELETED = "IS_DELETED";
    Boolean KEY_IS_DELETED_DEFAULT = Boolean.FALSE; // default is not deleted

    String KEY_IS_ENABLED = "IS_ENABLED";
    Boolean KEY_IS_ENABLED_DEFAULT = Boolean.TRUE; // default is enabled

    String KEY_CREATED_TIMESTAMP = "CREATED_TIMESTAMP";
    String KEY_CREATED_USER = "CREATED_USER";
    String KEY_ORIGINAL_CREATED_TIMESTAMP="ORIGINAL_CREATED_TIMESTAMP";
    String KEY_ORIGINAL_CREATED_USER = "ORIGINAL_CREATED_USER";
    String KEY_MODIFIED_TIMESTAMP = "MODIFIED_TIMESTAMP";
    String KEY_MODIFIED_USER = "MODIFIED_USER";
}
