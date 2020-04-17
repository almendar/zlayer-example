package pl.fp.zlayer.db

import java.sql.{Connection, SQLException}

import zio.{Has, IO, UIO, URIO, ZLayer}

case class User()

final case class DBCredentials(user: String, password: String, connString: String)

sealed abstract class DBError extends Product with Serializable

object UserRepo {

  trait Service {
    def getUser(userId: UserId): IO[DBError, Option[User]]

    def createUser(user: User): IO[DBError, Unit]
  }

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
