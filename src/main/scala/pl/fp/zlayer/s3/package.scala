package pl.fp.zlayer

import zio.{Has, ZIO}

package object s3 {
  type S3Client = Has[S3Client.Service]
  type Key      = String
  type Bucket   = String

  def upload(key: Key, data: Array[Byte]): ZIO[S3Client, S3Error, Long] =
    ZIO.accessM[S3Client](_.get.upload(key, data))

  def download(key: String): ZIO[S3Client, S3Error, Array[Byte]] =
    ZIO.accessM[S3Client](_.get.download(key))
}
