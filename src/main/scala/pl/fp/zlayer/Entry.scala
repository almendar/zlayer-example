import java.sql.SQLException

import pl.fp.zlayer.db.UserRepo
import pl.fp.zlayer.s3.{S3Client, S3Module}
import pl.fp.zlayer.services.user.ClientService
import pl.fp.zlayer.{db, s3}
import zio.{App, Has, Layer, UIO, ZIO, ZLayer}

object Entry extends App {

  //read from config
  val readDbConfig: Layer[Nothing, Has[db.DBCredentials]] =
    ZLayer.fromEffect(UIO(db.DBCredentials("", "", "")))
  val readS3Config: Layer[Nothing, S3Module.Config] =
    ZLayer.fromEffect(UIO(S3Client.S3Config("", "", "", "")))

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

    val fullLayerWaitingForConfig
        : ZLayer[Has[db.DBCredentials] with S3Module.Config, SQLException, UserRepo with S3Module.Service] =
      db.fullRepo ++ S3Module.liveLayer

    val filledLayer: Layer[Nothing, UserRepo with S3Module.Service] =
      (readDbConfig ++ readS3Config) >>> fullLayerWaitingForConfig.orDie

    // suppose we get a POST request with following data
    ClientService
      .registerNewClient("", "", Array.emptyByteArray, "", Array.emptyByteArray)
      .provideLayer(filledLayer)
      .mapError(x => new RuntimeException(x.toString))
      .orDie
      .map(_ => 0)
  }
}
