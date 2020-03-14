package com.github.io.remering.starter.api.account

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.mailClient
import io.vertx.core.json.Json
import io.vertx.ext.mail.*
import io.vertx.reactivex.ext.web.Router

class SendVerificationCodeRequestBody (
  val email: String = ""
)

class SendVerificationCodeResponseBody(
  val code: Int,
  val message: String
) {
  constructor(): this(0, "")
}


val mailRegex = Regex("^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+\$")

fun Router.mountSendVerificationCode() {
  post("/sendVerificationCode").handler {context ->
    val body = context.bodyAsJson?.mapTo(SendVerificationCodeRequestBody::class.java)
    if (body == null) {
      context.response().end(Json.encode(
        SendVerificationCodeResponseBody(
          ERROR,
          "参数错误"
        )
      ))
      return@handler
    }
    val toAddress = body.email
    if (!mailRegex.matches(toAddress)) {
      context.response().end(Json.encode(
        SendVerificationCodeResponseBody(
          ERROR,
          "邮箱格式不正确"
        )
      ))
      return@handler
    }

    val message = MailMessage().apply {
      from = "1015488424@qq.com"
      to = listOf(toAddress)
      text = "Hello, world"
    }

    mailClient.rxSendMail(message)
      .subscribe { result, error ->
        if (error != null) {
          context.fail(error)
          return@subscribe
        }
        context.response().end(
            Json.encode(
              if (result.recipients.firstOrNull() == toAddress)
                SendVerificationCodeResponseBody(
                  SUCCESS,
                  "邮件发送成功"
                )
              else
                SendVerificationCodeResponseBody(
                  ERROR,
                  "邮件发送失败"
                )
            )
        )
      }

  }

}
