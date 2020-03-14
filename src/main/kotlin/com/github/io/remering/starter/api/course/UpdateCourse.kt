package com.github.io.remering.starter.api.course

import com.github.io.remering.starter.*
import com.github.io.remering.starter.table.CourseEntity
import com.github.io.remering.starter.table.Courses
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull
import java.util.*

data class UpdateCourseRequestBody(
  val uuid: String = "",
  val name: String? = null,
  val introduction: String? = null,
  val picture: String? = null,
  val video: String? = null,
  val file: String? = null
)

typealias UpdateCourseResponseBody = CreateCourseResponseBody

fun Router.mountUpdateCourse() {
  post("/updateCourse")
    .handler(jwtAuthHandler)
    .handler { context ->
      val principal = context.user().principal()
      val role: Int = principal["role"]
      if (role != TEACHER) {
        context.response().end(
          Json.encode(
            UpdateCourseResponseBody(
              ERROR, "权限不足"
            )
          ))
        return@handler
      }
      val requestBody = try {
        context.bodyAsJson?.mapTo(UpdateCourseRequestBody::class.java)
      } catch (e: Throwable) {
        context.fail(e)
        return@handler
      }
      if (requestBody == null) {
        context.response().end(Json.encode(
          UpdateCourseResponseBody(
            ERROR, "参数错误"
          )
        ))
        return@handler
      }
      worker.rxExecuteBlocking<CourseEntity> {
        it.complete(database.sequenceOf(Courses).singleOrNull {
          it.uuid eq UUID.fromString(requestBody.uuid)
        })
      }.doOnError(context::fail)
      .subscribe { course ->
        if (course == null) {
          context.response().end(Json.encode(
            UpdateCourseResponseBody(
              ERROR, "未找到课程"
            )
          ))
          return@subscribe
        }
        course.name = requestBody.name?:course.name
        course.introduction = requestBody.introduction?:course.introduction
        course.picture = requestBody.picture?:course.picture
        course.video = requestBody.video?:course.video
        course.file = requestBody.file?:course.file
        course.flushChanges()
        context.response().end(Json.encode(
          UpdateCourseResponseBody(
            SUCCESS, "更新成功",
            Course(course)
          )
        ))
      }
  }
}
