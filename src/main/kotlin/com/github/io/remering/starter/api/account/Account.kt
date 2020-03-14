package com.github.io.remering.starter.api.account

import io.vertx.reactivex.ext.web.Router

fun Router.mountAccount() {
  mountRegister()
  mountSendVerificationCode()
  mountLogin()
  mountLogout()
  mountChangePassword()
  mountFindPassword()
}
