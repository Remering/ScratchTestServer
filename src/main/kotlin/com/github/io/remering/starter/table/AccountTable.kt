package com.github.io.remering.starter.table

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.*
import java.util.*

interface AccountEntity: Entity<AccountEntity> {
  companion object: Entity.Factory<AccountEntity>()
  val uuid: UUID
  val username: String
  var password: String
  val email: String
  val role: Int

}

object Accounts: Table<AccountEntity>("accounts") {
  val uuid by text("uuid").primaryKey().transform(
    fromUnderlyingValue = UUID::fromString,
    toUnderlyingValue = UUID::toString
  ).bindTo { it.uuid }
  val username by text("username").bindTo { it.username }
  val password by text("password").bindTo { it.password }
  val email by text("email").bindTo { it.email }
  val role by int("role").bindTo { it.role }

}



