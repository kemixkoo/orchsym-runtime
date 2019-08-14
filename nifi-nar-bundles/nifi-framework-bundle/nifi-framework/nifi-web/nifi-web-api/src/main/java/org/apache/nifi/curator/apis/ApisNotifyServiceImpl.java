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
package org.apache.nifi.curator.apis;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.nifi.apis.ApisNotifyService;
import org.apache.nifi.cluster.coordination.node.ClusterRoles;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.controller.cluster.ZooKeeperClientConfig;
import org.apache.nifi.controller.leader.election.LeaderElectionManager;
import org.apache.nifi.curator.CuratorFactory;
import org.apache.nifi.curator.config.PathConfig;
import org.apache.nifi.registry.api.ApiInfo;
import org.apache.nifi.util.NiFiProperties;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuxun
 * @apiNote api 通知实现类
 */
public class ApisNotifyServiceImpl implements ApisNotifyService {
    private static final Logger logger = LoggerFactory.getLogger(ApisNotifyServiceImpl.class);

    private CuratorFactory curatorFactory;

    private LeaderElectionManager leaderElectionManager;

    private FlowController flowController;

    private NiFiProperties properties;

    private enum Method {
        /**
         * 声明方法的标识
         */
        REGISTER,
        UNREGISTER,
        UPDATE
    }


    @Override
    public void register(ApiInfo apiInfo) {
        logger.debug("++++register  apiId={} +++++", apiInfo.id);
        handleNotify(apiInfo, apiInfo.id, Method.REGISTER);
    }

    @Override
    public void unregister(String apiId) {
        logger.debug("++++unregister  apiId={} +++++", apiId);
        handleNotify(null, apiId, Method.UNREGISTER);
    }

    @Override
    public void update(ApiInfo apiInfo) {
        logger.debug("++++update  apiId={} +++++", apiInfo.id);
        handleNotify(apiInfo, apiInfo.id, Method.UPDATE);
    }

    /**
     * @param apiInfo
     * @param apiId
     * @param method  方法标识
     * @apiNote 抽取统一的处理代码块
     */
    private void handleNotify(ApiInfo apiInfo, String apiId, Method method) {
        verifyAndWait();
        if (apiId == null) {
            return;
        }
        final CuratorFramework cf = getCuratorFramework();
        final String apiPath = getAPINotifyPath(apiId);
        String apiInfoJsonStr = null;
        if (apiInfo != null) {
            apiInfoJsonStr = JSON.toJSONString(apiInfo);
        }
        if (!isPathExists(apiPath, cf)) {
            if (method.equals(Method.REGISTER) || method.equals(Method.UPDATE)) {
                // 创建并设置数据
                try {
                    // 涉及API的管理交互，此处设置为异步执行
                    cf.create().creatingParentsIfNeeded()
                            .withMode(CreateMode.PERSISTENT).inBackground(new BackgroundCallback() {
                        @Override
                        public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                            logger.debug("+++++Register/UPDATE API 通知完毕+++++++++");
                            logger.debug("++++event.code={}+++++event.type={}", event.getResultCode(), event.getType());
                        }
                    }).forPath(apiPath, apiInfoJsonStr.getBytes("UTF-8"));
                } catch (Exception e) {
                    logger.error("e.message={}\n e.stack={}", e.getMessage(),e.getStackTrace());
                }
            }
        } else {
            if (method.equals(Method.REGISTER) || method.equals(Method.UPDATE)) {
                // 更新数据
                try {
                    cf.setData().inBackground(new BackgroundCallback() {
                        @Override
                        public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                            logger.debug("+++++Register/UPDATE API 通知完毕+++++++++");
                            logger.debug("++++event.code={}+++++event.type={}", event.getResultCode(), event.getType());
                        }
                    }).forPath(apiPath, apiInfoJsonStr.getBytes("UTF-8"));
                } catch (Exception e) {
                    logger.error("e.message={}\n e.stack={}", e.getMessage(),e.getStackTrace());
                }
            } else if (method.equals(Method.UNREGISTER)) {
                // 删除api节点
                try {
                    cf.delete().guaranteed().inBackground(new BackgroundCallback() {
                        @Override
                        public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                            logger.debug("+++++UnRegister API 通知完毕+++++++++");
                            logger.debug("++++event.code={}+++++event.type={}", event.getResultCode(), event.getType());
                        }
                    }).forPath(apiPath);
                } catch (Exception e) {
                    logger.error("e.message={}\n e.stack={}", e.getMessage(),e.getStackTrace());
                }
            }
        }
    }

    /**
     * @return
     * @apiNote 获取有效的curator客户端
     */
    private CuratorFramework getCuratorFramework() {
        while (curatorFactory.getCuratorFramework() == null) {
            logger.error("++++ 集群信息通知连接获得curatorFramework 失败, 重新获取中......");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        return curatorFactory.getCuratorFramework();
    }

    /**
     * @apiNote 判断是否需要 以及等待初始化执行API通知
     */
    private void verifyAndWait() {
        if (!properties.isNode()) {
            return;
        }

        if (!flowController.isClustered()) {
            logger.debug("++++++current node is disconnected status, cant notify api info");
            return;
        }

        while (!(isElected() && flowController.isInitialized())) {
            try {
                Thread.sleep(1000);
                logger.debug("++++ waiting elect primary or init flow controller+++++++");
            } catch (InterruptedException e) {
                logger.error("e.message={}\n e.stack={}", e.getMessage(),e.getStackTrace());
            }
        }

        if (!flowController.isPrimary()) {
            logger.debug("++++++current node is not primary for notify api +++++++");
            return;
        }
    }

    /**
     * @return 判断是否选举完毕
     */
    private boolean isElected() {
        return !StringUtils.isEmpty(leaderElectionManager.getLeader(ClusterRoles.CLUSTER_COORDINATOR));
    }

    /**
     * @param apiId 组件ID
     * @return 返回指定API组件在ZooKeeper上的绝对路径
     */
    private String getAPINotifyPath(String apiId) {
        ZooKeeperClientConfig zkConfig = ZooKeeperClientConfig.createConfig(properties);
        return PathConfig.getApisPath(zkConfig.getRootPath(), apiId);
    }

    private boolean isPathExists(String apiPath, CuratorFramework curatorFramework) {
        Stat stat = null;
        try {
            stat = curatorFramework.checkExists().forPath(apiPath);
        } catch (Exception e) {
            logger.error("e.message={}\n e.stack={}", e.getMessage(),e.getStackTrace());
        }
        return stat != null;
    }

    public void setCuratorFactory(CuratorFactory curatorFactory) {
        this.curatorFactory = curatorFactory;
    }

    public void setLeaderElectionManager(LeaderElectionManager leaderElectionManager) {
        this.leaderElectionManager = leaderElectionManager;
    }

    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }

    public void setProperties(NiFiProperties properties) {
        this.properties = properties;
    }
}
