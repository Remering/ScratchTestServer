package com.github.io.remering.starter.api

import com.github.io.remering.starter.api.account.*
import com.github.io.remering.starter.api.file.mountGetFile
import com.github.io.remering.starter.api.file.mountUploadFile
import com.github.io.remering.starter.api.profile.mountGetProfile
import com.github.io.remering.starter.api.profile.mountProfile
import com.github.io.remering.starter.api.profile.mountUpdateProfile
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router

fun Router.mountApi(vertx: Vertx) {
  val router = Router.router(vertx)
  mountSubRouter("/plarform/user", router)
  with(router) {
    mountAccount()
    mountProfile()
    mountUploadFile()
  }
  mountGetFile()

}

