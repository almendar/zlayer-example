import java.sql.SQLException

import pl.fp.zlayer.db.UserRepo
import pl.fp.zlayer.s3.{S3Client, S3Config}
import pl.fp.zlayer.{ClientService, db, s3}
import zio.blocking.Blocking
import zio.{App, Has, Layer, UIO, ZIO, ZLayer}

object Entry extends App {

  //read from config
//  val readDbConfig: Layer[Nothing, Has[db.DBCredentials]] =
//    ZLayer.fromEffect(UIO(db.DBCredentials("", "", "")))
//  val readS3Config: Layer[Nothing, S3Client.Config] =
//    ZLayer.fromEffect(UIO(S3Config("", "", "", "")))

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {

    val s3Config: S3Config = ???

    val s3ModuleLayer: ZLayer[Any, Throwable, S3Client] = (zio.blocking.Blocking.live >>> S3Client
      .liveS3ClientLayer(s3Config) ++ zio.blocking.Blocking.live) >>> S3Client.live

    //    val fullLayerWaitingForConfig
    //        : ZLayer[Has[db.DBCredentials] with S3Client.Config, SQLException, UserRepo with S3Client.Service] =
    //      db.fullRepo ++ S3Client.liveLayer
    //
    //    val filledLayer: Layer[Nothing, UserRepo with S3Client.Service] =
    //      (readDbConfig ++ readS3Config) >>> fullLayerWaitingForConfig.orDie
    //
    //    // suppose we get a POST request with following data
    //    ClientService
    //      .registerNewClient("", "", Array.emptyByteArray, "", Array.emptyByteArray)
    //      .provideLayer(filledLayer)
    //      .mapError(x => new RuntimeException(x.toString))
    //      .orDie
    //      .map(_ => 0)
    //  }
    ZIO.succeed(0)
  }
}
