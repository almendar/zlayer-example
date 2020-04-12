package pl.fp.zlayer.s3

import zio.IO

trait S3Client {
  import S3Client._

  def upload(key: Key, data: Array[Byte]): IO[S3Error, Long]
  def download(key: String): IO[S3Error, Array[Byte]]
}

object S3Client {
  type Key    = String
  type Bucket = String

  sealed trait S3Error
  final case class S3Config(bucket: Bucket, accessKey: String, secretKey: String)
}
