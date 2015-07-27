/**
 * Copyright (C) 2015 Captain Dash - All Rights Reserved
 */

package components.user

import org.mindrot.jbcrypt.BCrypt

trait PasswordAuthentication {

  /**
   * Check that an unencrypted password matches one that has
   * previously been hashed.
   */
  def authenticate(password: String, hash: String): Boolean = {
    BCrypt.checkpw(password, hash)
  }

   def passwordHash(password: String): String = {
     val salt = BCrypt.gensalt(10)
     BCrypt.hashpw(password, salt)
   }

}

object PasswordAuthentication extends PasswordAuthentication