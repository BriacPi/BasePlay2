package controllers


import javax.inject.Inject

import components.mvc.AuthController
import components.user.{PasswordAuthentication, SessionManager}

import models.authentication.{LoginValues, TemporaryUser}

import play.api.data.Form
import play.api.data.Forms._

import play.api.libs.ws.WSClient
import play.api.mvc._
import repositories.authentication.UserRepository


class UserController @Inject()(ws: WSClient) extends AuthController {
  val addUserForm : Form[TemporaryUser]= Form(
    mapping(
      "email" -> nonEmptyText,
      "firstName"->nonEmptyText,
      "lastName"->nonEmptyText,
      "password" -> nonEmptyText,
      "company" -> nonEmptyText
    )(TemporaryUser.apply)(TemporaryUser.unapply)
  )
  val form: Form[LoginValues] = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginValues.apply)(LoginValues.unapply)
  )
  def profil(id: Long) = AuthenticatedAction() { implicit request =>
    val user = repositories.authentication.UserRepository.findById(id)
    user match {
      case Some(u) => Ok(views.html.users.profil(u))
      case None => SessionManager.destroy(Ok(views.html.authentication.authentication(form)))
    }

  }

  def allUsers = AuthenticatedAction() { implicit request =>
    val userList = repositories.authentication.UserRepository.list().toList
    Ok(views.html.users.listUser(userList))
  }
  def addUser = AuthenticatedAction() { implicit request =>
    addUserForm.bindFromRequest.fold(
      error => {
        // binding failure, you retrieve the form containing errors:

        BadRequest(views.html.users.addUser(error))
      },
      userData => {
        val refillForm = addUserForm.fill(userData)
        UserRepository.findByEmail(userData.email) match {
          case None =>val newUser = userData.copy(password = PasswordAuthentication.passwordHash(userData.password))
            repositories.authentication.UserRepository.create(newUser)
            Redirect(routes.UserController.allUsers)
          case Some(u) =>BadRequest(views.html.users.addUser(refillForm.withGlobalError("error.usedEmail")))
        }
        /* binding success, you get the actual value. */

      }
    )
  }

  def deleteUser(email:String) = AuthenticatedAction() { implicit request =>
    UserRepository.delete(email)
    val userList = repositories.authentication.UserRepository.list().toList
    Ok(views.html.users.listUser(userList))
  }

  def newUser = AuthenticatedAction() { implicit request =>
    Ok(views.html.users.addUser(addUserForm))
  }

}