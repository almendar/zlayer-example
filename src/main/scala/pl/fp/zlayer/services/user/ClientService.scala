package pl.fp.zlayer.services.user

import pl.fp.zlayer.db.UserRepo
import pl.fp.zlayer.{db, s3}
import zio.{IO, ZIO}
import pl.fp.zlayer.s3.S3Module

object ClientService {

  abstract class ClientServiceError               extends Product with Serializable
  final case class CouldNotRegiser(login: String) extends ClientServiceError

  def registerNewClient(
      name: String,
      login: String,
      password: Array[Byte],
      city: String,
      avatar: Array[Byte]
  ): ZIO[S3Module.Service with UserRepo, ClientServiceError, Unit] = {
    for {
      user <- db.validate(db.User()).mapError(x => CouldNotRegiser(login))
      _    <- db.createUser(user).mapError(x => CouldNotRegiser(login))
      _    <- S3Module.upload(s"avatar_${login}", avatar).mapError(x => CouldNotRegiser(login))
    } yield ()
  }
}
