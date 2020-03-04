package com.github.io.remering.starter

import com.github.io.remering.starter.api.user.mountUser
import com.github.io.remering.starter.table.Accounts
import com.github.io.remering.starter.table.Avatars
import io.vertx.core.Promise
import io.vertx.core.http.HttpMethod
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.mail.LoginOption
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.StartTLSOptions
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.auth.jwt.JWTAuth
import io.vertx.reactivex.ext.mail.MailClient
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.handler.BodyHandler
import io.vertx.reactivex.ext.web.handler.CorsHandler
import io.vertx.reactivex.ext.web.handler.ResponseContentTypeHandler
import me.liuwj.ktorm.dsl.deleteAll
import java.util.*


fun Router.mountCorsHandler() {
  val corsHandler = CorsHandler.create("")
  corsHandler.allowedHeaders(setOf("*"))
//  corsHandler.allowCredentials(true)
  corsHandler.allowedMethods(EnumSet.allOf(HttpMethod::class.java))
  route().handler(corsHandler)
}

fun Router.mountBodyHandler() {
  route().handler(BodyHandler.create(true).setUploadsDirectory("upload"))
}

fun Router.mountResponseContentTypeHandler() {
  route().handler(ResponseContentTypeHandler.create())
}

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    val server = vertx.createHttpServer()
    val router = Router.router(vertx)
    router.mountCorsHandler()
    router.mountBodyHandler()
    router.mountResponseContentTypeHandler()
    router.mountUser(vertx)
    router.route().failureHandler {
      it.failure().printStackTrace()
      it.response().end(
        json {
          obj(
            "code" to ERROR,
            "message" to "服务器内部错误"
          )
        }.toString()
      )
    }
    initAuthProvider()
    initMailClient()
    server.requestHandler(router::handle).listen(6666)
    startPromise.complete()
  }

  @JvmSynthetic
  internal fun deleteAll() {
    database.deleteAll(Accounts)
    database.deleteAll(Avatars)
  }

  private fun initAuthProvider() {
    val config = JWTAuthOptions().apply {
      pubSecKeys = listOf(
        PubSecKeyOptions().apply {
          algorithm = "HS256"
          publicKey = "Scratch"
          isSymmetric = true
        }
      )
    }
    jwtAuthProvider = JWTAuth.create(vertx, config)
  }

  private fun initMailClient() {
    val config = MailConfig().apply {
      hostname = "smtp.qq.com"
      port = 465
      login = LoginOption.REQUIRED
      isSsl = true
      isKeepAlive = true
      starttls = StartTLSOptions.REQUIRED
      username = "1015488424@qq.com"
      password = ""
    }
    mailClient = MailClient.create(vertx, config)
  }
}
