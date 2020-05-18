package com.github.io.remering.starter.api.pojo

import com.github.io.remering.starter.table.QuestionEntity


data class Question(
  val uuid: String,
  val course: String,
  val issuer: UserWithAvatar? = null,
  val issueAt: Long,
  val text: String,
  val answer: String? = null,
  val answerAt: Long = 0L
) {
  constructor(entity: QuestionEntity, issuer: UserWithAvatar) : this(
    entity.uuid,
    entity.course,
    issuer,
    entity.issueAt,
    entity.text,
    entity.answer,
    entity.answerAt
  )
}
