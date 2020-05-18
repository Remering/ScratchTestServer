package com.github.io.remering.starter.api.pojo

class UserWithAvatar(
  val uuid: String = "",
  val username: String = "",
  avatarUrl: String? = null,
  host: String
) {
  val avatarUrl: String = "http://$host/${avatarUrl ?: "static/img/avatar_default.jpg"}"
}
