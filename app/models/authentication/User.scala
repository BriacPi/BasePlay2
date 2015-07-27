/**
 * Copyright (C) 2015 Captain Dash - All Rights Reserved
 */

package models.authentication

import play.api.libs.json._

case class User(id: Long, email: String, firstName: String, lastName: String, password: String, company:String) {

}

object User {
  
  /**
   * JSON writer serializer
   */
  implicit val writer = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id" -> user.id,
      "email" -> user.email,
      "last_name" -> user.lastName,
      "first_name" -> user.firstName,
      "company" -> user.company
    )
  }

  /**
   * JSON reader serializer
   */
  // implicit val reader = new Reads[User] = (
  //   (JsPath \ "id").read[Long] and
  //   (JsPath \ "email").read[Int](min(0) keepAnd max(150)) and
  //   (JsPath \ "role").readNullable[String]
  // )(Resident.apply _)

}

