package io.tolgee.hateoas.organization

import io.swagger.v3.oas.annotations.media.Schema
import io.tolgee.constants.Feature
import io.tolgee.hateoas.quickStart.QuickStartModel
import io.tolgee.publicBilling.PublicCloudSubscriptionModel
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Suppress("unused")
@Relation(collectionRelation = "organizations", itemRelation = "organization")
open class PrivateOrganizationModel(
  organizationModel: OrganizationModel,
  @get:Schema(example = "Features organization has enabled")
  val enabledFeatures: Array<Feature>,
  @get:Schema(example = "Quick start data for current user")
  val quickStart: QuickStartModel?,
  @get:Schema(example = "Current active subscription info")
  val activeCloudSubscription: PublicCloudSubscriptionModel?,
  @get:Schema(
    description =
      "True when the current user only has community access to this organization " +
        "(via its public projects) — member-only fields are withheld",
  )
  val communityOnly: Boolean = false,
) : RepresentationModel<PrivateOrganizationModel>(),
  IOrganizationModel by organizationModel
