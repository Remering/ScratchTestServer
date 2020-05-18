package com.github.io.remering.starter.table

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.text

interface AccountEntity: Entity<AccountEntity> {
  companion object : Entity.Factory<AccountEntity>()

  val uuid: String
  val username: String
  var password: String
  val email: String
  val role: Int

}

object Accounts: Table<AccountEntity>("accounts") {
  val uuid by text("uuid").primaryKey().bindTo { it.uuid }
  val username by text("username").bindTo { it.username }
  val password by text("password").bindTo { it.password }
  val email by text("email").bindTo { it.email }
  val role by int("role").bindTo { it.role }

}



