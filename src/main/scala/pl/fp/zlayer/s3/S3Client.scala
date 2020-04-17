package pl.fp.zlayer.s3

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import zio.blocking.Blocking
import zio._

object S3Client {

  trait Service {
    def upload(key: Key, data: Array[Byte]): IO[S3Error, Long]
    def download(key: String): IO[S3Error, Array[Byte]]
  }

  def createS3Client(awsConfig: S3Config): ZManaged[Blocking, Throwable, AmazonS3] =
    ZManaged.make(
      Task(
        AmazonS3ClientBuilder
          .standard()
          .withClientConfiguration(
            new ClientConfiguration()
              .withConnectionTimeout(ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT * 3)
              .withSocketTimeout(ClientConfiguration.DEFAULT_SOCKET_TIMEOUT * 3)
              .withMaxErrorRetry(6)
          )
          .withCredentials(
            new AWSStaticCredentialsProvider(
              new BasicAWSCredentials(awsConfig.accessKey, awsConfig.secretKey)
            )
          )
          .withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration(awsConfig.endpoint, null)
          )
          .build()
      )
    ) { client =>
      import zio.blocking.effectBlockingIO
      effectBlockingIO(client.shutdown()).orDie
    }

  def liveS3ClientLayer(s3Config: S3Config): ZLayer[Blocking, Throwable, Has[AmazonS3]] =
    ZLayer.fromManaged {
      createS3Client(s3Config)
    }

  val live: ZLayer[Has[AmazonS3] with Blocking, Nothing, S3Client] =
    ZLayer.fromFunction[Has[AmazonS3] with Blocking, Service] { env =>
      new Service {
        val client = env.get[AmazonS3]

        override def upload(key: Key, data: Array[Byte]): IO[S3Error, Long] = ???

        override def download(key: String): IO[S3Error, Array[Byte]] = ???
      }
    }
}

final case class S3Config(bucket: Bucket, accessKey: String, secretKey: String, endpoint: String)

sealed trait S3Error
