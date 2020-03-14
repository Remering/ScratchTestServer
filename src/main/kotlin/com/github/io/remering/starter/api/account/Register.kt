package com.github.io.remering.starter.api.account

import com.github.io.remering.starter.*
import com.github.io.remering.starter.table.Accounts
import io.vertx.core.json.Json
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.entity.count
import me.liuwj.ktorm.entity.sequenceOf
import java.util.*

data class RegisterRequestBody(
  val username: String,
  val password: String,
  val email: String,
  val veriCode: String,
  val role: Int
) {
  @Suppress("unused")
  constructor() : this(
    "",
    "",
    "",
    "",
    0
  )
}

class RegisterResponseBody(
  val code: Int,
  val message: String
)

fun Router.mountRegister() {

  post("/register").handler { context ->
    val body = context.bodyAsJson?.mapTo(RegisterRequestBody::class.java)
    if (body == null) {
      context.response().end(
        Json.encode(
          RegisterResponseBody(
            ERROR,
            "参数错误"
          )
        ))
      return@handler
    }
    val (username, password, email, veriCode, role) = body
    if (veriCode != FAKE_EMAIL_VERIFICATION_CODE) {
      context.response().end(
        Json.encode(
          RegisterResponseBody(
            ERROR,
            "验证码不正确"
          )
        ))
      return@handler
    }
    if (password.length != 64) {
      context.response().end(
        Json.encode(
          RegisterResponseBody(
            ERROR,
            "密码格式不正确"
          )
        )
      )
      return@handler
    }
    val emailCount = database.sequenceOf(Accounts).count { it.email eq email }

    if (emailCount > 0) {
      context.response().end(
        Json.encode(
          RegisterResponseBody(
            ERROR,
            "邮箱已被使用"
          )
        ))
      return@handler
    }
    database.insert(Accounts) {
      it.uuid to UUID.randomUUID()
      it.username to username
      it.password to password
      it.email to email
      it.role to role
    }
    context.response().end(
      Json.encode(
        RegisterResponseBody(
          SUCCESS,
          "注册成功"
        )
      ))
  }
}
