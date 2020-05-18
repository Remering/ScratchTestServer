package com.github.io.remering.starter.api.course

import io.vertx.reactivex.ext.web.Router

fun Router.mountCourse() {
  mountCreateCourse()
  mountUpdateCourse()
  mountGetCourses()
}
