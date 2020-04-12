package pl.fp.zlayer

import java.sql.{Connection, SQLException}

import zio.{Has, IO, Layer, UIO, URIO, ZIO, ZLayer}

package object db {

  type UserId = Long

  case class User()

  final case class DBCredentials(user: String, password: String, connString: String)

  sealed abstract class DBError extends Product with Serializable

  def validate(u: User): IO[DBError, User] = IO.succeed(User())

  type UserRepo = Has[UserRepo.Service]

  object UserRepo {

    trait Service {
      def getUser(userId: UserId): IO[DBError, Option[User]]

      def createUser(user: User): IO[DBError, Unit]
    }
  }

  def getUser(userId: UserId): ZIO[UserRepo, DBError, Option[User]] =
    ZIO.accessM(_.get.getUser(userId))

  def createUser(user: User): ZIO[UserRepo, DBError, Unit] = ZIO.accessM(_.get.createUser(user))

  def makeConnection: URIO[Has[DBCredentials], Connection] = UIO(???)

  val connectionLayer: ZLayer[Has[DBCredentials], SQLException, Has[Connection]] =
    ZLayer.fromAcquireRelease(makeConnection)(c => UIO(c.close()))

  val postgresLayer: ZLayer[Has[Connection], Nothing, UserRepo] =
    ZLayer.fromFunction { hasC =>
      new UserRepo.Service {
        override def getUser(userId: UserId): IO[DBError, Option[User]] = UIO(???)

        override def createUser(user: User): IO[DBError, Unit] = UIO(???)
      }
    }

  val fullRepo: ZLayer[Has[DBCredentials], SQLException, UserRepo] =
    connectionLayer >>> postgresLayer

}
