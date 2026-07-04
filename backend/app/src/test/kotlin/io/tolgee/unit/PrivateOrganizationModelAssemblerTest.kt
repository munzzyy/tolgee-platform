package io.tolgee.unit

import io.tolgee.constants.Feature
import io.tolgee.dtos.queryResults.organization.PrivateOrganizationView
import io.tolgee.hateoas.organization.OrganizationModel
import io.tolgee.hateoas.organization.OrganizationModelAssembler
import io.tolgee.hateoas.organization.PrivateOrganizationModelAssembler
import io.tolgee.hateoas.quickStart.QuickStartModelAssembler
import io.tolgee.publicBilling.CloudSubscriptionModelProvider
import io.tolgee.publicBilling.PublicCloudSubscriptionModel
import io.tolgee.testing.assert
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PrivateOrganizationModelAssemblerTest {
  private val organizationModelAssembler = Mockito.mock(OrganizationModelAssembler::class.java)
  private val quickStartModelAssembler = Mockito.mock(QuickStartModelAssembler::class.java)
  private val cloudSubscriptionModelProvider = Mockito.mock(CloudSubscriptionModelProvider::class.java)

  private val underTest =
    PrivateOrganizationModelAssembler(
      organizationModelAssembler,
      quickStartModelAssembler,
      cloudSubscriptionModelProvider,
    )

  private val view = Mockito.mock(PrivateOrganizationView::class.java)
  private val organizationModel = Mockito.mock(OrganizationModel::class.java)

  private fun setupView() {
    val organizationView =
      Mockito.mock(io.tolgee.dtos.queryResults.organization.OrganizationView::class.java)
    whenever(organizationView.id).thenReturn(42L)
    whenever(view.organization).thenReturn(organizationView)
    whenever(view.quickStart).thenReturn(null)
    whenever(organizationModelAssembler.toModel(any())).thenReturn(organizationModel)
  }

  @Test
  fun `community model intersects features with the community allow-list`() {
    setupView()
    val model =
      underTest.toCommunityModel(
        view,
        arrayOf(Feature.GLOSSARY, Feature.PREMIUM_SUPPORT, Feature.ACCOUNT_MANAGER, Feature.BRANCHING),
      )

    model.enabledFeatures
      .toSet()
      .assert
      .isEqualTo(setOf(Feature.GLOSSARY, Feature.BRANCHING))
  }

  @Test
  fun `community model does not advertise allow-listed features the org has not enabled`() {
    setupView()
    val model = underTest.toCommunityModel(view, arrayOf(Feature.PREMIUM_SUPPORT))

    model.enabledFeatures
      .toList()
      .assert
      .isEmpty()
  }

  @Test
  fun `community model withholds quick start and cloud subscription and marks itself`() {
    setupView()
    val model = underTest.toCommunityModel(view, arrayOf(Feature.GLOSSARY))

    model.communityOnly.assert.isTrue()
    model.quickStart.assert.isNull()
    model.activeCloudSubscription.assert.isNull()
    verify(cloudSubscriptionModelProvider, never()).provide(any())
  }

  @Test
  fun `full model keeps features and cloud subscription and is not community-only`() {
    setupView()
    val subscription = Mockito.mock(PublicCloudSubscriptionModel::class.java)
    whenever(cloudSubscriptionModelProvider.provide(42L)).thenReturn(subscription)

    val model = underTest.toModel(view, arrayOf(Feature.PREMIUM_SUPPORT, Feature.GLOSSARY))

    model.communityOnly.assert.isFalse()
    model.enabledFeatures
      .toSet()
      .assert
      .isEqualTo(setOf(Feature.PREMIUM_SUPPORT, Feature.GLOSSARY))
    model.activeCloudSubscription.assert.isEqualTo(subscription)
  }
}
