package com.github.io.remering.starter.api.user.account

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.jwtAuthProvider
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.ext.web.Router

class LogoutResponseBody (
  val code: Int,
  val message: String
)

fun Router.mountLogout() {
  get("/logout").handler { context ->
    val token = context.request().getHeader("Authorization")
    if (token == null) {
      context.response().end(Json.encode(
        LogoutResponseBody(
          ERROR, "用户未登录"
        )
      ))
      return@handler
    }
    jwtAuthProvider.rxAuthenticate(json {
      obj("jwt" to token)
    }).subscribe { user, error ->
      if (error != null) {
        context.fail(error)
        return@subscribe
      }
      println(user.principal())
      context.response().end(Json.encode(
        LogoutResponseBody(
          SUCCESS,
          "登出成功"
        )
      ))
    }
  }
}
