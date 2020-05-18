package com.github.io.remering.starter.api.question

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.api.pojo.Question
import com.github.io.remering.starter.api.pojo.UserWithAvatar
import com.github.io.remering.starter.database
import com.github.io.remering.starter.jwtAuthHandler
import com.github.io.remering.starter.table.Avatars
import com.github.io.remering.starter.table.QuestionEntity
import com.github.io.remering.starter.table.Questions
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull
import java.util.*

data class AddQuestionRequestBody(
  val course: String = "",
  val text: String = "",
  val issueAt: Long = 0L
)

class AddQuestionResponseBody(
  val code: Int,
  val message: String,
  val question: Question? = null
)


fun Router.mountAddQuestion() {
  post("/add")
    .handler(jwtAuthHandler)
    .handler { context ->
      val requestBody = try {
        context.bodyAsJson?.mapTo(AddQuestionRequestBody::class.java)
      } catch (e: Exception) {
        null
      }

      if (requestBody == null) {
        context.response().end(
          Json.encode(
            AddQuestionResponseBody(
              ERROR,
              "参数错误"
            )
          )
        )
        return@handler
      }

      val principal = context.user().principal()

      val issuerUUID: String = principal["uuid"]

      val entity = QuestionEntity {
        uuid = UUID.randomUUID().toString()
        course = requestBody.course
        issuer = issuerUUID
        issueAt = requestBody.issueAt
        text = requestBody.text
      }

      val avatar = database.sequenceOf(Avatars).singleOrNull {
        it.uuid eq issuerUUID
      }
      val issuerUsername: String = principal["username"]
      val issuer = UserWithAvatar(
        issuerUUID,
        issuerUsername,
        avatar?.avatarUrl,
        context.request().host()
      )
      val question = Question(entity, issuer)

      database.sequenceOf(Questions).add(entity)

      context.response().end(
        Json.encode(
          AddQuestionResponseBody(
            SUCCESS,
            "操作成功",
            question
          )
        )
      )
    }
}
