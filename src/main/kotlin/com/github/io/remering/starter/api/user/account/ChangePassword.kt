package com.github.io.remering.starter.api.user.account

import com.github.io.remering.starter.*
import com.github.io.remering.starter.table.Accounts
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.single

data class ChangePasswordRequestBody @JvmOverloads constructor(
  val oldPassword: String = "",
  val newPassword: String = "",
  val newPasswordConfirm: String = "",
  val veriCode: String = ""
)

class ChangePasswordResponseBody(
  val code: Int,
  val message: String
)

fun Router.mountChangePassword() {
  post("/changePassword").handler {context ->
    val token = context.request().getHeader("Authorization")
    if (token == null) {
      context.response().end(Json.encode(
        ChangePasswordResponseBody(
          ERROR,
          "用户未登录"
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
      val email: String = user.principal()["email"]
      val requestBody = context.bodyAsJson?.mapTo(ChangePasswordRequestBody::class.java)
      if (requestBody == null) {
        context.response().end(Json.encode(
          ChangePasswordResponseBody(
            ERROR,
            "参数错误"
          )
        ))
        return@subscribe
      }
      val (oldPassword, newPassword, newPasswordConfirm, veriCode) = requestBody

      if (veriCode != FAKE_EMAIL_VERIFICATION_CODE) {
        context.response().end(Json.encode(
          ChangePasswordResponseBody(
            ERROR,
            "验证码不正确"
          )
        ))
        return@subscribe
      }
      if (!arrayOf(oldPassword, newPassword, newPasswordConfirm).map { it.length }.all { it == 64 }){
        context.response().end(Json.encode(
          ChangePasswordResponseBody(
            ERROR,
            "密码格式不正确"
          )
        ))
        return@subscribe
      }
      if (newPassword != newPasswordConfirm) {
        context.response().end(Json.encode(
          ChangePasswordResponseBody(
            ERROR,
            "新密码和确认密码不一致"
          )
        ))
        return@subscribe
      }
      if (database.sequenceOf(Accounts).single { it.email eq email }.password != oldPassword) {
        context.response().end(Json.encode(
          ChangePasswordResponseBody(
            ERROR,
            "原密码不正确"
          )
        ))
        return@subscribe
      }

      if (oldPassword != newPassword) {
        database.update(Accounts) {
          it.password to newPassword
          where { it.email eq email }
        }
      }

      context.response().end(Json.encode(
        ChangePasswordResponseBody(
          SUCCESS,
          "密码修改成功"
        )
      ))
    }

  }
}
