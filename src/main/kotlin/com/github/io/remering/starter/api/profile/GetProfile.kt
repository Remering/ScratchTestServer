package com.github.io.remering.starter.api.profile

import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.api.pojo.GetProfileResponseBody
import com.github.io.remering.starter.api.pojo.Profile
import com.github.io.remering.starter.database
import com.github.io.remering.starter.jwtAuthHandler
import com.github.io.remering.starter.table.Avatars
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull




fun Router.mountGetProfile() {
  get("/getProfile")
    .handler(jwtAuthHandler)
    .handler { context ->
      try {
        val user = context.user()
        val principal = user.principal()
        val uuid: String = principal["uuid"]
        val avatarUrl = database.sequenceOf(Avatars).singleOrNull {
          it.uuid eq uuid
        }?.avatarUrl

        context.response().end(
          Json.encode(
            GetProfileResponseBody(
              code = SUCCESS,
              message = "操作成功",
              profile = Profile(
                username = principal["username"],
                uuid = uuid,
                avatarUrl = "http://${context.request().host()}/$avatarUrl",
                role = principal["role"],
                email = principal["email"]
              )
            )
          )
        )
      } catch (t: Throwable) {
        context.fail(t)
      }
    }
}
