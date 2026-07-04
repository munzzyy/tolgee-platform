package io.tolgee.development.testDataBuilder.data

import io.tolgee.development.testDataBuilder.builders.ProjectBuilder
import io.tolgee.model.Organization
import io.tolgee.model.Project
import io.tolgee.model.UserAccount
import io.tolgee.model.enums.OrganizationRoleType
import io.tolgee.model.enums.ProjectPermissionType

class PublicProjectsControllerTestData : BaseTestData() {
  val privateProject: Project get() = project

  lateinit var publicProject: Project
  lateinit var otherOrg: Organization
  lateinit var otherOrgPublicProject: Project
  lateinit var nonMember: UserAccount

  lateinit var directPermissionUser: UserAccount

  lateinit var noBaseLanguageProject: Project
  lateinit var softDeletedBaseProject: Project
  lateinit var orgLessProject: Project
  lateinit var deletedPublicProject: Project

  lateinit var otherOrgMember: UserAccount
  lateinit var serverAdmin: UserAccount

  lateinit var noPublicOrg: Organization
  lateinit var noPublicOrgMember: UserAccount

  lateinit var noBaseLangOnlyOrg: Organization
  lateinit var noBaseLangOnlyOrgProject: Project
  lateinit var softDeletedBaseLangOnlyOrg: Organization
  lateinit var softDeletedBaseLangOnlyOrgProject: Project
  lateinit var deletedProjectOnlyOrg: Organization
  lateinit var deletedProjectOnlyOrgProject: Project
  lateinit var softDeletedOrg: Organization
  lateinit var softDeletedOrgPublicProject: Project

  init {
    root.apply {
      nonMember =
        addUserAccount {
          username = "non_member"
          name = "Non Member"
        }.self

      directPermissionUser =
        addUserAccount {
          username = "direct_perm_user"
          name = "Direct Perm User"
        }.self

      addProject(organizationOwner = userAccountBuilder.defaultOrganizationBuilder.self) {
        name = "Public project"
        public = true
      }.build {
        publicProject = self
        addBaseLanguage()
      }

      otherOrgMember =
        addUserAccount {
          username = "other_org_member"
          name = "Other Org Member"
        }.self

      serverAdmin =
        addUserAccount {
          username = "server_admin"
          name = "Server Admin"
          role = UserAccount.Role.ADMIN
        }.self

      otherOrg =
        addOrganization {
          name = "Vibrant translators"
        }.build {
          addRole {
            user = this@PublicProjectsControllerTestData.otherOrgMember
            type = OrganizationRoleType.MEMBER
          }
        }.self

      addProject(organizationOwner = otherOrg) {
        name = "Other org public project"
        public = true
      }.build {
        otherOrgPublicProject = self
        addBaseLanguage()
        addPermission {
          user = this@PublicProjectsControllerTestData.directPermissionUser
          type = ProjectPermissionType.TRANSLATE
        }
      }

      noPublicOrgMember =
        addUserAccount {
          username = "no_public_org_member"
          name = "No Public Org Member"
        }.self

      noPublicOrg =
        addOrganization {
          name = "Members only outfit"
        }.build {
          addRole {
            user = this@PublicProjectsControllerTestData.noPublicOrgMember
            type = OrganizationRoleType.MEMBER
          }
        }.self

      addProject(organizationOwner = noPublicOrg) {
        name = "Members only private project"
      }.build {
        addBaseLanguage()
      }

      noBaseLangOnlyOrg =
        addOrganization {
          name = "No base lang only org"
        }.self

      addProject(organizationOwner = noBaseLangOnlyOrg) {
        name = "No base lang only org project"
        public = true
      }.build {
        noBaseLangOnlyOrgProject = self
        addBaseLanguage()
      }

      softDeletedBaseLangOnlyOrg =
        addOrganization {
          name = "Soft deleted base lang only org"
        }.self

      addProject(organizationOwner = softDeletedBaseLangOnlyOrg) {
        name = "Soft deleted base lang only org project"
        public = true
      }.build {
        softDeletedBaseLangOnlyOrgProject = self
        addBaseLanguage()
      }

      deletedProjectOnlyOrg =
        addOrganization {
          name = "Deleted project only org"
        }.self

      addProject(organizationOwner = deletedProjectOnlyOrg) {
        name = "Deleted project only org project"
        public = true
      }.build {
        deletedProjectOnlyOrgProject = self
        addBaseLanguage()
      }

      softDeletedOrg =
        addOrganization {
          name = "Soft deleted org"
        }.self

      addProject(organizationOwner = softDeletedOrg) {
        name = "Soft deleted org public project"
        public = true
      }.build {
        softDeletedOrgPublicProject = self
        addBaseLanguage()
      }

      addProject(organizationOwner = userAccountBuilder.defaultOrganizationBuilder.self) {
        name = "No base language project"
        public = true
      }.build {
        noBaseLanguageProject = self
        addBaseLanguage()
      }

      addProject(organizationOwner = userAccountBuilder.defaultOrganizationBuilder.self) {
        name = "Soft-deleted base project"
        public = true
      }.build {
        softDeletedBaseProject = self
        addBaseLanguage()
      }

      addProject(organizationOwner = userAccountBuilder.defaultOrganizationBuilder.self) {
        name = "Org-less project"
        public = true
      }.build {
        orgLessProject = self
        addBaseLanguage()
      }

      addProject(organizationOwner = userAccountBuilder.defaultOrganizationBuilder.self) {
        name = "Deleted public project"
        public = true
      }.build {
        deletedPublicProject = self
        addBaseLanguage()
      }
    }
  }

  private fun ProjectBuilder.addBaseLanguage() {
    addLanguage {
      name = "English"
      tag = "en"
      originalName = "English"
      this@addBaseLanguage.self.baseLanguage = this
    }
  }
}
