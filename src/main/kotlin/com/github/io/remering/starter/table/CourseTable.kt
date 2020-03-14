package com.github.io.remering.starter.table

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.varchar
import java.util.*

interface CourseEntity : Entity<CourseEntity> {
  companion object: Entity.Factory<CourseEntity>()
  var uuid: UUID
  var name: String
  var introduction: String
  var picture: String
  var teacher: UUID
  var video: String
  var file: String
}

object Courses: Table<CourseEntity>("courses") {
  val uuid by varchar("uuid")
    .transform(UUID::fromString, UUID::toString)
    .primaryKey()
    .bindTo { it.uuid }
  val name by varchar("name").bindTo { it.name }
  val introduction by varchar("introduction").bindTo { it.introduction }
  val picture by varchar("picture").bindTo { it.picture }
  val teacher by varchar("teacher")
    .transform(UUID::fromString, UUID::toString)
    .primaryKey()
    .bindTo { it.teacher }
  val video by varchar("video").bindTo { it.video }
  val file by varchar("file").bindTo { it.file }
}
