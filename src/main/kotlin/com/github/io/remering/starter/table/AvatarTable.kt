package com.github.io.remering.starter.table

import me.liuwj.ktorm.dsl.QueryRowSet
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.text
import me.liuwj.ktorm.schema.uuid
import java.util.*

data class Avatar(
  val uuid: UUID,
  var avatarUrl: String
)

object Avatars: BaseTable<Avatar>("avatars") {
  val uuid by uuid("uuid")
  val avatarUrl by text("avatar_url")
  override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = Avatar(
    uuid = row[uuid] ?: UUID.randomUUID(),
    avatarUrl = row[avatarUrl] ?: ""
  )
}
