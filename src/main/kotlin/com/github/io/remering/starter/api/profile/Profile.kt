package com.github.io.remering.starter.api.profile


import io.vertx.reactivex.ext.web.Router

fun Router.mountProfile() {
  mountGetProfile()
  mountUpdateProfile()
}
