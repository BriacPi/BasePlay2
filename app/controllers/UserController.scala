package controllers


import javax.inject.Inject

import components.mvc.AuthController
import components.user.{PasswordAuthentication, SessionManager}

import models.authentication.{LoginValues, TemporaryUser,EditUser,User}

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

  val editUserForm : Form[EditUser]= Form(
    mapping(
      "firstName"->nonEmptyText,
      "lastName"->nonEmptyText,
      "oldPassword" -> nonEmptyText,
      "newPassword" -> nonEmptyText,
      "company" -> nonEmptyText
    )(EditUser.apply)(EditUser.unapply)
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
  def myProfil(id: Long) = AuthenticatedAction() { implicit request =>
    val user = repositories.authentication.UserRepository.findById(id)
    user match {
      case Some(u) => Ok(views.html.users.currentUser(u))
      case None => SessionManager.destroy(Ok(views.html.authentication.authentication(form)))
    }

  }

  def allUsers = AuthenticatedAction() { implicit request =>
    val userList = repositories.authentication.UserRepository.list().toList
    Ok(views.html.users.listUser(userList,request.user))
  }
  def addUser = AuthenticatedAction() { implicit request =>
    addUserForm.bindFromRequest.fold(
      error => {
        // binding failure, you retrieve the form containing errors:

        BadRequest(views.html.users.addUser(error,request.user))
      },
      userData => {
        val refillForm = addUserForm.fill(userData)
        UserRepository.findByEmail(userData.email) match {
          case None =>
            val newUser = userData.copy(password = PasswordAuthentication.passwordHash(userData.password))
            repositories.authentication.UserRepository.create(newUser)
            Redirect(routes.UserController.allUsers)
          case Some(u) =>BadRequest(views.html.users.addUser(refillForm.withGlobalError("error.usedEmail"),request.user))
        }
        /* binding success, you get the actual value. */

      }
    )
  }

  def deleteUser(email:String) = AuthenticatedAction() { implicit request =>
    UserRepository.delete(email)
    val userList = repositories.authentication.UserRepository.list().toList
    Ok(views.html.users.listUser(userList,request.user))
  }

  def newUser = AuthenticatedAction() { implicit request =>
    Ok(views.html.users.addUser(addUserForm,request.user))
  }

  def editUser() = AuthenticatedAction(){ implicit request =>
    Ok(views.html.users.editUser(editUserForm,request.user))
  }

  def saveEdition() = AuthenticatedAction(){ implicit request =>
    editUserForm.bindFromRequest.fold(
      error => {

        // Request payload is invalid.envisageable
        BadRequest(views.html.users.editUser(editUserForm.withGlobalError("error.invalidUserOrPassword"),request.user))
      },
      success => {

        val filledForm = editUserForm.fill(success)

        UserRepository.findByEmail(request.user.email) match {
          case Some(user) =>
            println( success.oldPassword,user.password)
            if (PasswordAuthentication.authenticate( success.oldPassword,user.password)) {
              val newUser = User(user.id,user.email,success.firstName,success.lastName,PasswordAuthentication.passwordHash(success.newPassword),success.company)
              repositories.authentication.UserRepository.editUser(newUser)
              Ok(views.html.users.currentUser(newUser))

            }
            else {
            println("Unquthorized")
              Unauthorized(views.html.users.editUser(editUserForm.withGlobalError("error.invalidUserOrPassword"),request.user))
        }
          case None =>
            println("NO USER")
            Unauthorized(views.html.users.editUser(editUserForm.withGlobalError("error.invalidUserOrPassword"),request.user))
        }
      }
    )

  }

}