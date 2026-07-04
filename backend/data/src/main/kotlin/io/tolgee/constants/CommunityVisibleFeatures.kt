package io.tolgee.constants

import java.util.EnumSet

/**
 * Features disclosed to community (non-member) viewers of a public project's organization.
 * Adding a commercial-tier / plan-indicator feature (PREMIUM_SUPPORT, ACCOUNT_MANAGER,
 * DEDICATED_SLACK_CHANNEL, …) here discloses the organization's purchased plan to non-members.
 */
object CommunityVisibleFeatures {
  val features: Set<Feature> =
    EnumSet.of(
      Feature.GLOSSARY,
      Feature.BRANCHING,
      Feature.QA_CHECKS,
      Feature.TRANSLATION_LABELS,
    )
}
