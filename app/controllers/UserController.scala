package controllers


import javax.inject.Inject

import components.mvc.AuthController
import components.user.{PasswordAuthentication, SessionManager}


import models.authentication.{LoginValues, TemporaryUser,EditUser,User,EditPassword}


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

      "company" -> nonEmptyText
    )(EditUser.apply)(EditUser.unapply)
  )
  val editPasswordForm : Form[EditPassword]= Form(
    mapping(
      "oldPassword" -> nonEmptyText,
      "newPassword" -> nonEmptyText
    )(EditPassword.apply)(EditPassword.unapply)
  )

  val form: Form[LoginValues] = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginValues.apply)(LoginValues.unapply)
  )
  def profile(id: Long) = AuthenticatedAction() { implicit request =>
    val user = repositories.authentication.UserRepository.findById(id)
    user match {
      case Some(u) => Ok(views.html.users.profile(u))
      case None => SessionManager.destroy(Ok(views.html.authentication.authentication(form)))
    }

  }
  def myProfile(id: Long) = AuthenticatedAction() { implicit request =>
    val user = repositories.authentication.UserRepository.findById(id)

    user match {
      case Some(u) => {
        val tasks = models.SuspectRow.findByAdmin(u.email).length
        Ok(views.html.myaccount.designProfile(u,tasks))
      }
      case None => SessionManager.destroy(Ok(views.html.authentication.authentication(form)))
    }

  }

  def allUsers = AuthenticatedAction() { implicit request =>
    val userList = repositories.authentication.UserRepository.list().toList
    Ok(views.html.users.listUsers(userList,request.user))
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
    Ok(views.html.users.listUsers(userList,request.user))
  }

  def newUser = AuthenticatedAction() { implicit request =>
    Ok(views.html.users.addUser(addUserForm,request.user))
  }

  def editUser() = AuthenticatedAction(){ implicit request =>



    val user =models.authentication.EditUser(request.user.firstName,request.user.lastName,request.user.password,request.user.company)
    Ok(views.html.myaccount.designEdit(editUserForm.fill(user.copy(password="")),request.user))


  }
  def editPassword() = AuthenticatedAction(){ implicit request =>
    Ok(views.html.myaccount.designEditPassword(editPasswordForm,request.user))
  }

  def saveEditionUser() = AuthenticatedAction(){ implicit request =>
    val cuser =models.authentication.EditUser(request.user.firstName,request.user.lastName,request.user.password,request.user.company)

    editUserForm.bindFromRequest.fold(
      error => {

        // Request payload is invalid.envisageable

        BadRequest(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser),request.user))

      },
      success => {

        val filledForm = editUserForm.fill(success.copy(password=""))

        UserRepository.findByEmail(request.user.email) match {
          case Some(user) =>

            if (PasswordAuthentication.authenticate( success.password,user.password)) {
              val newUser = User(user.id,user.email,success.firstName,success.lastName,PasswordAuthentication.passwordHash(success.password),success.company)
              val tasks = models.SuspectRow.findByAdmin(newUser.email).length
              repositories.authentication.UserRepository.editUser(newUser)
              Ok(views.html.myaccount.designProfile(newUser,tasks))

            }
            else {


              Unauthorized(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser.copy(password="")),request.user))

        }

          case None =>
            Unauthorized(views.html.myaccount.designEdit(editUserForm.withGlobalError("error.invalidPassword").fill(cuser.copy(password="")),request.user))
        }
      }
    )

  }
  def saveEditionPassword() = AuthenticatedAction(){ implicit request =>
    editPasswordForm.bindFromRequest.fold(
      error => {

        // Request payload is invalid.envisageable
        BadRequest(views.html.myaccount.designEditPassword(editPasswordForm.withGlobalError("error.invalidPassword"),request.user))
      },
      success => {

        val filledForm = editPasswordForm.fill(success)

        UserRepository.findByEmail(request.user.email) match {
          case Some(user) =>
            if (PasswordAuthentication.authenticate( success.oldPassword,user.password)) {
              val newUser = User(user.id,user.email,user.firstName,user.lastName,PasswordAuthentication.passwordHash(success.newPassword),user.company)
              val tasks = models.SuspectRow.findByAdmin(newUser.email).length
              repositories.authentication.UserRepository.editPassword(newUser)
              Ok(views.html.myaccount.designProfile(newUser,tasks))

            }
            else {
              Unauthorized(views.html.myaccount.designEditPassword(editPasswordForm.withGlobalError("error.invalidPassword"),request.user))
            }
          case None =>
            Unauthorized(views.html.myaccount.designEditPassword(editPasswordForm.withGlobalError("error.invalidPassword"),request.user))

        }
      }
    )

  }

}