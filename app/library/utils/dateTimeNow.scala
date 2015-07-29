package library.utils


object dateTimeNow {
def now() =java.time.LocalDateTime.now(java.time.ZoneId.of("Europe/Paris"))
}
