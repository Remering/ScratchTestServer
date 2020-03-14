package com.github.io.remering.starter.api.account

import com.github.io.remering.starter.*
import com.github.io.remering.starter.table.Accounts
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull

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
  post("/changePassword")
    .handler(jwtAuthHandler)
    .handler { context ->
      val user = context.user()
      val email: String = user.principal()["email"]
      val requestBody = context.bodyAsJson?.mapTo(ChangePasswordRequestBody::class.java)
      if (requestBody == null) {
        context.response().end(
          Json.encode(
            ChangePasswordResponseBody(
              ERROR,
              "参数错误"
            )
          )
        )
        return@handler
      }
      val (oldPassword, newPassword, newPasswordConfirm, veriCode) = requestBody

      if (veriCode != FAKE_EMAIL_VERIFICATION_CODE) {
        context.response().end(
          Json.encode(
            ChangePasswordResponseBody(
              ERROR,
              "验证码不正确"
            )
          )
        )
        return@handler
      }
      if (!arrayOf(oldPassword, newPassword, newPasswordConfirm).map { it.length }.all { it == 64 }) {
        context.response().end(
          Json.encode(
            ChangePasswordResponseBody(
              ERROR,
              "密码格式不正确"
            )
          )
        )
        return@handler
      }
      if (newPassword != newPasswordConfirm) {
        context.response().end(
          Json.encode(
            ChangePasswordResponseBody(
              ERROR,
              "新密码和确认密码不一致"
            )
          )
        )
        return@handler
      }
      val account = database.sequenceOf(Accounts).singleOrNull { it.email eq email }
      if (account == null) {
        context.response().end(
          Json.encode(
            ChangePasswordResponseBody(
              ERROR,
              "用户不存在"
            )
          )
        )
        return@handler
      }
      if (account.password != oldPassword) {
        context.response().end(
          Json.encode(
            ChangePasswordResponseBody(
              ERROR,
              "原密码不正确"
            )
          )
        )
        return@handler
      }

      if (oldPassword != newPassword) {
        account.password = newPassword
      }
      worker.rxExecuteBlocking<Int> {
        it.complete(account.flushChanges())
      }
        .subscribe {
          if (it != 1) {
            context.response().end(
              Json.encode(
                ChangePasswordResponseBody(
                  WARNING,
                  "密码修改失败"
                )
              )
            )
            return@subscribe
          }
          context.response().end(
            Json.encode(
              ChangePasswordResponseBody(
                SUCCESS,
                "密码修改成功"
              )
            )
          )

        }
    }

}
