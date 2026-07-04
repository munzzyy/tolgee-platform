package io.tolgee.security.authorization

/**
 * Grants read-only access to the annotated organization endpoint for authenticated non-members
 * when the organization has at least one publicly visible project. Evaluated only after the
 * admin/supporter bypass, so admin access keeps its audit trail.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
annotation class AllowsCommunityAccess
