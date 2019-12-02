package org.apache.nifi.web.init;

import org.apache.nifi.authorization.*;
import org.apache.nifi.authorization.resource.Authorizable;
import org.apache.nifi.authorization.resource.EnforcePolicyPermissionsThroughBaseResource;
import org.apache.nifi.authorization.user.NiFiUserUtils;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.web.NiFiServiceFacade;
import org.apache.nifi.web.ResourceNotFoundException;
import org.apache.nifi.web.api.dto.*;
import org.apache.nifi.web.api.entity.AccessPolicyEntity;
import org.apache.nifi.web.api.entity.ComponentReferenceEntity;
import org.apache.nifi.web.api.entity.TenantEntity;
import org.apache.nifi.web.dao.AccessPolicyDAO;
import org.apache.nifi.web.dao.UserDAO;
import org.apache.nifi.web.dao.UserGroupDAO;
import org.apache.nifi.web.revision.RevisionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 初始化权限组
 * @author liuxun
 */
public class OrchsymInitAdminPolicy {
    private static final Logger logger = LoggerFactory.getLogger(OrchsymInitAdminPolicy.class);

    /**
     * 固定初始化的admin组的identity标识
     */
    private static final String ADMIN_GROUP_IDENTITY = "USER_ADMIN_GROUP";
    /**
     * 固定初始化的admin组的ID标识
     */
    private static final String ADMIN_GROUP_ID = "USER_ADMIN_GROUP_ID";

    private Authorizer authorizer;
    private FlowController flowController;
    private NiFiServiceFacade serviceFacade;
    private UserDAO userDAO;
    private UserGroupDAO userGroupDAO;
    private AccessPolicyDAO accessPolicyDAO;
    private NiFiProperties properties;
    private DtoFactory dtoFactory;
    private EntityFactory entityFactory;
    private RevisionManager revisionManager;
    private AuthorizableLookup authorizableLookup;

    private ExecutorService executorService = null;

    public void initAdminPolicy() {
        // 如果没有配置证书HTTPS 是不能初始的
        if (!AuthorizerCapabilityDetection.isManagedAuthorizer(authorizer)) {
            return;
        }
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.execute(() -> executeInitAdminOperations(100, 1000));
    }

