package hammock


trait HttpResponse {
  def status: Status
  def headers: Map[String, String]
  def content: String

  override def toString: String =
    s"""HttpResponse(status = $status, headers = $headers, content = "$content")"""
}

object HttpResponse {
  def apply(st: Status, h: Map[String, String], c: String) = new HttpResponse {
    def status = st
    def headers = h
    def content = c
  }
}
