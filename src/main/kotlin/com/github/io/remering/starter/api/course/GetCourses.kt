package com.github.io.remering.starter.api.course

import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.api.pojo.UserWithAvatar
import com.github.io.remering.starter.database
import com.github.io.remering.starter.table.Accounts
import com.github.io.remering.starter.table.Avatars
import com.github.io.remering.starter.table.CourseEntity
import com.github.io.remering.starter.table.Courses
import io.vertx.core.json.Json
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.entity.fold
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.single
import me.liuwj.ktorm.entity.singleOrNull



data class TeacherCourse (
  val uuid: String = "",
  var name: String = "",
  var introduction: String = "",
  var picture: String = "", // url
  var teacher: UserWithAvatar? = null,
  var video: String = "", // uuid
  var file: String = "" // url
) {
  constructor(entity: CourseEntity, teacher: UserWithAvatar, host: String): this(
    entity.uuid,
    entity.name,
    entity.introduction,
    "http://$host/${entity.picture}",
    teacher,
    "http://$host/${entity.video}",
    "http://$host/${entity.file}"
  )
}

class GetCoursesResponseBody (
  val code: Int,
  val message: String,
  var courses: Map<String, TeacherCourse>? = null
)

fun Router.mountGetCourses() {
  get("/getCourses").handler { context ->
    database.sequenceOf(Courses).fold(mutableMapOf<String, TeacherCourse>()) { map, course ->
      val teacherUUID = course.teacher
      val teacherEntity = database.sequenceOf(Accounts).single {
        it.uuid eq teacherUUID
      }
      val teacherAvatar = database.sequenceOf(Avatars).singleOrNull {
        it.uuid eq teacherUUID
      }?.avatarUrl
      val teacher = UserWithAvatar(
        teacherUUID,
        teacherEntity.username,
        teacherAvatar,
        context.request().host()
      ).also {
        map[course.uuid] = TeacherCourse(course, it, context.request().host())
      }
      map
    }.let { map ->
      val response = GetCoursesResponseBody(
        SUCCESS, "操作成功"
      )
      println(context.request().host())
      if (map.isNotEmpty()) {
        response.courses = map
      }
      context.response().end(Json.encode(response))
    }
  }
}
