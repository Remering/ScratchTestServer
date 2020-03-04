package com.github.io.remering.starter.api.user.account

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.FAKE_EMAIL_VERIFICATION_CODE
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.database
import com.github.io.remering.starter.table.Accounts
import io.vertx.core.json.Json
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.entity.count
import me.liuwj.ktorm.entity.sequenceOf
import java.util.*

data class RegisterBody(
  val username: String,
  val password: String,
  val email: String,
  val veriCode: String,
  val role: Int
) {
  constructor() : this(
    "",
    "",
    "",
    "",
    0
  )
}

class RegisterResponse(
  val code: Int,
  val message: String
)

fun Router.mountRegister() {

  post("/register").handler { context ->
    val body = context.bodyAsJson?.mapTo(RegisterBody::class.java)
    if (body == null) {
      context.response().end(
        Json.encode(
          RegisterResponse(
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
          RegisterResponse(
            ERROR,
            "验证码不正确"
          )
        ))
      return@handler
    }
    if (password.length != 64) {
      context.response().end(
        Json.encode(
          RegisterResponse(
            ERROR,
            "密码格式不正确"
          )
        )
      )
    }
    try {
      val emailCount = database.sequenceOf(Accounts)
        .count { it.email eq email }
      if (emailCount == 1) {
        context.response().end(
          Json.encode(
            RegisterResponse(
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
          RegisterResponse(
            SUCCESS,
            "注册成功"
          )
        ))
    } catch (e: Exception) {
      context.fail(e)
    }
  }
}
