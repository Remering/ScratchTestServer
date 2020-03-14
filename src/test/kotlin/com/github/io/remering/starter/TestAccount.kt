package com.github.io.remering.starter

import com.github.io.remering.starter.api.account.*
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.get
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*


class TestAccount {
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
  fun testRegister() {
    class RegisterBody(
      var username: String,
      var password: String,
      var email: String,
      var veriCode: String,
      var role: Int
    )

    var response =
    client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSend()
      .toBlocking()
      .value()
      .bodyAsJsonObject()

    assertEquals(ERROR, response["code"])
    assertEquals("参数错误", response["message"])

    val body = RegisterBody("remering", PASSWORD_ENCODED, "1015488424@qq.com", "123456", 0)

    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("验证码不正确", response["message"])
    body.veriCode = "666666"


    body.password = PASSWORD
    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("密码格式不正确", response["message"])

    body.password = PASSWORD_ENCODED
    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("注册成功", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("邮箱已被使用", response["message"])

  }


  @Test
  fun testLogin() {
    var response =
      client.postAbs("$BASE_URL/plarform/user/login")
        .putHeader("Content-Type", "application/json")
        .rxSend()
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("参数错误", response["message"])



    response = client.postAbs("$BASE_URL/plarform/user/login")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        LoginRequestBody(
          ACCOUNT,
          PASSWORD
        )
      )
      .toBlocking()
      .value()
      .bodyAsJsonObject()

    assertEquals(ERROR, response["code"])
    assertEquals("密码格式错误", response["message"])
    assertNull(response["token"])

    val body = RegisterRequestBody(
      USERNAME,
      PASSWORD_ENCODED,
      "1015488424@qq.com",
      "666666",
      0
    )
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
            FAKE_PASSWORD_ENCODED
          )
        )
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("账号或密码错误", response["message"])
    assertNull(response["token"])

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
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    val token: String? = response["token"]
    assertNotNull(token)

    response =
      client.postAbs("$BASE_URL/plarform/user/login")
        .putHeader("Content-Type", "application/json")
        .putHeader("Authorization", "Bearer $token")
        .rxSendJson(
          LoginRequestBody(
            ACCOUNT,
            PASSWORD_ENCODED
          )
        )
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("用户已登录", response["message"])
    assertNull(response["token"])
  }

  @Test
  fun testLogout() {

    var response = client.getAbs("$BASE_URL/plarform/user/logout")
      .putHeader("Content-Type", "application/json")
      .rxSend()
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("用户未登录", response["message"])

    val body = RegisterRequestBody(
      ACCOUNT,
      PASSWORD_ENCODED,
      ACCOUNT,
      FAKE_EMAIL_VERIFICATION_CODE,
      0
    )
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
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    val token: String? = response["token"]
    assertNotNull(token)

    response = client.getAbs("$BASE_URL/plarform/user/logout")
      .putHeader("Authorization", "Bearer $token")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("登出成功", response["message"])
  }

  @Test
  fun testChangePassword() {
   val body = RegisterRequestBody(
     ACCOUNT,
     PASSWORD_ENCODED,
     ACCOUNT,
     FAKE_EMAIL_VERIFICATION_CODE,
     STUDENT
   )
    var response = client.postAbs("$BASE_URL/plarform/user/changePassword")
      .putHeader("Content-Type", "application/json")
      .rxSend().toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("用户未登录", response["message"])


    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("注册成功", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/login")
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
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    val token: String? = response["token"]
    assertNotNull(token)
    response = client.postAbs("$BASE_URL/plarform/user/changePassword")
      .putHeader("Content-Type", "application/json")
      .putHeader("Authorization", "Bearer $token")
      .rxSend().toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("参数错误", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/changePassword")
      .putHeader("Content-Type", "application/json")
      .putHeader("Authorization", "Bearer $token")
      .rxSendJson(
        ChangePasswordRequestBody(
          oldPassword = PASSWORD_ENCODED,
          newPassword = PASSWORD_ENCODED,
          newPasswordConfirm = FAKE_PASSWORD_ENCODED
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("新密码和确认密码不一致", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/changePassword")
      .putHeader("Content-Type", "application/json")
      .putHeader("Authorization", "Bearer $token")
      .rxSendJson(
        ChangePasswordRequestBody(
          oldPassword = FAKE_PASSWORD_ENCODED,
          newPassword = PASSWORD_ENCODED,
          newPasswordConfirm = PASSWORD_ENCODED
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("原密码不正确", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/changePassword")
      .putHeader("Content-Type", "application/json")
      .putHeader("Authorization", "Bearer $token")
      .rxSendJson(
        ChangePasswordRequestBody(
          oldPassword = PASSWORD_ENCODED,
          newPassword = "12345",
          newPasswordConfirm = "12345"
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("密码格式不正确", response["message"])


    response = client.postAbs("$BASE_URL/plarform/user/changePassword")
      .putHeader("Content-Type", "application/json")
      .putHeader("Authorization", "Bearer $token")
      .rxSendJson(
        ChangePasswordRequestBody(
          oldPassword = PASSWORD_ENCODED,
          newPassword = FAKE_PASSWORD_ENCODED,
          newPasswordConfirm = FAKE_PASSWORD_ENCODED
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("密码修改成功", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/login")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        LoginRequestBody(
          ACCOUNT,
          FAKE_PASSWORD_ENCODED
        )
      )
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    assertNotNull(response["token"])
  }


  @Test
  fun testFindPassword() {
    val account = "1015488424@qq.com"
    val passwordEncoded = "e150a1ec81e8e93e1eae2c3a77e66ec6dbd6a3b460f89c1d08aecf422ee401a0"
    val fakePasswordEncoded = "f33ae3bc9a22cd7564990a794789954409977013966fb1a8f43c35776b833a95"
    val body = RegisterRequestBody(
      USERNAME,
      passwordEncoded,
      account,
      "666666",
      0
    )
    var response = client.postAbs("$BASE_URL/plarform/user/findPassword")
      .putHeader("Content-Type", "application/json")
      .rxSend().toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("参数错误", response["message"])


    response = client.postAbs("$BASE_URL/plarform/user/findPassword")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        FindPasswordRequestBody(
          email = account,
          veriCode = "123456",
          newPassword = passwordEncoded,
          newPasswordConfirm = passwordEncoded
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("验证码不正确", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/findPassword")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        FindPasswordRequestBody(
          email = account,
          veriCode = FAKE_EMAIL_VERIFICATION_CODE,
          newPassword = passwordEncoded,
          newPasswordConfirm = fakePasswordEncoded
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("密码与确认密码不一致", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/findPassword")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        FindPasswordRequestBody(
          email = account,
          veriCode = FAKE_EMAIL_VERIFICATION_CODE,
          newPassword = "123456",
          newPasswordConfirm = "123456"
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("密码格式不正确", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/findPassword")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        FindPasswordRequestBody(
          email = account,
          veriCode = FAKE_EMAIL_VERIFICATION_CODE,
          newPassword = passwordEncoded,
          newPasswordConfirm = passwordEncoded
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("用户不存在", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(body)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("注册成功", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/findPassword")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        FindPasswordRequestBody(
          email = account,
          veriCode = FAKE_EMAIL_VERIFICATION_CODE,
          newPassword = fakePasswordEncoded,
          newPasswordConfirm = fakePasswordEncoded
        )
      ).toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("密码修改成功", response["message"])

    response = client.postAbs("$BASE_URL/plarform/user/login")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(
        LoginRequestBody(
          account,
          fakePasswordEncoded
        )
      )
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    assertNotNull(response["token"])
  }

  @Test
  fun testSendVerificationCode() {
    var responseBody = client.postAbs("$BASE_URL/plarform/user/sendVerificationCode")
      .rxSendJson(
        SendVerificationCodeRequestBody(
          FAKE_EMAIL_VERIFICATION_CODE
        )
      )
      .toBlocking()
      .value().bodyAsString()
    var responseBodyJson: SendVerificationCodeResponseBody? = null
    try {
      responseBodyJson = Json.decodeValue(responseBody, SendVerificationCodeResponseBody::class.java)

    } catch (e: Exception) {
      assertTrue(false)
    }
    assertNotNull(responseBodyJson)
    assertEquals(responseBodyJson?.code, ERROR)
    assertEquals(responseBodyJson?.message, "邮箱格式不正确")
//    responseBody = client.postAbs("$BASE_URL/plarform/user/sendVerificationCode")
//      .rxSendJson(SendVerificationCodeRequestBody("1015488424@qq.com"))
//      .toBlocking()
//      .value().bodyAsString()
//    println(responseBody)
//    try {
//      responseBodyJson = Json.decodeValue(responseBody, SendVerificationCodeResponseBody::class.java)
//
//    } catch (e: Exception) {
//      assertTrue(false)
//    }
//    assertNotNull(responseBodyJson)
//    assertEquals(responseBodyJson?.code, SUCCESS)
//    assertEquals(responseBodyJson?.message, "邮件发送成功")
  }

}
