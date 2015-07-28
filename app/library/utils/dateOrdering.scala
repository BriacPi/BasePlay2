package library.utils

/**
 * Created by briac on 28/07/15.
 */
object dateOrdering {
    implicit def dateTimeOrdering: Ordering[java.time.LocalDate] = Ordering.fromLessThan(_ isBefore _)
}
