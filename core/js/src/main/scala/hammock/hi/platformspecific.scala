package hammock
package hi

import java.util.{Date => JavaDate}
import scalajs.js.{Date => JsDate}

object platformspecific {
  def convert(d: JavaDate): JsDate = new JsDate(d.getTime().toDouble)

  implicit object JSDateFormatter extends DateFormatter {
    def format(date: JavaDate): String = fmt(convert(date))

    def fmt(date: JsDate): String = date.toString
  }
}
