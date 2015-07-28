package repositories.authentication

import anorm.SqlParser._
import anorm._
import models.authentication.User
import play.api.Play.current
import play.api.db.DB
import models.authentication.TemporaryUser


import scala.language.postfixOps

trait UserRepository {

  private[repositories] val recordMapper = {
    long("users.id") ~
      str("users.email") ~
      get[Option[String]]("users.first_name") ~
      get[Option[String]]("users.last_name") ~
      str("users.encrypted_password") ~
      str("users.company") map {
      case id ~ email ~ firstName ~ lastName ~ password ~ company => {
        User(id, email, firstName.getOrElse(""), lastName.getOrElse(""), password, company)
      }
    }
  }

  def create(user: TemporaryUser): Unit = {
    DB.withConnection { implicit c =>
      SQL("insert into users (email,first_name,last_name,encrypted_password,company) values " +
        "({email},{first_name},{last_name},{encrypted_password},{company})").on(
          'email -> user.email,
          'first_name -> user.firstName,
          'last_name -> user.lastName,
          'encrypted_password -> user.password,
          'company -> user.company
        ).executeInsert()
    }
  }

  def delete(email:String): Unit = {
    DB.withConnection { implicit c =>
      SQL("delete from users  where email = " +
        "{email}").on(
          'email -> email
        ).executeUpdate()
    }
  }

  def list(): Seq[User] = {
    DB.withConnection { implicit current =>
      SQL(
        """
          SELECT * FROM users
          ORDER BY id
        """
      )
        .on("enabled" -> true)
        .as(recordMapper *)
        .toList
    }
  }

  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit current =>
      SQL(
        """
          SELECT users.*
          FROM users
          WHERE users.email = {email}
        """
      )
        .on("email" -> email)
        .as(recordMapper.singleOpt)
    }
  }

  def findById(id: Long): Option[User] = {
    DB.withConnection { implicit current =>
      SQL(
        """
          SELECT users.*
          FROM users
          WHERE users.id = {id}
        """
      )
        .on("id" -> id)
        .as(recordMapper.singleOpt)
    }
  }

}

object UserRepository extends UserRepository