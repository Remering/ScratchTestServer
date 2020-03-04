package com.github.io.remering.starter.api.user

import com.github.io.remering.starter.api.user.account.*
import com.github.io.remering.starter.api.user.profile.mountGetProfile
import com.github.io.remering.starter.api.user.profile.mountUpdateProfile
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router

fun Router.mountUser(vertx: Vertx) {
  val router = Router.router(vertx)
  mountSubRouter("/plarform/user", router)
  with(router) {
    mountRegister()
    mountSendVerificationCode()
    mountLogin()
    mountLogout()
    mountChangePassword()
    mountFindPassword()
    mountGetProfile()
    mountUpdateProfile()
  }
}

