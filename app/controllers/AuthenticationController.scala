/**
 * Copyright (C) 2015 Captain Dash - All Rights Reserved
 */

package controllers

import javax.inject._

import components.user.{PasswordAuthentication, SessionManager}
import models.SuspectRow
import models.authentication.{User, LoginValues}
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import repositories.authentication.UserRepository


import scala.concurrent.Future

/**
 * Controller used to manage user session.
 */
class AuthenticationController @Inject()(cache: CacheApi) extends Controller {

  val form: Form[LoginValues] = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginValues.apply)(LoginValues.unapply)
  )

  def welcome: Action[AnyContent] = Action { implicit request =>

     Ok(views.html.authentication.authentication(form))

  }

  def createUser(email:String,firstName:String,lastName:String,password:String,company:String): Action[AnyContent] = Action { implicit request =>
      UserRepository.create(new User(0,email,firstName,lastName,PasswordAuthentication.passwordHash(password),company))
      Ok("User created")
  }

  def login: Action[AnyContent] = Action { implicit request =>

    form.bindFromRequest.fold(
      error => {

        // Request payload is invalid.envisageable
        BadRequest(views.html.authentication.authentication(form.withGlobalError("error.invalidUserOrPassword")))
      },
      success => {

        // Request payload variables.
        val email = success.email
        val password = success.password
        val filledForm  =  form.fill(LoginValues(email,password))

        UserRepository.findByEmail(email) match {
          case Some(user) =>
            if (PasswordAuthentication.authenticate(password, user.password)) {
              SessionManager.create(Ok(views.html.solved(SuspectRow.filterByStatus(models.Status.DetectedOnly))),user)
            }
            else Unauthorized(views.html.authentication.authentication(filledForm.withGlobalError("error.invalidPassword")))

          case None =>
            Unauthorized(views.html.authentication.authentication(filledForm.withGlobalError("error.userNotFound")))
        }
      }
    )

  }

  def logout = Action { request =>
    SessionManager.destroy(NoContent)
    Ok(views.html.authentication.authentication(form))
  }

}