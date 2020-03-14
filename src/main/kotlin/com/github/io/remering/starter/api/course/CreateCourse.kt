package com.github.io.remering.starter.api.course

import com.github.io.remering.starter.*
import com.github.io.remering.starter.table.CourseEntity
import com.github.io.remering.starter.table.Courses
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.sequenceOf
import java.util.*


data class CreateCourseRequestBody(
  val name: String = "",
  val introduction: String = "",
  val picture: String = "",
  val video: String = "",
  val file: String = ""
)
class CreateCourseResponseBody (
  val code: Int,
  val message: String,
  val course: Course? = null
)



fun Router.mountCreateCourse() {
  post("/updateCourse")
    .handler(jwtAuthHandler)
    .handler { context ->
      val principal = context.user().principal()
      val role: Int = principal["role"]
      if (role != TEACHER) {
        context.response().end(Json.encode(
          CreateCourseResponseBody(
            ERROR, "权限不足"
          )
        ))
        return@handler
      }
      val requestBody = try {
        context.bodyAsJson?.mapTo(CreateCourseRequestBody::class.java)
      } catch (e: Throwable) {
        context.fail(e)
        return@handler
      }
      if (requestBody == null) {
        context.response().end(Json.encode(
          CreateCourseResponseBody(
            ERROR, "参数错误"
          )
        ))
        return@handler
      }

      val teacherUUID: String = principal["uuid"]
      val courseUUID = UUID.randomUUID()
      val (name, introduction, picture, video, file) = requestBody
      val entity = CourseEntity {
        uuid = courseUUID
        this.name = name
        this.introduction = introduction
        this.picture = picture
        this.teacher = UUID.fromString(teacherUUID)
        this.video = video
        this.file = file
      }
      worker.rxExecuteBlocking<Int> {
        it.complete(database.sequenceOf(Courses).add(entity))
      }.doOnError(context::fail)
      .subscribe {
        if (it != 1) {
          context.response().end(Json.encode(
            CreateCourseResponseBody(ERROR, "创建失败")
          ))
          return@subscribe
        }
        context.response().end(Json.encode(
          CreateCourseResponseBody(SUCCESS, "创建成功",
            Course(courseUUID.toString(), name, introduction, picture, teacherUUID, video, file)
            )
        ))
      }
  }
}
