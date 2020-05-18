package com.github.io.remering.starter.api.question

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.database
import com.github.io.remering.starter.jwtAuthHandler
import com.github.io.remering.starter.table.Courses
import com.github.io.remering.starter.table.Questions
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.single
import me.liuwj.ktorm.entity.singleOrNull

class DeleteQuestionRequestBody(
  val uuid: String = ""
)

class DeleteQuestionResponseBody(
  val code: Int,
  val message: String
)

fun Router.mountDeleteQuestion() {
  post("/delete")
    .handler(jwtAuthHandler)
    .handler { context ->
      val principal = context.user().principal()
      val requestBody = try {
        context.bodyAsJson?.mapTo(DeleteQuestionRequestBody::class.java)
      } catch (e: Exception) {
        null
      }
      if (requestBody == null) {
        context.response().end(
          Json.encode(
            DeleteQuestionResponseBody(
              ERROR, "参数错误"
            )
          )
        )
        return@handler
      }

      val questionEntity = database.sequenceOf(Questions).singleOrNull {
        it.uuid eq requestBody.uuid
      }

      if (questionEntity == null) {
        context.response().end(
          Json.encode(
            DeleteQuestionResponseBody(
              ERROR, "问题不存在"
            )
          )
        )
        return@handler
      }

      val courseEntity = database.sequenceOf(Courses).single { it.uuid eq questionEntity.course }

      val deleterUUID: String = principal["uuid"]

      if (!arrayOf(courseEntity.teacher, questionEntity.issuer).contains(deleterUUID)) {
        context.response().end(
          Json.encode(
            DeleteQuestionResponseBody(
              ERROR, "权限不足"
            )
          )
        )
        return@handler
      }

      questionEntity.delete()

      context.response().end(
        Json.encode(
          DeleteQuestionResponseBody(
            SUCCESS, "删除成功"
          )
        )
      )
    }
}
