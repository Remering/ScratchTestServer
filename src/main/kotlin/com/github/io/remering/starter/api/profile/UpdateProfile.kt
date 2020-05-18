package com.github.io.remering.starter.api.profile

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.api.course.getUrl
import com.github.io.remering.starter.api.course.renameWithExtension
import com.github.io.remering.starter.api.pojo.Profile
import com.github.io.remering.starter.api.pojo.UpdateProfileResponseBody
import com.github.io.remering.starter.database
import com.github.io.remering.starter.jwtAuthHandler
import com.github.io.remering.starter.table.AvatarEntity
import com.github.io.remering.starter.table.Avatars
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull


fun Router.mountUpdateProfile() {
  post("/updateProfile")
    .handler(jwtAuthHandler)
    .handler { context ->

      val user = context.user()
      val principal = user.principal()
      val avatarUploads = context.fileUploads().singleOrNull()
      if (avatarUploads == null) {
        context.response().end(
          Json.encode(
            UpdateProfileResponseBody(ERROR, "参数错误")
          )
        )
        return@handler
      }

      val avatarUrl = avatarUploads.getUrl(avatarUploads.renameWithExtension(context.vertx().fileSystem()))

      val avatarEntity = database.sequenceOf(Avatars).singleOrNull {
        it.uuid eq principal.getString("uuid")
      }

      if (avatarEntity == null) {
        val newAvatarEntity = AvatarEntity {
          uuid = principal["uuid"]
          this.avatarUrl = avatarUrl
        }
        database.sequenceOf(Avatars).add(newAvatarEntity)
      } else {
        avatarEntity.avatarUrl = avatarUrl
        avatarEntity.flushChanges()
      }

      context.response().end(
        Json.encode(
          UpdateProfileResponseBody(
            SUCCESS,
            "修改成功",
            profile = Profile(
              username = principal["username"],
              uuid = principal["uuid"],
              avatarUrl = "http://${context.request().host()}/$avatarUrl",
              role = principal["role"],
              email = principal["email"]
            )
          )
        )
      )
    }
}
