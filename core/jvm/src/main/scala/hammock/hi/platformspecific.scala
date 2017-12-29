package hammock
package hi

import java.text.SimpleDateFormat
import java.util.Date

object platformspecific {
  implicit object JVMDateFormatter extends DateFormatter {
    private val fmt                = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
    def format(date: Date): String = fmt.format(date)
  }
}
