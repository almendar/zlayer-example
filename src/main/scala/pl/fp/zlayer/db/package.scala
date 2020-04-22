package pl.fp.zlayer

import zio.{Has, IO, ZIO}

package object db {

  type UserId   = Long
  type UserRepo = Has[UserRepo.Service]

  def validate(u: User): IO[DBError, User] = IO.succeed(User())

  def getUser(userId: UserId): ZIO[UserRepo, DBError, Option[User]] =
    ZIO.accessM(_.get.getUser(userId))

  def createUser(user: User): ZIO[UserRepo, DBError, Unit] = ZIO.accessM(_.get.createUser(user))

}
