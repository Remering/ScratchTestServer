package com.github.io.remering.starter.api.pojo

import com.github.io.remering.starter.api.course.mountCreateCourse
import com.github.io.remering.starter.api.course.mountGetCourses
import com.github.io.remering.starter.api.course.mountUpdateCourse
import com.github.io.remering.starter.table.CourseEntity
import io.vertx.reactivex.ext.web.Router

data class Course (
  val uuid: String = "",
  var name: String = "",
  var introduction: String = "",
  var picture: String = "", // url
  var teacher: String = "",
  var video: String = "", // uuid
  var file: String = "" // url
) {
  constructor(entity: CourseEntity, host: String): this(
    entity.uuid,
    entity.name,
    entity.introduction,
    "http://$host/${entity.picture}",
    entity.teacher,
    "http://$host/${entity.video}",
    "http://$host/${entity.file}"
  )
}


