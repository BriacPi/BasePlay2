package models

case class ErrorBPCE(date : String, caisse: String, groupe: String, agence: String, pdv: String, metric: String,
                     errorType: String, firstDate: String, status: String, comment: String, admin: String)