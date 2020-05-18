package com.github.io.remering.starter.api

import com.github.io.remering.starter.api.account.mountAccount
import com.github.io.remering.starter.api.course.mountCourse
import com.github.io.remering.starter.api.file.mountGetFile
import com.github.io.remering.starter.api.profile.mountProfile
import com.github.io.remering.starter.api.question.mountQuestion
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.handler.StaticHandler

fun Router.mountApi(vertx: Vertx) {
  val userRouter = Router.router(vertx)
  mountSubRouter("/plarform/user", userRouter)
  with(userRouter) {
    mountAccount()
    mountProfile()
//    mountUploadFile()
  }

  val teacherRouter = Router.router(vertx)
  mountSubRouter("/plarform/teacher", teacherRouter)
  teacherRouter.mountCourse()

  mountGetFile()

  val questionRouter = Router.router(vertx)
  mountSubRouter("/plarform/question", questionRouter)
  questionRouter.mountQuestion()

  val staticRouter = Router.router(vertx)
  mountSubRouter("/static", staticRouter)

  staticRouter.route().handler(StaticHandler.create("static"))

}

