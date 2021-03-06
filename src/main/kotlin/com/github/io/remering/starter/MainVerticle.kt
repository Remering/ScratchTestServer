package com.github.io.remering.starter

import com.github.io.remering.starter.api.mountApi
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
import io.vertx.kotlin.ext.auth.jwt.jwtOptionsOf
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.http.HttpServer
import io.vertx.reactivex.ext.auth.jwt.JWTAuth
import io.vertx.reactivex.ext.mail.MailClient
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.handler.BodyHandler
import io.vertx.reactivex.ext.web.handler.CorsHandler
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler
import io.vertx.reactivex.ext.web.handler.ResponseContentTypeHandler
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.deleteAll
import org.sqlite.JDBC
import java.nio.channels.ClosedChannelException
import java.util.*


fun Router.mountCorsHandler() {
  route().handler(
    CorsHandler.create("http://localhost:8080")
      .allowedMethods(EnumSet.allOf(HttpMethod::class.java))
      .allowCredentials(true)
      .allowedHeaders(
        setOf(
          "Access-Control-Allow-Origin",
          "Access-Control-Allow-Credentials",
          "X-PINGARUNER",
          "X-Request-With",
          "Origin",
          "Content-Type",
          "Accept",
          "Authorization "
        )
      )
  )
}

fun Router.mountBodyHandler() {
  route().handler(
    BodyHandler.create(USER_UPLOAD_FILE_DIRECTORY)
      .setMergeFormAttributes(true)
      .setPreallocateBodyBuffer(true)
  )
}

fun Router.mountResponseContentTypeHandler() {
  route().handler(ResponseContentTypeHandler.create())
    .handler { context ->
      context.addHeadersEndHandler {
        if (context.response().headers()["content-type"] == null) {
          context.response().putHeader("content-type", "application/json; charset=utf-8")
        }
      }
      context.next()
    }
}

class MainVerticle : AbstractVerticle() {

  lateinit var server: HttpServer

  override fun init(vertx: io.vertx.core.Vertx?, context: io.vertx.core.Context?) {
    super.init(vertx, context)
    initDatabase()
    initAuthProvider()
    initAuthHandler()
    initMailClient()
    initWorker()

  }

  private fun initWorker() {
    worker = vertx.createSharedWorkerExecutor("io")
  }

  private fun initDatabase() {
    database = Database.connect("jdbc:sqlite:database.db", JDBC::class.qualifiedName)
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
      jwtOptions = jwtOptionsOf(
        expiresInMinutes = 5
      )
    }
    jwtAuthProvider = JWTAuth.create(vertx, config)
  }

  private fun initAuthHandler() {
    jwtAuthHandler = JWTAuthHandler.create(jwtAuthProvider)
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


  override fun start(startPromise: Promise<Void>) {
    server = vertx.createHttpServer()
    val router = Router.router(vertx)
    router.mountCorsHandler()
    router.mountBodyHandler()
    router.mountResponseContentTypeHandler()
    router.mountApi(vertx)
    router.get("/").handler {
      it.response().end("Hello, world")
    }

    router.route().failureHandler {
      if (it.failure() is ClosedChannelException) {
        it.request().connection().close()
        return@failureHandler
      }

      it.failure().printStackTrace()



      when (it.statusCode()) {
        500 -> {
          it.response().end(
            json {
              obj(
                "code" to ERROR,
                "message" to "服务器内部错误"
              )
            }.toString()
          )
        }
        401 -> {
          it.response().end(
            json {
              obj(
                "code" to ERROR,
                "message" to "用户未登录"
              ).toString()
            }
          )
        }
        400 -> {
          it.response().end(
            json {
              obj(
                "code" to ERROR,
                "message" to "用户登录认证错误"
              )
            }.toString()
          )
        }
      }
    }
    server.requestHandler(router::handle).listen(8888)
    startPromise.complete()
  }

  override fun stop() {
    mailClient.close()
    server.close()
  }

  @JvmSynthetic
  internal fun deleteAll() {
    database.deleteAll(Accounts)
    database.deleteAll(Avatars)
  }

}

fun main() {
  io.vertx.core.Vertx.vertx().deployVerticle(MainVerticle())
}
