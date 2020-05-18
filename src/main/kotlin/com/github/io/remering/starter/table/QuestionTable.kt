package com.github.io.remering.starter.table

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.long
import me.liuwj.ktorm.schema.text

interface QuestionEntity : Entity<QuestionEntity> {
  companion object : Entity.Factory<QuestionEntity>()

  var uuid: String
  var course: String
  var issuer: String
  var issueAt: Long
  var answerAt: Long
  var text: String
  var answer: String?
}

object Questions : Table<QuestionEntity>("questions") {
  val uuid by text("uuid").primaryKey().bindTo { it.uuid }
  val course by text("course").bindTo { it.course }
  val issuer by text("issuer").bindTo { it.issuer }
  val issueAt by long("issue_at").bindTo { it.issueAt }
  val answerAt by long("answer_at").bindTo { it.answerAt }
  val text by text("text").bindTo { it.text }
  val answer by text("answer").bindTo { it.answer }
}
