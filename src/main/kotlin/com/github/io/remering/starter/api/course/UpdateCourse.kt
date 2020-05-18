package com.github.io.remering.starter.api.course

import com.github.io.remering.starter.*
import com.github.io.remering.starter.api.pojo.Course
import com.github.io.remering.starter.table.CourseEntity
import com.github.io.remering.starter.table.Courses
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.ext.web.FileUpload
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.sequenceOf
import me.liuwj.ktorm.entity.singleOrNull
import java.util.*



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
          )
        )
        return@handler
      }


      val courseUUID = context.request().getParam("uuid")
      if (courseUUID == null) {
        context.response().end(Json.encode(
          UpdateCourseResponseBody(
            ERROR, "参数错误"
          )
        ))
        return@handler
      }
      val name: String? = context.request().getParam("name")
      val introduction: String? = context.request().getParam("introduction")


      val courseEntity = database.sequenceOf(Courses).singleOrNull {
        it.uuid eq courseUUID
      }

      if (courseEntity == null) {
        context.response().end(
          Json.encode(
            UpdateCourseResponseBody(
              ERROR, "未找到课程"
            )
          )
        )
        return@handler
      }


      var fileUpload: FileUpload? = null
      var pictureUpload: FileUpload? = null
      var videoUpload: FileUpload? = null

      context.fileUploads().forEach {
        when(it.name()) {
          "file" -> fileUpload = it
          "picture" -> pictureUpload = it
          "video" -> videoUpload = it
        }
      }

      val fs = context.vertx().fileSystem()
      val fileUrl = fileUpload?.getUrl(fileUpload!!.renameWithExtension(fs))
      val pictureUrl = pictureUpload?.getUrl(pictureUpload!!.renameWithExtension(fs))
      val videoUrl = videoUpload?.getUrl(videoUpload!!.renameWithExtension(fs))

      courseEntity.name = name?: courseEntity.name
      courseEntity.introduction = introduction?: courseEntity.introduction
      courseEntity.file = fileUrl?: courseEntity.file
      courseEntity.picture = pictureUrl?: courseEntity.picture
      courseEntity.video = videoUrl?: courseEntity.video

      courseEntity.flushChanges()

      context.response().end(
        Json.encode(
          UpdateCourseResponseBody(
            SUCCESS, "更新成功",
            Course(courseEntity, context.request().host())
          )
        )
      )
    }
}
