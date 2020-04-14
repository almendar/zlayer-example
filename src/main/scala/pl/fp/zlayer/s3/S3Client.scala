package pl.fp.zlayer.s3

import com.amazonaws.services.s3.transfer.TransferManager
import pl.fp.zlayer.s3.S3Client.Key
import zio.{IO, Task, UIO, ZIO, ZLayer, ZManaged}

trait S3Client {
  import S3Client._

  def upload(key: Key, data: Array[Byte]): IO[S3Error, Long]
  def download(key: String): IO[S3Error, Array[Byte]]
}

object S3Client {
  type Key    = String
  type Bucket = String

  sealed trait S3Error
  final case class S3Config(bucket: Bucket, accessKey: String, secretKey: String, endpoint: String)

  def live: ZManaged[TransferManager, Nothing, S3Client] =
    ZManaged.make(
      ZIO.access[TransferManager](new S3Live(_))
    )(x => UIO(x.tm.shutdownNow()))

  //def mock: S3Client How to pass Console here?

}

private class S3Live(val tm: TransferManager) extends S3Client {
  override def upload(key: Key, data: Array[Byte]): IO[S3Client.S3Error, Long] = ???

  override def download(key: String): IO[S3Client.S3Error, Array[Byte]] = ???

}
