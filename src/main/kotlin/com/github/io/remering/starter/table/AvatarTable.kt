package com.github.io.remering.starter.table

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.text

interface AvatarEntity : Entity<AvatarEntity> {
  companion object : Entity.Factory<AvatarEntity>()

  var uuid: String
  var avatarUrl: String?

}


object Avatars : Table<AvatarEntity>("avatars") {
  val uuid by text("uuid").primaryKey().bindTo { it.uuid }
  val avatarUrl by text("avatar_url").bindTo { it.avatarUrl }

}
