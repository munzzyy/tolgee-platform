package io.tolgee.component

import io.tolgee.component.enabledFeaturesProvider.EnabledFeaturesProvider
import io.tolgee.hateoas.organization.PrivateOrganizationModel
import io.tolgee.hateoas.organization.PrivateOrganizationModelAssembler
import io.tolgee.security.authentication.AuthenticationFacade
import io.tolgee.service.organization.OrganizationAccessTier
import io.tolgee.service.organization.OrganizationCommunityAccessService
import io.tolgee.service.organization.OrganizationService
import io.tolgee.service.security.UserPreferencesService
import org.springframework.stereotype.Component

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Component
class PreferredOrganizationFacade(
  private val authenticationFacade: AuthenticationFacade,
  private val userPreferencesService: UserPreferencesService,
  private val privateOrganizationModelAssembler: PrivateOrganizationModelAssembler,
  private val enabledFeaturesProvider: EnabledFeaturesProvider,
  private val organizationService: OrganizationService,
  private val organizationCommunityAccessService: OrganizationCommunityAccessService,
) {
  fun getPreferred(): PrivateOrganizationModel? {
    val userId = authenticationFacade.authenticatedUser.id
    val preferences = userPreferencesService.findOrCreate(userId)
    val preferredOrganization = preferences.preferredOrganization ?: return null
    val tier = organizationCommunityAccessService.getAccessTier(userId, preferredOrganization.id)
    if (tier == OrganizationAccessTier.NONE) {
      return getFallback(userId, preferredOrganization.id)
    }
    val view = organizationService.findPrivateView(preferredOrganization.id, userId) ?: return null
    val features = enabledFeaturesProvider.get(view.organization.id)
    if (tier == OrganizationAccessTier.FULL) {
      return privateOrganizationModelAssembler.toModel(view, features)
    }
    return privateOrganizationModelAssembler.toCommunityModel(view, features)
  }

  /**
   * Must stay a pure read — this runs on `GET /v2/public/initial-data`. Do not "heal" the stale
   * preference here via `refreshPreferredOrganization`: it saves and can even create an
   * organization. The next set-preferred-organization write corrects the stored value.
   */
  private fun getFallback(
    userId: Long,
    staleOrganizationId: Long,
  ): PrivateOrganizationModel? {
    val fallback = organizationService.findPreferred(userId, exceptOrganizationId = staleOrganizationId) ?: return null
    val view = organizationService.findPrivateView(fallback.id, userId) ?: return null
    return privateOrganizationModelAssembler.toModel(view, enabledFeaturesProvider.get(view.organization.id))
  }
}
