package com.github.io.remering.starter.api.file

import com.github.io.remering.starter.USER_UPLOAD_FILE_DIRECTORY
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.handler.StaticHandler

fun Router.mountGetFile() {
  get("/user_upload/*")

    .handler(StaticHandler.create(USER_UPLOAD_FILE_DIRECTORY))
}
