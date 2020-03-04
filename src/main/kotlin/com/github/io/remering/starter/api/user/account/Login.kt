package com.github.io.remering.starter.api.user.account

import com.github.io.remering.starter.*
import com.github.io.remering.starter.table.Account
import com.github.io.remering.starter.table.Accounts
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull

class LoginRequestBody @JvmOverloads constructor(
  val account: String = "",
  val password: String = ""
)

class LoginResponseBody(
  val code: Int,
  val message: String,
  val token: String? = null
)

data class UserPrincipal @JvmOverloads constructor(
  val uuid: String = "",
  val username: String = "",
  val email: String = "",
  val role: Int = STUDENT
) {
  constructor(account: Account):
    this(
      uuid = account.uuid.toString(),
      username = account.username,
      email = account.email,
      role = account.role
    )
}


fun Router.mountLogin() {
  post("/login").handler {context ->

    if (context.request().getHeader("Authorization") != null) {
      context.response().end(Json.encode(
        LoginResponseBody(
          ERROR,
          "用户已登录"
        )
      ))
      return@handler
    }

    val requestBody = context.bodyAsJson?.mapTo(LoginRequestBody::class.java)
    if (requestBody == null) {
      context.response().end(Json.encode(
        LoginResponseBody(
          ERROR,
          "参数错误"
        )
      ))
      return@handler
    }
    if (requestBody.password.length != 64) {
      context.response().end(Json.encode(
        LoginResponseBody(
          ERROR,
          "密码格式错误"
        )
      ))
      return@handler
    }
    val account= database.sequenceOf(Accounts).singleOrNull {
      it.email eq requestBody.account
    }

    if (account == null) {
      context.response().end(Json.encode(
        LoginResponseBody(
          ERROR,
          "用户不存在"
        )
      ))
      return@handler
    }

    if (account.password != requestBody.password) {
      context.response().end(Json.encode(
        LoginResponseBody(
          ERROR,
          "账号或密码错误"
        )
      ))
      return@handler
    }

    val token = jwtAuthProvider.generateToken(JsonObject.mapFrom(UserPrincipal(account)))
    context.response().end(
      Json.encode(
        LoginResponseBody(SUCCESS, "登录成功", token)
      )
    )
  }.failureHandler {
    it.failure().printStackTrace()
    it.response().end(Json.encode(
      LoginResponseBody(
        ERROR,
        "服务器内部错误"
      )
    ))
  }
}
