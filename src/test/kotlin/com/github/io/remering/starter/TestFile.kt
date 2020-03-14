package com.github.io.remering.starter

import com.github.io.remering.starter.api.account.LoginRequestBody
import com.github.io.remering.starter.api.account.RegisterRequestBody
import com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureDSA
import io.vertx.kotlin.core.json.get
import io.vertx.rxjava.ext.web.multipart.MultipartForm
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigInteger
import java.nio.file.*
import java.security.MessageDigest
import java.util.*


const val TEST_FILE_NAME = "Helloworld.txt"

class TestFile {
  companion object {

    lateinit var testPath: Path
    lateinit var testFileSha256: String
    @BeforeAll
    @JvmStatic
    fun deployVerticle() {
      vertx.deployVerticle(verticle)
    }

    @BeforeAll
    @JvmStatic
    fun createTestFile() {
      testPath = Files.createTempFile("test", "txt")
      Files.newBufferedWriter(testPath).use {
        it.write("Hello, world!")
      }
      testFileSha256 = Files.newInputStream(testPath).use {
        calculateSha256(it.readBytes())
      }

    }

    @AfterAll
    @JvmStatic
    fun cleanUpload() {
      Files.newDirectoryStream(Paths.get(USER_UPLOAD_FILE_DIRECTORY)).forEach(Files::delete)
    }

  }

  @AfterEach
  fun clearUpDb() {
    verticle.deleteAll()
  }

  @Test
  fun testUploadFile() {
    val registerRequestBody = RegisterRequestBody(
      USERNAME,
      PASSWORD_ENCODED,
      "1015488424@qq.com",
      "666666",
      0
    )
    var response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(registerRequestBody)
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

    response = client.postAbs("$BASE_URL/plarform/user/uploadFile")
      .rxSend()
      .toBlocking().value()
      .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("用户未登录", response["message"])
    assertNull(response["url"])

    response = client.postAbs("$BASE_URL/plarform/user/uploadFile")
      .putHeader("Authorization", "Bearer $token")
      .rxSend()
      .toBlocking().value()
      .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("参数错误", response["message"])
    assertNull(response["url"])

    var form = MultipartForm.create()
    form.textFileUpload(
      "file",
      TEST_FILE_NAME, testPath.toAbsolutePath().toString(), "text/plain"
    )
    form.attribute("sha256", UUID.randomUUID().toString())

    response = client.postAbs("$BASE_URL/plarform/user/uploadFile")
      .putHeader("Authorization", "Bearer $token")
      .rxSendMultipartForm(form)
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("上传文件校验失败", response["message"])
    assertNull(response["url"])

    form = MultipartForm.create()
    form.textFileUpload(
      "file",
      TEST_FILE_NAME, testPath.toAbsolutePath().toString(), "text/plain"
    )
    form.attribute("sha256", testFileSha256)
    response = client.postAbs("$BASE_URL/plarform/user/uploadFile")
      .putHeader("Authorization", "Bearer $token")
      .rxSendMultipartForm(form)
      .toBlocking()
      .value()
      .bodyAsJsonObject()

    assertEquals(SUCCESS, response["code"])
    assertEquals("文件上传成功", response["message"])
    assertNotNull(response["url"])
  }

  @Test
  fun testGetFile() {
    val registerRequestBody = RegisterRequestBody(
      USERNAME,
      PASSWORD_ENCODED,
      "1015488424@qq.com",
      "666666",
      0
    )
    var response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(registerRequestBody)
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

    val form = MultipartForm.create()
    form.textFileUpload(
      "file",
      TEST_FILE_NAME, testPath.toAbsolutePath().toString(), "text/plain"
    )
    form.attribute("sha256", testFileSha256)
    response = client.postAbs("$BASE_URL/plarform/user/uploadFile")
      .putHeader("Authorization", "Bearer $token")
      .rxSendMultipartForm(form)
      .toBlocking()
      .value()
      .bodyAsJsonObject()

    assertEquals(SUCCESS, response["code"])
    assertEquals("文件上传成功", response["message"])
    val url: String? = response["url"]
    assertNotNull(url)

    println(url)
    val rawResponseBody =
      client.getAbs(url)
        .rxSend()
        .toBlocking()
        .value()
        .body().bytes
    val testBuffer = Files.readAllBytes(testPath)
    assertArrayEquals(testBuffer, rawResponseBody)

  }
}
