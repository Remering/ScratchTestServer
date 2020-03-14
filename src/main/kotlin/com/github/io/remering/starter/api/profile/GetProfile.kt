package com.github.io.remering.starter.api.profile

import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.database
import com.github.io.remering.starter.jwtAuthHandler
import com.github.io.remering.starter.table.Avatars
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull
import java.util.*


class Profile(
  val username: String,
  val uuid: String,
  val avatarUrl: String? = null,
  val role: Int
)

class GetProfileResponseBody(
  val code: Int,
  val message: String,
  val profile: Profile? = null
)

fun Router.mountGetProfile() {
  get("/getProfile")
    .handler(jwtAuthHandler)
    .handler { context ->
      val user = context.user()
      val principal = user.principal()
      val uuid: String = principal["uuid"]
      val avatarUrl = database.sequenceOf(Avatars).singleOrNull {
        it.uuid eq UUID.fromString(uuid)
      }?.avatarUrl

      context.response().end(
        Json.encode(
          GetProfileResponseBody(
            code = SUCCESS,
            message = "操作成功",
            profile = Profile(
              username = principal["username"],
              uuid = uuid,
              avatarUrl = avatarUrl,
              role = principal["role"]
            )
          )
        )
      )
    }
}
