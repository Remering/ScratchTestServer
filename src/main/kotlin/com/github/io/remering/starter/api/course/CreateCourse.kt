package com.github.io.remering.starter.api.course

import com.github.io.remering.starter.*
import com.github.io.remering.starter.api.pojo.Course
import com.github.io.remering.starter.table.CourseEntity
import com.github.io.remering.starter.table.Courses
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.reactivex.core.file.FileSystem
import io.vertx.reactivex.ext.web.FileUpload
import io.vertx.reactivex.ext.web.Router
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.sequenceOf
import java.io.File
import java.util.*


class CreateCourseResponseBody (
  val code: Int,
  val message: String,
  val course: Course? = null
)

fun FileUpload.getUrl(uploadedName: String): String {
  var filePath = uploadedName
  if (File.separatorChar != '/') {
    filePath = filePath.replace(File.separatorChar, '/')
  }
  return filePath
}

fun FileUpload.renameWithExtension(fs: FileSystem): String {
  val extension = fileName().substringAfter(".")
  val renamedName = "${uploadedFileName()}.${extension}"
  fs.moveBlocking(uploadedFileName(), renamedName)
  return renamedName
}

fun Router.mountCreateCourse() {
  post("/createCourse")
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
      val uploads = context.fileUploads()
      val name = context.request().getFormAttribute("name")
      val introduction = context.request().getFormAttribute("introduction")
      val pictureSha256 = context.request().getFormAttribute("pictureSha256")
      val videoSha256 = context.request().getFormAttribute("videoSha256")
      val fileSha256 = context.request().getFormAttribute("fileSha256")
      var fileUpload: FileUpload? = null
      var pictureUpload: FileUpload? = null
      var videoUpload: FileUpload? = null

      uploads.forEach {
        when(it.name()) {
          "file" -> fileUpload = it
          "picture" -> pictureUpload = it
          "video" -> videoUpload = it
        }
      }


//      context.response().end("Helloworld")
      if (arrayOf(name, introduction, fileUpload, pictureUpload, videoUpload).any { it == null}) {
        context.response().end(Json.encode(
          CreateCourseResponseBody(
            ERROR, "参数错误"
          )
        ))
        return@handler
      }




      val fs = context.vertx().fileSystem()
      val fileUrl = fileUpload!!.getUrl(fileUpload!!.renameWithExtension(fs))
      val pictureUrl = pictureUpload!!.getUrl(pictureUpload!!.renameWithExtension(fs))
      val videoUrl = videoUpload!!.getUrl(videoUpload!!.renameWithExtension(fs))

      println("fileUUID = $fileUrl")
      println("pictureUrl = $pictureUrl")
      println("videoUrl = $videoUrl")
      val course
        = CourseEntity {
        this.name = name
        this.introduction = introduction
        file = fileUrl
        picture = pictureUrl
        video = videoUrl
        teacher = principal["uuid"]
        uuid = UUID.randomUUID().toString()
      }
      context.response().end(Json.encode(
        CreateCourseResponseBody(
          SUCCESS, "创建成功",
            Course(course, context.request().host())
        )
      ))
      println("insert into database")

      database.sequenceOf(Courses).add(course)

  }
}
