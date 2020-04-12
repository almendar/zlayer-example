package pl.fp.zlayer.s3

import zio.IO
import zio.ZIO
import zio.Task
import zio.ZLayer
import zio.ZManaged
import zio.Has

object S3Module {
  import S3Client._

  type Service = Has[S3Client]
  type Config  = Has[S3Config]

  val live: ZManaged[S3Config, Nothing, S3Client] = {
    val s3Client: ZIO[S3Config, Nothing, S3Client] = for {
      cfg <- ZIO.environment[S3Config]
      service <- Task {
                  new S3Client {
                    def upload(key: Key, data: Array[Byte]): IO[S3Error, Long] = ???
                    def download(key: String): IO[S3Error, Array[Byte]]        = ???
                  }
                }.orDie
    } yield service

    ZManaged.make(s3Client)(_ => Task.unit)
  }

  val liveLayer: ZLayer[Config, Nothing, Service] = {
    ZLayer.fromServiceManaged(cfg => live.provide(cfg))
  }
}
