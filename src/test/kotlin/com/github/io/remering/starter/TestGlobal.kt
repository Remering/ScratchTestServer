package com.github.io.remering.starter

import io.vertx.kotlin.ext.web.client.webClientOptionsOf
import io.vertx.rxjava.core.Vertx
import io.vertx.rxjava.ext.web.client.WebClient

val vertx = Vertx.vertx()
val client = WebClient.create(
  vertx, webClientOptionsOf(
    connectTimeout = 1000,
    keepAlive = true,
    keepAliveTimeout = 1000,
    maxChunkSize = 52428800,
    pipelining = true
  )
)
val verticle = MainVerticle()

const val BASE_URL = "http://localhost:8888"
const val STUDENT_ACCOUNT = "1015488424@qq.com"
const val TEACHER_ACCOUNT = "879969355@qq.com"
const val STUDENT_USERNAME = "remering"
const val TEACHER_USERNAME = "Euphoria"
const val STUDENT_PASSWORD = "123456"
const val TEACHER_PASSWORD = "leishennb"
const val STUDENT_PASSWORD_ENCODED = "e150a1ec81e8e93e1eae2c3a77e66ec6dbd6a3b460f89c1d08aecf422ee401a0"
const val TEACHER_PASSWORD_ENCODED = "571b95d32df050a78d092fe9fc47b01e46336fac3b1c0c68f25ae530e4447472"
const val STUDENT_ROLE = STUDENT
const val TEACHER_ROLE = TEACHER
const val FAKE_AVATAR_URL = "http://www.bilibili.com"
