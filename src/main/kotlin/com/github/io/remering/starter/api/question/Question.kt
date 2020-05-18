package com.github.io.remering.starter.api.question

import io.vertx.reactivex.ext.web.Router

fun Router.mountQuestion() {
  mountAddQuestion()
  mountAnswerQuestion()
  mountDeleteQuestion()
  mountGetQuestions()
  mountModifyText()
}
