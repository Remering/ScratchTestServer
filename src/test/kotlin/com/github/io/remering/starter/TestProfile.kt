package com.github.io.remering.starter

import com.github.io.remering.starter.api.account.LoginRequestBody
import com.github.io.remering.starter.api.account.RegisterRequestBody
import io.vertx.kotlin.core.json.get
import io.vertx.rxjava.ext.web.multipart.MultipartForm
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

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
      STUDENT_USERNAME,
      STUDENT_PASSWORD_ENCODED,
      STUDENT_ACCOUNT,
      FAKE_EMAIL_VERIFICATION_CODE,
      STUDENT_ROLE
    )
    println("测试用户未登录")
    var response = client.getAbs("$BASE_URL/plarform/user/getProfile")
      .rxSend()
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    println(response)
    assertEquals(ERROR, response["code"])
    assertEquals("用户未登录", response["message"])
    assertNull(response["profile"])

    println("测试注册成功")
    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    println(response)
    assertEquals(SUCCESS, response["code"])
    assertEquals("注册成功", response["message"])

    println("注册成功")
    response = client.postAbs("$BASE_URL/plarform/user/login")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        LoginRequestBody(
          STUDENT_ACCOUNT,
          STUDENT_PASSWORD_ENCODED
        )
      )
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    println(response)
    val token: String? = response["token"]
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    assertNotNull(token)

    println("测试正确样例")
    response = client.getAbs("$BASE_URL/plarform/user/getProfile")
      .putHeader("Authorization", "Bearer $token")
      .rxSend()
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    println(response)
    assertEquals(SUCCESS, response["code"])
    assertEquals("操作成功", response["message"])
    val profile = response.getJsonObject("profile")
    assertEquals(STUDENT_USERNAME, profile["username"])
    assertNotNull(profile["uuid"])
    assertEquals(STUDENT_ROLE, profile["role"])
    assertNull(profile["avatarUrl"])
  }

  @Test
  fun testUpdateProfile() {
    val body = RegisterRequestBody(
      "remering",
      STUDENT_PASSWORD_ENCODED,
      "1015488424@qq.com",
      "666666",
      STUDENT_ROLE
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
            STUDENT_ACCOUNT,
            STUDENT_PASSWORD_ENCODED
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

    val formdata = MultipartForm.create()
    formdata.binaryFileUpload("avatar", "avatar.png", FAKE_AVATAR_URL, "img/jpg")
    response =
      client.postAbs("$BASE_URL/plarform/user/updateProfile")
        .putHeader("Content-Type", "application/json")
        .putHeader("Authorization", "Bearer $token")
        .rxSendMultipartForm(formdata)
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("修改成功", response["message"])
    var profile = response.getJsonObject("profile")
    assertNotNull(profile)
    assertEquals(STUDENT_USERNAME, profile["username"])
    val uuid: String? = profile["uuid"]
    assertNotNull(uuid)
    assertNull(profile["avatarUrl"])
    assertEquals(STUDENT_ROLE, profile["role"])

    response =
      client.postAbs("$BASE_URL/plarform/user/updateProfile")
        .putHeader("Content-Type", "application/json")
        .putHeader("Authorization", "Bearer $token")
        .rxSendMultipartForm(formdata)
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("修改成功", response["message"])
    profile = response.getJsonObject(
      "p" +
        "rofile"
    )
    assertNotNull(profile)
    assertEquals(STUDENT_USERNAME, profile["username"])
    assertEquals(uuid, profile["uuid"])
    assertEquals(profile["avatarUrl"], FAKE_AVATAR_URL)
    assertEquals(STUDENT_ROLE, profile["role"])

  }

}
