package pl.fp.zlayer

import zio._

package object s3 {

  type S3 = Has[S3.Service]

  type Bucket = String
  type Key    = String

  final case class S3Config(bucket: Bucket, accessKey: String, secretKey: String)

  sealed abstract class S3Error extends Product with Serializable

  object S3 extends Serializable {

    trait Service extends Serializable {
      def upload(key: Key, data: Array[Byte]): IO[S3Error, Long]

      def download(key: String): IO[S3Error, Array[Byte]]
    }

    val live: ZManaged[S3Config, Nothing, S3.Service] = ZManaged.make {
      for {
        s3Client <- ZIO.access[S3Config](_.bucket)
      } yield new Service {
        override def upload(key: Key, data: Array[Byte]): IO[S3Error, Long] = ???

        override def download(key: String): IO[S3Error, Array[Byte]] = ???
      }
    } { service => UIO.unit }

  }

  val any: ZLayer[S3, Nothing, S3] =
    ZLayer.requires[S3]

  val live: ZLayer[Has[S3Config], Nothing, Has[S3.Service]] =
    ZLayer.fromServiceManaged((cfg: S3Config) => S3.live.provide(cfg))

  def upload(key: Key, data: Array[Byte]): ZIO[S3, S3Error, Long] =
    ZIO.accessM(_.get.upload(key, data))

  def download(key: String): ZIO[S3, S3Error, Array[Byte]] = ZIO.accessM(_.get.download(key))

}
