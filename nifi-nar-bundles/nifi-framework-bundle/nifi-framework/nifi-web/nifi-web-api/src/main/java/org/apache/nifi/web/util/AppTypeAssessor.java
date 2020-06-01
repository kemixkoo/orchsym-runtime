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

import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.groups.ProcessGroup;

public class AppTypeAssessor {
    public static enum AppType {
        GENERAL(false), //
        API(true),//

        ;
        private boolean block;

        AppType(boolean block) {
            this.block = block;
        }

        boolean isBlocked() {
            return this.block;
        }

        public String getName() {
            return name();
        }
    }

    public static final String judge(final ProcessGroup group) {
        return judgeType(group).getName();

    }

    public static final AppType judgeType(final ProcessGroup group) {
        for (ProcessorNode node : group.getProcessors()) {
            if (node.getComponentClass().getName().equals("org.apache.nifi.processors.standard.HandleHttpRequest")) {
                return AppType.API; // blocked
            }
        }

        // children
        for (ProcessGroup child : group.getProcessGroups()) {
            final AppType childType = judgeType(child);
            if (childType.isBlocked()) {
                return childType;
            }
        }

        return AppType.GENERAL;

    }
}
