package controllers


import javax.inject.Inject

import components.mvc.AuthController
import components.user.SessionManager

import models.authentication.{LoginValues, TemporaryUser}

import play.api.data.Form
import play.api.data.Forms._

import play.api.libs.ws.WSClient
import play.api.mvc._




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
        println(error)
        BadRequest(views.html.users.addUser(error))
      },
      userData => {
        /* binding success, you get the actual value. */

        repositories.authentication.UserRepository.create(userData)
        Redirect(routes.UserController.allUsers)
      }
    )

  }

  def addPage = AuthenticatedAction() { implicit request =>
    Ok(views.html.users.addUser(addUserForm))
  }

}