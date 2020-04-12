import java.sql.SQLException

import pl.fp.zlayer.db.UserRepo
import pl.fp.zlayer.s3.S3
import pl.fp.zlayer.services.user.ClientService
import pl.fp.zlayer.{db, s3}
import zio.{App, Has, Layer, UIO, ZIO, ZLayer}

object Entry extends App {

  //read from config
  val readDbConfig: Layer[Nothing, Has[db.DBCredentials]] =
    ZLayer.fromEffect(UIO(db.DBCredentials("", "", "")))
  val readS3Config: Layer[Nothing, Has[s3.S3Config]] =
    ZLayer.fromEffect(UIO(s3.S3Config("", "", "")))

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

    val fullLayerWaitingForConfig
        : ZLayer[Has[db.DBCredentials] with Has[s3.S3Config], SQLException, UserRepo with Has[
          S3.Service
        ]] =
      db.fullRepo ++ s3.live

    val filledLayer: Layer[Nothing, UserRepo with s3.S3] =
      (readDbConfig ++ readS3Config) >>> fullLayerWaitingForConfig.orDie

    //suppose we get a POST request with following data
    ClientService
      .registerNewClient("", "", Array.emptyByteArray, "", Array.emptyByteArray)
      .provideLayer(filledLayer)
      .mapError(x => new RuntimeException(x.toString))
      .orDie
      .map(_ => 0)
  }
}
