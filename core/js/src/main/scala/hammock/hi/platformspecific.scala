package hammock
package hi

import java.time.ZonedDateTime
import scalajs.js.{Date => JsDate}

object platformspecific {
  def convert(d: ZonedDateTime): JsDate = new JsDate(d.toInstant.toEpochMilli.toDouble)

  implicit object JSDateFormatter extends DateFormatter {
    def format(date: ZonedDateTime): String = fmt(convert(date))

    def fmt(date: JsDate): String = date.toUTCString
  }
}
