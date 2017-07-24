package hammock

import java.text.SimpleDateFormat
import java.util.Date

package object hi {
  implicit object JVMDateFormatter extends DateFormatter {
    private val fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
    def format(date: Date): String = fmt.format(date)
  }
}
