package com.github.io.remering.starter.api.user.profile

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.database
import com.github.io.remering.starter.jwtAuthProvider
import com.github.io.remering.starter.table.Accounts
import com.github.io.remering.starter.table.Avatars
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull
import java.util.*

data class UpdateProfileRequestBody @JvmOverloads constructor(
  val avatarUrl: String? = null
)

class UpdateProfileResponseBody (
  val code: Int,
  val message: String,
  val profile: Profile? = null
)

fun Router.mountUpdateProfile() {
  post("/updateProfile").handler { context ->
    val token = context.request().getHeader("Authorization")
    if (token == null) {
      context.response().end(Json.encode(UpdateProfileResponseBody(
        ERROR, "用户未登录"
      )))
      return@handler
    }
    val requestBody = context.bodyAsJson?.mapTo(UpdateProfileRequestBody::class.java)
    if (requestBody == null) {
      context.response().end(Json.encode(UpdateProfileResponseBody(
        ERROR, "参数错误"
      )))
      return@handler
    }
    jwtAuthProvider.rxAuthenticate(json {
      obj("jwt" to token)
    }).subscribe { user, error ->
      if (error != null) {
        context.fail(error)
        return@subscribe
      }
      val (avatarUrl) = requestBody
      val principal = user.principal()

      if (avatarUrl != null && avatarUrl != principal["avatarUrl"]) {
        val avatar = database.sequenceOf(Avatars).singleOrNull {
          it.uuid eq UUID.fromString(principal["uuid"])
        }
        if (avatar == null) {
          database.insert(Avatars) {
            it.uuid to UUID.fromString(principal["uuid"])
            it.avatarUrl to avatarUrl
          }
        } else {
          avatar.avatarUrl = avatarUrl
        }
      }

      context.response().end(Json.encode(
        UpdateProfileResponseBody(
          SUCCESS,
          "修改成功",
          profile = Profile(
            username = principal["username"],
            uuid = principal["uuid"],
            avatarUrl = avatarUrl?:principal["avatarUrl"],
            role = principal["role"]
          )
        )
      ))

    }

  }
}
