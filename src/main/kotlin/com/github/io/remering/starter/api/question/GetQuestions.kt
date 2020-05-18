package com.github.io.remering.starter.api.question

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.api.pojo.Question
import com.github.io.remering.starter.api.pojo.UserWithAvatar
import com.github.io.remering.starter.database
import com.github.io.remering.starter.table.Accounts
import com.github.io.remering.starter.table.Avatars
import com.github.io.remering.starter.table.Courses
import com.github.io.remering.starter.table.Questions
import io.vertx.core.json.Json
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.*

data class GetQuestionsRequestBody(
  val course: String = ""
)

class GetQuestionsResponseBody(
  val code: Int,
  val message: String,
  val questions: Map<String, Question>? = null
)

fun Router.mountGetQuestions() {
  post("/get").handler { context ->
    val requestBody = try {
      context.bodyAsJson?.mapTo(GetQuestionsRequestBody::class.java)
    } catch (e: Exception) {
      null
    }

    if (requestBody == null) {
      context.response().end(
        Json.encode(
          GetQuestionsResponseBody(
            ERROR, "参数错误"
          )
        )
      )
      return@handler
    }

    database.from(Questions)
      .innerJoin(Courses, Courses.uuid eq Questions.course)
      .leftJoin(Avatars, Avatars.uuid eq Questions.issuer)
      .innerJoin(Accounts, Accounts.uuid eq Questions.issuer)
      .select(
        Questions.uuid,
        Questions.course,
        Questions.issuer,
        Avatars.avatarUrl,
        Accounts.username,
        Questions.issueAt,
        Questions.answerAt,
        Questions.text,
        Questions.answer
      )
      .where { Questions.course eq requestBody.course }
      .asSequence()
//      .filter {
//        arrayOf(it[Questions.uuid], it[Questions.course], it[Questions.issuer], it[Accounts.username], it[Questions.issueAt], it[Questions.text])
//          .all { it != null }
//      }
      .map { row ->
        val questionUUID = row[Questions.uuid]!!
        val courseUUID = row[Questions.course]!!
        val issuerUUID = row[Questions.issuer]!!
        val issuerAvatarUrl = row[Avatars.avatarUrl]
        val issuerUsername = row[Accounts.username]!!
        val issuerAt = row[Questions.issueAt]!!
        val answerAt = row[Questions.answerAt]
        val text = row[Questions.text]!!
        val answer = row[Questions.answer]

        Question(
          questionUUID,
          courseUUID,
          UserWithAvatar(
            issuerUUID,
            issuerUsername,
            issuerAvatarUrl,
            context.request().host()
          ),
          issuerAt,
          text,
          answer,
          answerAt ?: 0L
        )
      }.fold(mutableMapOf<String, Question>()) { map, question ->
        map += question.uuid to question
        map
      }.let { questions ->
        context.response().end(
          Json.encode(
            GetQuestionsResponseBody(
              SUCCESS, "操作成功",
              questions
            )
          )
        )
      }
  }
}
