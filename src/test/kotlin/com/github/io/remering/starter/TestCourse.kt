package com.github.io.remering.starter

import com.github.io.remering.starter.api.account.LoginRequestBody
import com.github.io.remering.starter.api.account.RegisterRequestBody
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.json.get
import io.vertx.rxjava.ext.web.multipart.MultipartForm
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class TestCourse {
  companion object {

    lateinit var testFilePath: Path
    lateinit var testFileSha256: String
    lateinit var testVideoPath: Path
    lateinit var testVideoSha256: String
    lateinit var testPicturePath: Path
    lateinit var testPictureSha256: String

    @BeforeAll
    @JvmStatic
    fun deployVerticle() {
      vertx.deployVerticle(verticle.javaClass.canonicalName, deploymentOptionsOf(instances = 4))
    }

    @BeforeAll
    @JvmStatic
    fun initTestFiles() {
      testFilePath = Paths.get(this::class.java.getResource("/学生版.docx").toURI())
      testVideoPath = Paths.get(this::class.java.getResource("/三位数乘两位数.wmv").toURI())
      testPicturePath = Paths.get(this::class.java.getResource("/unknown.jpg").toURI())
      testFileSha256 = calculateSha256(Files.readAllBytes(testFilePath))
      testVideoSha256 = calculateSha256(Files.readAllBytes(testVideoPath))
      testPictureSha256 = calculateSha256(Files.readAllBytes(testPicturePath))
    }


//    @AfterAll
//    @JvmStatic
//    fun cleanUpload() {
//      Files.newDirectoryStream(Paths.get(USER_UPLOAD_FILE_DIRECTORY)).forEach(Files::delete)
//    }

  }


  @AfterEach
  fun clearUpDb() {
    verticle.deleteAll()
  }

  @Test
  fun createCourse() {
    val formdata = MultipartForm.create()
    formdata.binaryFileUpload(
      "picture", "test.jpg", testPicturePath.toString(), "image/jpeg"
    )
    formdata.binaryFileUpload(
      "video", "test.video", testVideoPath.toString(), "video/wmv"
    )
    formdata.binaryFileUpload(
      "file", "test.docx", testFilePath.toString(), "application/msword"
    )
    formdata.attribute("name", "name")
    formdata.attribute("introduction", "introduction")
    formdata.attribute("fileSha256", testFileSha256)
    formdata.attribute("videoSha256", testVideoSha256)
    formdata.attribute("pictureSha256", testPictureSha256)
    val teacherToken =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiOWRhYjMzY2EtYWEzNC00OTYwLWJjZTgtYmVmMjQ1YzE0MjRhIiwidXNlcm5hbWUiOiJFdXBob3JpYSIsImVtYWlsIjoiODc5OTY5MzU1QHFxLmNvbSIsInJvbGUiOjEsImlhdCI6MTU4NTIwMTAwN30.UU9m7sYhDxtGUbmdAelLbTs3H-yoY-0zMn8_u3-0FnY"
    val response = client.postAbs("$BASE_URL/plarform/teacher/createCourse")
      .bearerTokenAuthentication(teacherToken)
      .rxSendMultipartForm(formdata)
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("创建成功", response["message"])
    val course = response.getJsonObject("course")
    assertNotNull(course)
    assertEquals("name", course["name"])
    assertEquals("introduction", course["introduction"])
    assertNotNull(course["teacher"])
  }

  //  @Test
  fun testCreateCourse() {
    val studentRegisterRequestBody = RegisterRequestBody(
      STUDENT_USERNAME,
      STUDENT_PASSWORD_ENCODED,
      STUDENT_ACCOUNT,
      FAKE_EMAIL_VERIFICATION_CODE,
      STUDENT
    )
    val teacherRegisterRequestBody = RegisterRequestBody(
      TEACHER_USERNAME,
      TEACHER_PASSWORD_ENCODED,
      TEACHER_ACCOUNT,
      FAKE_EMAIL_VERIFICATION_CODE,
      TEACHER
    )
    var response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(studentRegisterRequestBody)
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
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    val studentToken: String? = response["token"]
    assertNotNull(studentToken)


    response = client.postAbs("$BASE_URL/plarform/user/register")
      .putHeader("Content-Type", "application/json")
      .rxSendJson(teacherRegisterRequestBody)
      .toBlocking()
      .value().bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("注册成功", response["message"])

    response =
      client.postAbs("$BASE_URL/plarform/user/login")
        .putHeader("Content-Type", "application/json")
        .rxSendJson(
          LoginRequestBody(
            TEACHER_ACCOUNT,
            TEACHER_PASSWORD_ENCODED
          )
        )
        .toBlocking()
        .value()
        .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("登录成功", response["message"])
    val teacherToken: String? = response["token"]
    assertNotNull(teacherToken)

    println("注册登录完毕")

    println("studentToken = $studentToken")
    println("teacherToken = $teacherToken")
    response = client.getAbs("$BASE_URL/plarform/user/getProfile")
      .putHeader("Authorization", "Bearer $teacherToken")
      .rxSend()

      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(SUCCESS, response["code"])
    assertEquals("操作成功", response["message"])

    println("教师UUID获取完毕")

    println("测试用户未登录")
    val rawResponse = client.postAbs("$BASE_URL/plarform/teacher/createCourse")
      .rxSend()
      .toBlocking().value()
    println(rawResponse.body())
    response = rawResponse.bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("用户未登录", response["message"])
    assertNull(response["url"])


    println("测试权限不足")
    response = client.postAbs("$BASE_URL/plarform/teacher/createCourse")
      .bearerTokenAuthentication(studentToken)
      .rxSend()
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("权限不足", response["message"])
    assertNull(response.getJsonObject("course"))

    println("测试参数错误")
    response = client.postAbs("$BASE_URL/plarform/teacher/createCourse")
      .bearerTokenAuthentication(teacherToken)
      .rxSend()
      .toBlocking()
      .value()
      .bodyAsJsonObject()
    assertEquals(ERROR, response["code"])
    assertEquals("参数错误", response["message"])
    assertNull(response.getJsonObject("course"))

//    println("测试正确样例")
//    val formdata = MultipartForm.create()
//    formdata.binaryFileUpload(
//      "picture", "test.jpg", testPicturePath.toString(), "image/jpeg"
//    )
//    formdata.binaryFileUpload(
//      "video", "test.video", testVideoPath.toString(), "video/wmv"
//    )
//    formdata.binaryFileUpload(
//      "file", "test.docx", testFilePath.toString(), "application/msword"
//    )
//    formdata.attribute("name", "name")
//    formdata.attribute("introduction", "introduction")
//    formdata.attribute("pictureSha256", testPictureSha256)
//    formdata.attribute("videoSha256", testVideoSha256)
//    formdata.attribute("fileSha256", testFileSha256)
//    response = client.postAbs("$BASE_URL/plarform/teacher/createCourse")
//      .bearerTokenAuthentication(teacherToken)
//      .rxSendMultipartForm(formdata)
//      .toBlocking()
//      .value()
//      .bodyAsJsonObject()
//

  }
}
