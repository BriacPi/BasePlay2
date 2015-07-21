package library.utils
import anorm._

object dateParser {
  implicit def rowToDate: Column[java.sql.Date] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case d: java.sql.Date => Right(d)
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to date for column " + qualified))
    }
  }
}
