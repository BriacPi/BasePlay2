/**
 * Copyright (C) 2015 Captain Dash - All Rights Reserved
 */

package controllers

import javax.inject._

import components.user.{PasswordAuthentication, SessionManager}
import models.SuspectRow
import models.authentication.{LoginValues, User,TemporaryUser}
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import repositories.authentication.UserRepository

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

  def createUser(email: String, firstName: String, lastName: String, password: String, company: String): Action[AnyContent] = Action { implicit request =>

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
        val filledForm = form.fill(LoginValues(email, password))

        UserRepository.findByEmail(email) match {
          case Some(user) =>
            if (PasswordAuthentication.authenticate(password, user.password)) {
              SessionManager.create(Ok(views.html.detectedOnly(user)), user)
            }
            else Unauthorized(views.html.authentication.authentication(filledForm.withGlobalError("error.invalidPassword")))

          case None =>
            Unauthorized(views.html.authentication.authentication(filledForm.withGlobalError("error.userNotFound")))
        }
      }
    )
  }


  def logout = Action { implicit request =>
    SessionManager.destroy(Ok(views.html.authentication.authentication(form)))

  }

}