    public void destroy() {
        if (executorService != null || !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * ===================== private methods  ===============================
     */

    private void executeInitAdminOperations(int times, long millis) {
        int count = 0;
        while ((!flowController.isFlowSynchronized() || (properties.isNode() && !flowController.isConnected())) && count < times) {
            try {
                Thread.sleep(millis);
                count++;
            } catch (InterruptedException e) {
                //
            }
        }

        final String adminIdentity = NiFiUserUtils.getAdminIdentity();
        if (Objects.isNull(adminIdentity)) {
            logger.error("please  config a admin user first");
            return;
        }

        final String adminGroupId = existAdminGroup();
        if (Objects.isNull(adminGroupId)) {
            // 创建admin group
            UserGroupDTO userGroupDTO = new UserGroupDTO();
            Set<TenantEntity> users = new HashSet<>();
            TenantEntity tenantEntity = new TenantEntity();
            tenantEntity.setId(getAdminUserId());
            users.add(tenantEntity);
            userGroupDTO.setUsers(users);
            userGroupDTO.setId(ADMIN_GROUP_ID);
            userGroupDTO.setIdentity(ADMIN_GROUP_IDENTITY);
            userGroupDAO.createUserGroup(userGroupDTO);
        }

        try {
            addAdminGroupPolicy();
        }catch (Exception e){
            logger.error("init failure: e={}, message={} ", e, e.getMessage() );
        }

    }

    /**
     * 判断是否存在adminGroup 如果存在，返回adminGroup的id，不存在则返回null
     */

    private String existAdminGroup() {
        Group userGroup = null;
        try {
            userGroup = userGroupDAO.getUserGroup(ADMIN_GROUP_ID);
        }catch (ResourceNotFoundException e){
            //
        }
        return userGroup == null ? null : userGroup.getIdentifier();
    }

    private String getAdminUserId() {
        final List<String> adminIds = userDAO.getUsers().stream().filter(e -> e.getIdentity().equals(NiFiUserUtils.getAdminIdentity())).map(User::getIdentifier).collect(Collectors.toList());
        return adminIds.isEmpty() ? null : adminIds.get(0);
    }

    /**
     * 为admin group 赋予权限
     */
    private void addAdminGroupPolicy() {
        final Set<AccessPolicy> accessPoliciesForUser = userGroupDAO.getAccessPoliciesForUser(getAdminUserId());
        accessPoliciesForUser.forEach(accessPolicy -> {
            final AccessPolicyDTO policyDTO = createAccessPolicyEntity(accessPolicy).getComponent();
            policyDTO.getUserGroups().add(getNewAdminTenantEntity(ADMIN_GROUP_ID, ADMIN_GROUP_IDENTITY));
            final AccessPolicy policy = accessPolicyDAO.updateAccessPolicy(policyDTO);
            logger.info("{}", policy.getGroups().size());
        });

    }

    private TenantEntity getNewAdminTenantEntity(String id, String identity) {
        TenantEntity tenantEntity = new TenantEntity();
        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setId(id);
        tenantDTO.setIdentity(identity);
        tenantDTO.setConfigurable(true);
        tenantEntity.setComponent(tenantDTO);
        RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        PermissionsDTO permissionDTO = new PermissionsDTO();
        permissionDTO.setCanRead(true);
        permissionDTO.setCanWrite(true);
        tenantEntity.setPermissions(permissionDTO);
        tenantEntity.setRevision(revision);
        tenantEntity.setId(id);
        return tenantEntity;
    }

    private AccessPolicyEntity createAccessPolicyEntity(final AccessPolicy accessPolicy) {
        final RevisionDTO revision = dtoFactory.createRevisionDTO(revisionManager.getRevision(accessPolicy.getIdentifier()));
        final PermissionsDTO permissions = new PermissionsDTO();
        permissions.setCanWrite(true);
        permissions.setCanRead(true);
        final ComponentReferenceEntity componentReference = createComponentReferenceEntity(accessPolicy.getResource());
        return entityFactory.createAccessPolicyEntity(
                dtoFactory.createAccessPolicyDto(accessPolicy,
                        accessPolicy.getGroups().stream().map(mapUserGroupIdToTenantEntity()).collect(Collectors.toSet()),
                        accessPolicy.getUsers().stream().map(mapUserIdToTenantEntity()).collect(Collectors.toSet()), componentReference),
                revision, permissions);
    }

    private ComponentReferenceEntity createComponentReferenceEntity(final String resource) {
        ComponentReferenceEntity componentReferenceEntity = null;
        try {
            // get the component authorizable
            Authorizable componentAuthorizable = authorizableLookup.getAuthorizableFromResource(resource);

            // if this represents an authorizable whose policy permissions are enforced through the base resource,
            // get the underlying base authorizable for the component reference
            if (componentAuthorizable instanceof EnforcePolicyPermissionsThroughBaseResource) {
                componentAuthorizable = ((EnforcePolicyPermissionsThroughBaseResource) componentAuthorizable).getBaseAuthorizable();
            }

            final ComponentReferenceDTO componentReference = dtoFactory.createComponentReferenceDto(componentAuthorizable);
            if (componentReference != null) {
                final PermissionsDTO componentReferencePermissions = dtoFactory.createPermissionsDto(componentAuthorizable);
                final RevisionDTO componentReferenceRevision = dtoFactory.createRevisionDTO(revisionManager.getRevision(componentReference.getId()));
                componentReferenceEntity = entityFactory.createComponentReferenceEntity(componentReference, componentReferenceRevision, componentReferencePermissions);
            }
        } catch (final ResourceNotFoundException e) {
            // component not found for the specified resource
        }

        return componentReferenceEntity;
    }

    private Function<String, TenantEntity> mapUserGroupIdToTenantEntity() {
        return userGroupId -> {
            final RevisionDTO userGroupRevision = dtoFactory.createRevisionDTO(revisionManager.getRevision(userGroupId));
            return entityFactory.createTenantEntity(dtoFactory.createTenantDTO(userGroupDAO.getUserGroup(userGroupId)), userGroupRevision,
                    dtoFactory.createPermissionsDto(authorizableLookup.getTenant()));
        };
    }

    private Function<String, TenantEntity> mapUserIdToTenantEntity() {
        return userId -> {
            final RevisionDTO userRevision = dtoFactory.createRevisionDTO(revisionManager.getRevision(userId));
            return entityFactory.createTenantEntity(dtoFactory.createTenantDTO(userDAO.getUser(userId)), userRevision,
                    dtoFactory.createPermissionsDto(authorizableLookup.getTenant()));
        };
    }

    /**
     * ==================== setter and getter methods =======================
     */

    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }

    public void setServiceFacade(NiFiServiceFacade serviceFacade) {
        this.serviceFacade = serviceFacade;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setUserGroupDAO(UserGroupDAO userGroupDAO) {
        this.userGroupDAO = userGroupDAO;
    }

    public void setAccessPolicyDAO(AccessPolicyDAO accessPolicyDAO) {
        this.accessPolicyDAO = accessPolicyDAO;
    }

    public void setProperties(NiFiProperties properties) {
        this.properties = properties;
    }

    public void setDtoFactory(DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public void setRevisionManager(RevisionManager revisionManager) {
        this.revisionManager = revisionManager;
    }

    public void setAuthorizableLookup(AuthorizableLookup authorizableLookup) {
        this.authorizableLookup = authorizableLookup;
    }
}
