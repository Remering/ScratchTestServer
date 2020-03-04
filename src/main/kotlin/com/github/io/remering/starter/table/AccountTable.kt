package com.github.io.remering.starter.table

import com.github.io.remering.starter.STUDENT
import me.liuwj.ktorm.dsl.QueryRowSet
import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.*
import java.util.*

data class Account (
  val uuid: UUID,
  val username: String,
  val password: String,
  val email: String,
  val role: Int
)


object Accounts: BaseTable<Account>("accounts") {
  val uuid by text("uuid").primaryKey().transform(
    fromUnderlyingValue = UUID::fromString,
    toUnderlyingValue = UUID::toString
  )
  val username by text("username")
  val password by text("password")
  val email by text("email")
  val role by int("role")
  override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Account(
    uuid = row[uuid] ?: UUID.randomUUID(),
    username = row[username] ?: "",
    password = row[password] ?: "",
    email = row[email] ?: "",
    role = row[role] ?: STUDENT
  )
}



