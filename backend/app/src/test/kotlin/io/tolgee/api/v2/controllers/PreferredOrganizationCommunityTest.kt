package io.tolgee.api.v2.controllers

import io.tolgee.development.testDataBuilder.data.PublicProjectsControllerTestData
import io.tolgee.fixtures.andAssertThatJson
import io.tolgee.fixtures.andIsOk
import io.tolgee.model.UserAccount
import io.tolgee.testing.AuthorizedControllerTest
import io.tolgee.testing.assertions.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PreferredOrganizationCommunityTest : AuthorizedControllerTest() {
  lateinit var testData: PublicProjectsControllerTestData

  @BeforeEach
  fun setup() {
    testData = PublicProjectsControllerTestData()
    testDataService.saveTestData(testData.root)
  }

  @AfterEach
  fun clean() {
    testDataService.cleanTestData(testData.root)
  }

  @Test
  fun `community user gets the reduced organization model`() {
    setPreferred(testData.nonMember, testData.otherOrg.id)
    userAccount = testData.nonMember
    performAuthGet("/v2/preferred-organization").andIsOk.andAssertThatJson {
      node("name").isEqualTo("Vibrant translators")
      node("communityOnly").isEqualTo(true)
      node("currentUserRole").isEqualTo(null)
      node("basePermissions").isNotNull
      node("quickStart").isEqualTo(null)
      node("activeCloudSubscription").isEqualTo(null)
    }
  }

  @Test
  fun `member gets the full organization model`() {
    setPreferred(testData.otherOrgMember, testData.otherOrg.id)
    userAccount = testData.otherOrgMember
    performAuthGet("/v2/preferred-organization").andIsOk.andAssertThatJson {
      node("name").isEqualTo("Vibrant translators")
      node("communityOnly").isEqualTo(false)
      node("currentUserRole").isEqualTo("MEMBER")
    }
  }

  @Test
  fun `direct project permission user gets the full organization model`() {
    setPreferred(testData.directPermissionUser, testData.otherOrg.id)
    userAccount = testData.directPermissionUser
    performAuthGet("/v2/preferred-organization").andIsOk.andAssertThatJson {
      node("name").isEqualTo("Vibrant translators")
      node("communityOnly").isEqualTo(false)
      node("currentUserRole").isEqualTo(null)
    }
  }

  @Test
  fun `stale preference falls back to a member organization without writing`() {
    setPreferred(testData.user, testData.otherOrg.id)
    unpublishOtherOrgProject()

    userAccount = testData.user
    val ownOrg = testData.userAccountBuilder.defaultOrganizationBuilder.self
    performAuthGet("/v2/public/initial-data").andIsOk.andAssertThatJson {
      node("preferredOrganization.id").isEqualTo(ownOrg.id)
      node("preferredOrganization.communityOnly").isEqualTo(false)
    }
    assertStoredPreference(testData.user.id, testData.otherOrg.id)
  }

  @Test
  fun `stale preference with no other viewable organization serializes null without writing`() {
    setPreferred(testData.nonMember, testData.otherOrg.id)
    unpublishOtherOrgProject()
    executeInNewTransaction {
      entityManager
        .createNativeQuery("update organization set deleted_at = now() where address_part = 'non-member'")
        .executeUpdate()
    }

    userAccount = testData.nonMember
    performAuthGet("/v2/public/initial-data").andIsOk.andAssertThatJson {
      node("preferredOrganization").isEqualTo(null)
    }
    assertStoredPreference(testData.nonMember.id, testData.otherOrg.id)
  }

  private fun setPreferred(
    user: UserAccount,
    organizationId: Long,
  ) {
    executeInNewTransaction {
      userPreferencesService.setPreferredOrganization(
        organizationService.get(organizationId),
        userAccountService.get(user.id),
      )
    }
  }

  private fun unpublishOtherOrgProject() {
    executeInNewTransaction {
      entityManager
        .createNativeQuery("update project set is_public = false where id = :id")
        .setParameter("id", testData.otherOrgPublicProject.id)
        .executeUpdate()
    }
  }

  private fun assertStoredPreference(
    userId: Long,
    organizationId: Long,
  ) {
    executeInNewTransaction {
      assertThat(userPreferencesService.find(userId)!!.preferredOrganization!!.id).isEqualTo(organizationId)
    }
  }
}
