package com.github.io.remering.starter

import io.vertx.reactivex.core.WorkerExecutor
import io.vertx.reactivex.ext.auth.jwt.JWTAuth
import io.vertx.reactivex.ext.mail.MailClient
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler
import me.liuwj.ktorm.database.Database
import java.math.BigInteger
import java.security.MessageDigest


const val STUDENT = 0
const val TEACHER = 1

const val SUCCESS = 0
const val INFO = 1
const val WARNING = 2
const val ERROR = 3

const val AUTHORIZATION = "Authorization"
const val USER_UPLOAD_FILE_DIRECTORY = "user_upload"

lateinit var database: Database
lateinit var jwtAuthProvider: JWTAuth
lateinit var jwtAuthHandler: JWTAuthHandler
lateinit var mailClient: MailClient
lateinit var worker: WorkerExecutor

const val FAKE_EMAIL_VERIFICATION_CODE = "666666"

fun calculateSha256(buffer: ByteArray): String {
  val md = MessageDigest.getInstance("SHA-256")
  return String.format("%14x", BigInteger(1, md.digest(buffer)))
}
