package com.github.io.remering.starter.api.user.account

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.FAKE_EMAIL_VERIFICATION_CODE
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.database
import com.github.io.remering.starter.table.Accounts
import io.vertx.core.json.Json
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull

data class FindPasswordRequestBody @JvmOverloads constructor(
  val email: String = "",
  val veriCode: String = "",
  val newPassword: String = "",
  val newPasswordConfirm: String = ""
)

class FindPasswordResponseBody (
  val code: Int,
  val message: String
)

fun Router.mountFindPassword() {
  post("/findPassword").handler { context ->
    val requestBody = context.bodyAsJson?.mapTo(FindPasswordRequestBody::class.java)
    if (requestBody == null) {
      context.response().end(Json.encode(
        FindPasswordResponseBody(
          ERROR,
          "参数错误"
        )
      ))
      return@handler
    }
    val (email, veriCode, newPassword, newPasswordConfirm) = requestBody
    if (veriCode != FAKE_EMAIL_VERIFICATION_CODE) {
      context.response().end(Json.encode(
        FindPasswordResponseBody(
          ERROR,
          "验证码不正确"
        )
      ))
      return@handler
    }
    if (newPassword != newPasswordConfirm) {
      context.response().end(Json.encode(
        FindPasswordResponseBody(
          ERROR,
          "密码与确认密码不一致"
        )
      ))
      return@handler
    }
    if (!arrayOf(newPassword, newPasswordConfirm).map { it.length}.all { it == 64 }) {
      context.response().end(Json.encode(
        FindPasswordResponseBody(
          ERROR,
          "密码格式不正确"
        )
      ))
      return@handler
    }
    val account = database.sequenceOf(Accounts).singleOrNull {
      it.email eq email
    }
    if (account == null) {
      context.response().end(Json.encode(
        FindPasswordResponseBody(
          ERROR,
          "用户不存在"
        )
      ))
      return@handler
    }
    if (account.password != newPassword) {
      account.password = newPassword
    }
    context.response().end(Json.encode(
      FindPasswordResponseBody(
        SUCCESS,
        "密码修改成功"
      )
    ))
  }
}
