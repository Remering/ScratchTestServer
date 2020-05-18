package com.github.io.remering.starter.api.pojo

class Profile(
  val username: String,
  val uuid: String,
  val avatarUrl: String? = null,
  val role: Int,
  val email: String
)

class GetProfileResponseBody(
  val code: Int,
  val message: String,
  val profile: Profile? = null
)

class UpdateProfileResponseBody(
  val code: Int,
  val message: String,
  val profile: Profile? = null
)
