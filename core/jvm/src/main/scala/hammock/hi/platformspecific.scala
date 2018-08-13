package hammock
package hi

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object platformspecific {
  implicit object JVMDateFormatter extends DateFormatter {
    private val fmt                         = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O")
    def format(date: ZonedDateTime): String = date.format(fmt)
  }
}
