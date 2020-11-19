package hammock
package hi

import java.time.ZonedDateTime

trait DateFormatter {
  def format(date: ZonedDateTime): String
}
