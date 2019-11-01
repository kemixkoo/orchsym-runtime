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
package org.apache.nifi.web.api;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.authorization.RequestAction;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.web.StandardNiFiServiceFacade;
import org.apache.nifi.web.api.dto.search.ComponentSearchResultDTO;
import org.apache.nifi.web.api.dto.search.SearchResultsDTO;
import org.apache.nifi.web.api.entity.*;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * @author liuxun
 * @apiNote 处理app的相关功能
 */
@Component
@Path("/application")
@Api(value = "/application", description = "app search")
public class OrchsymApplicationResource extends AbsOrchsymResource {
    /**
     * @apiNote group中相关的创建和修改时间
     */
    private static final String createdTime = StandardNiFiServiceFacade.createdTime;
    private static final String modifiedTime = StandardNiFiServiceFacade.modifiedTime;

    /**
     * 为APP实体类赋值
     * @param groupEntity
     * @param groupId
     */
    private void setTimeStampForApp(AppGroupEntity groupEntity, String  groupId){
        final ProcessGroup group = flowController.getGroup(groupId);
        if (group == null){
            return;
        }
        groupEntity.setId(group.getIdentifier());
        groupEntity.setName(group.getName());
        groupEntity.setComments(group.getComments());

        final Map<String, String> additions = group.getAdditions();
        if (additions != null && additions.containsKey(createdTime)){
            long createTime = Long.parseLong(additions.get(createdTime));
            groupEntity.setCreatedTime(createTime);
        }

        if (additions != null && additions.containsKey(modifiedTime)){
            long modifyTime = Long.parseLong(additions.get(modifiedTime));
            groupEntity.setModifiedTime(modifyTime);
        }

    }

