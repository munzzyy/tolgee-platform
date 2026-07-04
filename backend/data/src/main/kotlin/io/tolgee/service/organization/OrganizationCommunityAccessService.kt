package io.tolgee.service.organization

import io.tolgee.constants.Message
import io.tolgee.exceptions.PermissionException
import io.tolgee.security.authentication.AuthenticationFacade
import io.tolgee.service.project.ProjectService
import org.springframework.stereotype.Service

@Service
class OrganizationCommunityAccessService(
  private val organizationRoleService: OrganizationRoleService,
  private val projectService: ProjectService,
  private val authenticationFacade: AuthenticationFacade,
) {
  fun checkUserCanViewAtLeastCommunity(organizationId: Long) {
    if (canUserViewAtLeastCommunity(authenticationFacade.authenticatedUser.id, organizationId)) {
      return
    }
    throw PermissionException(Message.USER_CANNOT_VIEW_THIS_ORGANIZATION)
  }

  /**
   * True for members, direct-project-permission holders, admins/supporters AND community users
   * (any authenticated user when the org has a publicly visible project). Gating full
   * `PrivateOrganizationModel` serialization on this leaks `activeCloudSubscription`/`quickStart`
   * to non-members — community callers must go through `toCommunityModel`.
   */
  fun canUserViewAtLeastCommunity(
    userId: Long,
    organizationId: Long,
  ): Boolean {
    return getAccessTier(userId, organizationId) != OrganizationAccessTier.NONE
  }

  fun getAccessTier(
    userId: Long,
    organizationId: Long,
  ): OrganizationAccessTier {
    if (organizationRoleService.canUserView(userId, organizationId)) {
      return OrganizationAccessTier.FULL
    }
    if (projectService.hasPublicProjects(organizationId)) {
      return OrganizationAccessTier.COMMUNITY
    }
    return OrganizationAccessTier.NONE
  }
}

enum class OrganizationAccessTier {
  FULL,
  COMMUNITY,
  NONE,
}
