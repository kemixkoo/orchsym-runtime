/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orchsym.udc.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.nifi.bundle.BundleExtensionDiscover;
import org.apache.nifi.util.NiFiProperties;

import com.orchsym.udc.CollectorService;

import net.minidev.json.JSONObject;

/**
 * @author GU Guoqiang
 *
 */
public class UsageDataManager {
    private static UsageDataManager INSTANCE;

    public static UsageDataManager get() {
        if (null == INSTANCE) {
            synchronized (UsageDataManager.class) {
                if (null == INSTANCE) {
                    INSTANCE = new UsageDataManager();
                    INSTANCE.load();
                }
            }
        }
        return INSTANCE;
    }

    String FIELD_TIMESTAMP = "timestamp";

    private static final String KEY_REPO_DIR = "orchsym.usage.repository.directory";
    private static final String DEFAULT_REPO_DIR = "./usage_repository";

    private static final String KEY_REPO_REFRESH = "orchsym.usage.repository.autorefresh.interval";
    private static final String DEFAULT_REFRESH = "12 hours";

    private static final String KEY_REPO_FILE_PATTERN = "orchsym.usage.repository.file.pattern";
    private static final String FILE_PREFIX = "orchsym_";
    private static final String FILE_EXT = ".ud";
    private static final String DEFAULT_FILE_PATTERN = "yyyy-MM-dd"; // orchsym_2019-08-06.ud

    private List<CollectorService> servicesList = new ArrayList<>();
    private final NiFiProperties properties;

    private UsageDataManager() {
        properties = NiFiProperties.createBasicNiFiProperties(null, null);
    }

    @SuppressWarnings("unchecked")
    private void load() {
        BundleExtensionDiscover.discoverExtensions(CollectorService.class).forEach(s -> {
            if (s instanceof CollectorService) {
                servicesList.add((CollectorService) s);
            }
        });
    }

    public JSONObject collect(final Map<String, Object> parameters) {
        JSONObject result = new JSONObject();

        result.put(FIELD_TIMESTAMP, LocalDateTime.now().toString());

        servicesList.forEach(s -> result.merge(s.collect(parameters)));

        return result;
    }

    public Path getUsageRepositoryPath() {
        return Paths.get(properties.getProperty(KEY_REPO_DIR, DEFAULT_REPO_DIR));
    }

    public String getAutoRefreshInterval() {
        return properties.getProperty(KEY_REPO_REFRESH, DEFAULT_REFRESH);
    }

    private String getFilePattern() {
        return properties.getProperty(KEY_REPO_FILE_PATTERN, DEFAULT_FILE_PATTERN);
    }

    /**
     * TODO, Need consider the cluster to synchronize the data file, or distribute the data to all nodes.
     */
    public List<File> getCollectorFiles() {
        final File folder = getUsageRepositoryPath().toFile();
        if (folder.exists()) {
            File[] udcFiles = folder.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    if (file.isFile()) {
                        final String name = file.getName();
                        return name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXT);
                    }
                    return false;
                }
            });

            if (null != udcFiles) {
                return Arrays.asList(udcFiles);
            }

        }
        return Collections.emptyList();
    }

    public List<String> getDateOfCollectorFiles() {
        return getCollectorFiles().stream().map(f -> f.getName()).map(n -> n.substring(FILE_PREFIX.length(), n.lastIndexOf(FILE_EXT))).collect(Collectors.toList());
    }

    public void saveToRepository() throws IOException {
        final File folder = getUsageRepositoryPath().toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        final JSONObject result = collect(null);

        final String ts = result.getAsString(FIELD_TIMESTAMP);
        LocalDateTime datetime = LocalDateTime.now();
        if (null != ts) {
            datetime = LocalDateTime.parse(ts);
        } else {
            result.put(FIELD_TIMESTAMP, datetime.toString());
        }
        final DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern(getFilePattern());
        final String fileFormat = datetime.format(datetimeFormatter);
        File file = new File(folder, FILE_PREFIX + fileFormat + FILE_EXT);
        try (FileWriter fw = new FileWriter(file)) {
            result.writeJSONString(fw);
        }
    }
}