    /**
     *
     * @param value
     * @param page
     * @param pageSize
     * @param sortedField 有以下几个取值类型: name createdTime modifiedTime 名称、创建时间、修改时间
     * @return
     * @throws InterruptedException
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/search-results")
    @ApiOperation(value = "Performs a search against this NiFi using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    @ApiResponses(value = { @ApiResponse(code = 400, message = "NiFi was unable to complete the request because it was invalid. The request should not be retried without modification."),
            @ApiResponse(code = 401, message = "Client could not be authenticated."), @ApiResponse(code = 403, message = "Client is not authorized to make this request."),
            @ApiResponse(code = 409, message = "The request was valid but NiFi was not in the appropriate state to process it. Retrying the same request later may be successful.") })
    public Response searchFlowAPPGroup(
            @QueryParam("q") @DefaultValue(StringUtils.EMPTY) String value,
            @QueryParam("page") @DefaultValue("1") Integer page,
            @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @QueryParam("sortedField") @DefaultValue("name") String sortedField,
            @QueryParam("isDesc") @DefaultValue("true") Boolean isDesc

    ) throws InterruptedException {

        List<AppGroupEntity> appGroupEntityList = new ArrayList<>();

        // 进行数据封装抽取
        final SearchResultsDTO results = serviceFacade.searchAppsOfController(value,flowController.getRootGroupId());
        final List<ComponentSearchResultDTO> processGroupResults = results.getProcessGroupResults();
        for (ComponentSearchResultDTO dto : processGroupResults){
            final AppGroupEntity appGroupEntity = new AppGroupEntity();
            setTimeStampForApp(appGroupEntity, dto.getId());
            appGroupEntityList.add(appGroupEntity);
        }

        // 进行排序
        Collections.sort(appGroupEntityList, new Comparator<AppGroupEntity>() {
            @Override
            public int compare(AppGroupEntity o1, AppGroupEntity o2) {
                if ("name".equalsIgnoreCase(sortedField)){
                    return isDesc? o2.getName().compareToIgnoreCase(o1.getName()) : o1.getName().compareToIgnoreCase(o2.getName());
                }else if ("createdTime".equalsIgnoreCase(sortedField)){
                    return isDesc? o2.getCreatedTime().compareTo(o1.getCreatedTime()): o1.getCreatedTime().compareTo(o2.getCreatedTime());
                }else if ("modifiedTime".equalsIgnoreCase(sortedField)){
                    return isDesc? o2.getModifiedTime().compareTo(o1.getModifiedTime()) : o1.getModifiedTime().compareTo(o2.getModifiedTime());
                }else {
                    return isDesc? o2.getId().compareTo(o1.getId()) : o1.getId().compareTo(o2.getId());
                }
            }
        });


        // 处理分页
        // 总条数 与 总页数
        int totalSize = appGroupEntityList.size();
        int totalPage = (totalSize + pageSize - 1) / pageSize;
        int index = (page - 1) * pageSize;
        int currentPage = page;

        List<AppGroupEntity> resultList = null;
        if (index >= totalSize ){
            resultList = new ArrayList<>();
        }else {
            int endIndex = Math.min(index + pageSize, totalSize);
            resultList = appGroupEntityList.subList(index, endIndex);
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("totalSize",totalSize);
        resultMap.put("totalPage",totalPage);
        resultMap.put("currentPage",currentPage);
        resultMap.put("results", resultList);

        // generate the response
        return noCache(Response.ok(resultMap)).build();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/component/{appId}/search-results")
    @ApiOperation(value = "Performs a search against this NiFi using the specified search term", notes = "Only search results from authorized components will be returned.", response = SearchResultsEntity.class, authorizations = {
            @Authorization(value = "Read - /flow") })
    @ApiResponses(value = { @ApiResponse(code = 400, message = "NiFi was unable to complete the request because it was invalid. The request should not be retried without modification."),
            @ApiResponse(code = 401, message = "Client could not be authenticated."), @ApiResponse(code = 403, message = "Client is not authorized to make this request."),
            @ApiResponse(code = 409, message = "The request was valid but NiFi was not in the appropriate state to process it. Retrying the same request later may be successful.") })
    public Response searchFlowByGroup(
            @QueryParam("q") @DefaultValue(StringUtils.EMPTY) String value,
            @ApiParam(value = "The group id", required = true)
            @PathParam("appId") @DefaultValue(StringUtils.EMPTY)
            final String appId
    ) throws InterruptedException {

        // query the controller
        final SearchResultsDTO results = serviceFacade.searchController(value, appId);

        // create the entity
        final SearchResultsEntity entity = new SearchResultsEntity();
        entity.setSearchResultsDTO(results);

        // generate the response
        return noCache(Response.ok(entity)).build();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/app/check_name")
    @ApiOperation(value = "Get the favorites of current user", //
            response = String.class)
    @ApiResponses(value = { //
            @ApiResponse(code = 400, message = CODE_MESSAGE_400), //
            @ApiResponse(code = 401, message = CODE_MESSAGE_401), //
            @ApiResponse(code = 403, message = CODE_MESSAGE_403), //
            @ApiResponse(code = 409, message = CODE_MESSAGE_409) //
    })
    public Response checkAppName(
            @QueryParam("name")String name,
            @QueryParam("appId") @DefaultValue(StringUtils.EMPTY) String appId
    ) {
        final boolean isAppNameValid = checkAppNameValid(name,appId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name",name);
        resultMap.put("isValid", isAppNameValid);
        return noCache(Response.ok(resultMap)).build();
    }

    /**
     * 检查新的APP名称是否合法。重名校验
     * @param appName
     * @return
     */
    protected boolean checkAppNameValid(final String appName, final String appId) {
        if (StringUtils.isBlank(appName)) {
            return false;
        }

        return !flowController.getRootGroup().getProcessGroups().stream() //
                .filter(g -> StringUtils.isBlank(appId) || !g.getIdentifier().equals(appId)) // exclude
                .filter(p -> p.getName().equals(appName))// existed
                .findFirst() //
                .isPresent();//

    }
}
