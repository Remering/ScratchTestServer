package com.github.io.remering.starter.api.course

import com.github.io.remering.starter.table.CourseEntity

data class Course (
  val uuid: String = "",
  var name: String = "",
  var introduction: String = "",
  var picture: String = "",
  var teacher: String = "",
  var video: String = "",
  var file: String = ""
) {
  constructor(entity: CourseEntity): this(
    entity.uuid.toString(),
    entity.name,
    entity.introduction,
    entity.picture,
    entity.teacher.toString(),
    entity.video,
    entity.file
  )
}
