package hammock

trait Status {
  def code: Int
  def text: String
}

object Status {
  val NotFound = status(404, "Not Found")
  val Ok = status(200, "OK")
  val InternalServerError = status(500, "Internal server error")

  def status(c: Int, t: String) = new Status {
    def code = c
    def text = t
  }
}
