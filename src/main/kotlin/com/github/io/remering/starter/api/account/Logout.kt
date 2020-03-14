package com.github.io.remering.starter.api.account

import com.github.io.remering.starter.*
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.ext.web.Router

class LogoutResponseBody (
  val code: Int,
  val message: String
)

fun Router.mountLogout() {

  get("/logout")
    .handler(jwtAuthHandler)
    .handler { context ->
    val token = context.request().getHeader(AUTHORIZATION)
    if (token == null) {
      context.response().end(Json.encode(
        LogoutResponseBody(
          ERROR, "用户未登录"
        )
      ))
      return@handler
    }

      context.clearUser()
      context.response().end(Json.encode(
        LogoutResponseBody(
          SUCCESS,
          "登出成功"
        )
      ))
    }
  }

