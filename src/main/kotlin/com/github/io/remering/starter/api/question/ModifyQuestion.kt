package com.github.io.remering.starter.api.question

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.api.pojo.Question
import com.github.io.remering.starter.api.pojo.UserWithAvatar
import com.github.io.remering.starter.database
import com.github.io.remering.starter.jwtAuthHandler
import com.github.io.remering.starter.table.Avatars
import com.github.io.remering.starter.table.Questions
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull

data class ModifyTextRequestBody(
  val uuid: String = "",
  val text: String = "",
  val issueAt: Long = 0L
)

typealias ModifyTextResponseBody = AddQuestionResponseBody

fun Router.mountModifyText() {
  post("/modifyText")
    .handler(jwtAuthHandler)
    .handler { context ->
      val principal = context.user().principal()
      val requestBody = try {
        context.bodyAsJson?.mapTo(ModifyTextRequestBody::class.java)
      } catch (e: Exception) {
        null
      }
      if (requestBody == null) {
        context.response().end(
          Json.encode(
            ModifyTextResponseBody(
              ERROR, "参数错误"
            )
          )
        )
        return@handler
      }
      val modifierUUID: String = principal["uuid"]
      val (questionUUID, text, issueAt) = requestBody

      val questionEntity = database.sequenceOf(Questions).singleOrNull {
        it.uuid eq questionUUID
      }

      if (questionEntity == null) {
        context.response().end(
          Json.encode(
            ModifyTextResponseBody(
              ERROR, "问题不存在"
            )
          )
        )
        return@handler
      }

      if (questionEntity.issuer != modifierUUID) {
        context.response().end(
          Json.encode(
            ModifyTextResponseBody(
              ERROR, "权限不足"
            )
          )
        )
        return@handler
      }

      if (questionEntity.text != text) {
        questionEntity.text = text
        questionEntity.issueAt = issueAt
        questionEntity.flushChanges()
      }


      val avatarEntity = database.sequenceOf(Avatars).singleOrNull {
        it.uuid eq modifierUUID
      }

      val modifiedUsername: String = principal["username"]


      val user = UserWithAvatar(
        modifierUUID,
        modifiedUsername,
        avatarEntity?.avatarUrl,
        context.request().host()
      )

      context.response().end(
        Json.encode(
          ModifyTextResponseBody(
            SUCCESS, "操作成功",
            Question(questionEntity, user)
          )
        )
      )

    }
}
