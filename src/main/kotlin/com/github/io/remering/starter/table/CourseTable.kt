package com.github.io.remering.starter.table

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.varchar

interface CourseEntity : Entity<CourseEntity> {
  companion object: Entity.Factory<CourseEntity>()
  var uuid: String
  var name: String
  var introduction: String
  var picture: String
  var teacher: String
  var video: String
  var file: String
}

object Courses: Table<CourseEntity>("courses") {
  val uuid by varchar("uuid").primaryKey().bindTo { it.uuid }
  val name by varchar("name").bindTo { it.name }
  val introduction by varchar("introduction").bindTo { it.introduction }
  val picture by varchar("picture").bindTo { it.picture }
  val teacher by varchar("teacher").bindTo { it.teacher }
  val video by varchar("video").bindTo { it.video }
  val file by varchar("file").bindTo { it.file }
}
