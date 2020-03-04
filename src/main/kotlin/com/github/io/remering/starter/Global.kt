package com.github.io.remering.starter

import io.vertx.reactivex.ext.auth.jwt.JWTAuth
import io.vertx.reactivex.ext.mail.MailClient
import me.liuwj.ktorm.database.Database
import org.sqlite.JDBC


const val STUDENT = 0
const val TEACHER = 1

const val SUCCESS = 0
const val INFO = 1
const val WARNING = 2
const val ERROR = 3

val database = Database.connect("jdbc:sqlite:database.db", JDBC::class.qualifiedName)


lateinit var jwtAuthProvider: JWTAuth
lateinit var mailClient: MailClient

const val FAKE_EMAIL_VERIFICATION_CODE = "666666"
