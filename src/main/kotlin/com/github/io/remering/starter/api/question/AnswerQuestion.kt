package com.github.io.remering.starter.api.question

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.api.pojo.Question
import com.github.io.remering.starter.api.pojo.UserWithAvatar
import com.github.io.remering.starter.database
import com.github.io.remering.starter.jwtAuthHandler
import com.github.io.remering.starter.table.Accounts
import com.github.io.remering.starter.table.Avatars
import com.github.io.remering.starter.table.Courses
import com.github.io.remering.starter.table.Questions
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.single
import me.liuwj.ktorm.entity.singleOrNull

data class AnswerQuestionRequestBody(
  val uuid: String = "",
  val answer: String = "",
  val answerAt: Long = 0L
)

typealias AnswerQuestionResponseBody = AddQuestionResponseBody

fun Router.mountAnswerQuestion() {
  post("/answer")
    .handler(jwtAuthHandler)
    .handler { context ->

      val principal = context.user().principal()

      val answererUUID: String = principal["uuid"]

      val requestBody = try {
        context.bodyAsJson?.mapTo(AnswerQuestionRequestBody::class.java)
      } catch (e: Exception) {
        e.printStackTrace()
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

      val (questionUUID, answer, answerAt) = requestBody

      val questionEntity = database.sequenceOf(Questions).singleOrNull {
        it.uuid eq questionUUID
      }
      if (questionEntity == null) {
        context.response().end(
          Json.encode(
            AnswerQuestionResponseBody(
              ERROR, "没有此问题"
            )
          )
        )
        return@handler
      }

      val courseEntity = database.sequenceOf(Courses).single {
        it.uuid eq questionEntity.course
      }

      if (answererUUID != courseEntity.teacher) {
        context.response().end(
          Json.encode(
            AnswerQuestionResponseBody(
              ERROR, "权限不足"
            )
          )
        )
        return@handler
      }

      questionEntity.answer = answer
      questionEntity.answerAt = answerAt
      questionEntity.flushChanges()


      val user = database.from(Accounts)
        .leftJoin(Avatars, Avatars.uuid eq Accounts.uuid)
        .select(Accounts.username, Avatars.avatarUrl)
        .where { Accounts.uuid eq questionEntity.issuer }
        .map { row ->
          UserWithAvatar(
            questionEntity.issuer,
            row[Accounts.username]!!,
            row[Avatars.avatarUrl],
            context.request().host()
          )
        }.single()



      context.response().end(
        Json.encode(
          AnswerQuestionResponseBody(
            SUCCESS, "操作成功",
            Question(questionEntity, user)
          )
        )
      )
    }
}
