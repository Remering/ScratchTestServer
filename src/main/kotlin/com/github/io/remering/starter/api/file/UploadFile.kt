package com.github.io.remering.starter.api.file

import com.github.io.remering.starter.ERROR
import com.github.io.remering.starter.SUCCESS
import com.github.io.remering.starter.calculateSha256
import com.github.io.remering.starter.jwtAuthHandler
import io.vertx.core.json.Json
import io.vertx.reactivex.ext.web.Router
import java.io.File

class UploadFileResponseBody(
  val code: Int,
  val message: String,
  val url: String? = null
)

fun Router.mountUploadFile() {
  post("/uploadFile")
    .handler(jwtAuthHandler)
    .handler { context ->
      val sha256 = context.request().getFormAttribute("sha256")
      val fileUpload = context.fileUploads().firstOrNull()
      if (sha256 == null || fileUpload == null) {
        context.response().end(Json.encode(UploadFileResponseBody(ERROR, "参数错误")))
        return@handler
      }
      context.vertx().fileSystem()
        .rxReadFile(fileUpload.uploadedFileName()).subscribe { buffer, error ->
          if (error != null) {
            context.fail(error)
            return@subscribe
          }
          val calculatedSha256 = calculateSha256(buffer.bytes)
          if (calculatedSha256 != sha256) {
            context.vertx().fileSystem().deleteBlocking(fileUpload.uploadedFileName())
            context.response().end(Json.encode(
              UploadFileResponseBody(
              ERROR, "上传文件校验失败"
              )
            ))
            return@subscribe
          }

          var filePath = fileUpload.uploadedFileName()
          if (File.separatorChar != '/') {
            filePath = filePath.replace(File.separatorChar, '/')
          }
          context.response().end(Json.encode(
            UploadFileResponseBody(
              SUCCESS, "文件上传成功",
              "http://${context.request().host()}/$filePath"
            )
          ))
      }
    }
}

