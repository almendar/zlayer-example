package pl.fp.zlayer

import pl.fp.zlayer.db.UserRepo
import pl.fp.zlayer.s3.S3Client
import zio.ZIO

abstract class ClientServiceError               extends Product with Serializable
final case class CouldNotRegiser(login: String) extends ClientServiceError

final class ClientService {

  def registerNewClient(
      name: String,
      login: String,
      password: Array[Byte],
      city: String,
      avatar: Array[Byte]
  ): ZIO[S3Client with UserRepo, ClientServiceError, Unit] = {
    for {
//      _    <- db.validate(db.User()).mapError(x => CouldNotRegiser(login))
      user <- ZIO.accessM[UserRepo](_.get.createUser(db.User()))
      _    <- db.createUser(user).mapError(x => CouldNotRegiser(login))
      _    <- s3.upload(s"avatar_${login}", avatar).mapError(x => CouldNotRegiser(login))
    } yield ()
  }
}
