package pl.fp.zlayer.s3

import java.util.concurrent.ExecutorService

import com.amazonaws.ClientConfiguration
import com.amazonaws.services.s3.transfer.{TransferManager, TransferManagerBuilder}
import zio.{Has, IO, Task, ZIO, ZLayer, ZManaged, blocking}
import com.amazonaws.auth.{
  AWSStaticCredentialsProvider,
  BasicAWSCredentials,
  DefaultAWSCredentialsProviderChain
}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.client.builder.ExecutorFactory
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import pl.fp.zlayer.s3.S3Module.Config
import zio.blocking.Blocking

object S3Module {
  import S3Client._

  type Config  = Has[S3Config]
  type Service = Has[S3Client]
  type FFI     = TransferManager

  val p: ZManaged[Blocking with Config, Nothing, FFI] = ZManaged.make {
    for {
      s3Config         <- ZIO.access[Config](_.get)
      blockingExecutor <- ZIO.access[Blocking](_.get.blockingExecutor)
    } yield {
      val clientConfig = new ClientConfiguration()

      val execFac: ExecutorFactory = () => blockingExecutor.asECES

      val client = AmazonS3ClientBuilder
        .standard()
        .withCredentials(
          new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(s3Config.accessKey, s3Config.secretKey)
          )
        )
        .withClientConfiguration(clientConfig)
        .withEndpointConfiguration(new EndpointConfiguration(s3Config.endpoint, null))
        .build()
      TransferManagerBuilder.standard().withS3Client(client).withExecutorFactory(execFac).build()
    }
  }(x => blocking.effectBlockingIO(x.shutdownNow()).orDie)

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

  def upload(key: Key, data: Array[Byte]): ZIO[Service, S3Error, Long] =
    ZIO.accessM[Service](_.get.upload(key, data))

}
