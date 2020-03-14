package com.github.io.remering.starter

import com.github.io.remering.starter.api.account.LoginRequestBody
import com.github.io.remering.starter.api.account.RegisterRequestBody
import com.github.io.remering.starter.api.profile.UpdateProfileRequestBody
import io.vertx.kotlin.core.json.get
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

class TestProfile {
  companion object {
    @BeforeAll
    @JvmStatic
    fun deployVerticle() {
      vertx.deployVerticle(verticle)
    }

  }

  @AfterEach
  fun clearUpDb() {
    verticle.deleteAll()
  }

  @Test
  fun testGetProfile() {
    val body = RegisterRequestBody(
      "remering",
      PASSWORD_ENCODED,
      "1015488424@qq.com",
      "666666",
      ROLE
    )
    var response = client.getAbs("$BASE_URL/plarform/user/getProfile")
      .rxSend()
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("用户未登录", response["message"])
    assertNull(response["profile"])

     response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("注册成功", response["message"])

    response =
      client.postAbs("$BASE_URL/plarform/user/login")
        .putHeader("Content-Type", "application/json")
        .rxSendJson(
          LoginRequestBody(
            ACCOUNT,
            PASSWORD_ENCODED
          )
        )
        .toBlocking()
        .value()
        .bodyAsJsonObject()

    val token: String? = response["token"]
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    assertNotNull(token)

    response = client.getAbs("$BASE_URL/plarform/user/getProfile")
      .putHeader("Authorization", token)
      .rxSend()
      .toBlocking()
      .value()
      .bodyAsJsonObject()

    assertEquals(SUCCESS, response["code"])
    assertEquals("操作成功", response["message"])
    val profile = response.getJsonObject("profile")
    assertEquals(USERNAME, profile["username"])
    assertNotNull(profile["uuid"])
    assertEquals(ROLE, profile["role"])
    assertNull(profile["avatarUrl"])
  }

  @Test
  fun testUpdateProfile() {
    val body = RegisterRequestBody(
      "remering",
      PASSWORD_ENCODED,
      "1015488424@qq.com",
      "666666",
      ROLE
    )
    var response = client.postAbs("$BASE_URL/plarform/user/updateProfile")
      .rxSend()
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("用户未登录", response["message"])
    assertNull(response["profile"])

    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("注册成功", response["message"])

    response =
      client.postAbs("$BASE_URL/plarform/user/login")
        .putHeader("Content-Type", "application/json")
        .rxSendJson(
          LoginRequestBody(
            ACCOUNT,
            PASSWORD_ENCODED
          )
        )
        .toBlocking()
        .value()
        .bodyAsJsonObject()

    val token: String? = response["token"]
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    assertNotNull(token)

    response =
      client.postAbs("$BASE_URL/plarform/user/updateProfile")
        .putHeader("Content-Type", "application/json")
        .putHeader("Authorization", token)
        .rxSend()
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("参数错误", response["message"])
    assertNull(response["profile"])

    response =
      client.postAbs("$BASE_URL/plarform/user/updateProfile")
        .putHeader("Content-Type", "application/json")
        .putHeader("Authorization", token)
        .rxSendJson(
          UpdateProfileRequestBody(
            null
          )
        )
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("修改成功", response["message"])
    var profile = response.getJsonObject("profile")
    assertNotNull(profile)
    assertEquals(USERNAME, profile["username"])
    val uuid: String? = profile["uuid"]
    assertNotNull(uuid)
    assertNull(profile["avatarUrl"])
    assertEquals(ROLE, profile["role"])

    response =
      client.postAbs("$BASE_URL/plarform/user/updateProfile")
        .putHeader("Content-Type", "application/json")
        .putHeader("Authorization", token)
        .rxSendJson(
          UpdateProfileRequestBody(
            FAKE_AVATAR_URL
          )
        )
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("修改成功", response["message"])
    profile = response.getJsonObject("profile")
    assertNotNull(profile)
    assertEquals(USERNAME, profile["username"])
    assertEquals(uuid, profile["uuid"])
    assertEquals(profile["avatarUrl"], FAKE_AVATAR_URL)
    assertEquals(ROLE, profile["role"])

  }

}
