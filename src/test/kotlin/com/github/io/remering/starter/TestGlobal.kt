package com.github.io.remering.starter

import io.vertx.rxjava.core.Vertx
import io.vertx.rxjava.ext.web.client.WebClient

val vertx = Vertx.vertx()
val client = WebClient.create(vertx)
val verticle = MainVerticle()

const val BASE_URL = "http://localhost:6666"
const val ACCOUNT = "1015488424@qq.com"
const val USERNAME = "remering"
const val FAKE_USERNAME = "baka"
const val PASSWORD = "123456"
const val FAKE_PASSWORD = "12345"
const val PASSWORD_ENCODED = "e150a1ec81e8e93e1eae2c3a77e66ec6dbd6a3b460f89c1d08aecf422ee401a0"
const val FAKE_PASSWORD_ENCODED = "f33ae3bc9a22cd7564990a794789954409977013966fb1a8f43c35776b833a95"
const val ROLE = STUDENT
const val FAKE_AVATAR_URL = "http://www.bilibili.com"
