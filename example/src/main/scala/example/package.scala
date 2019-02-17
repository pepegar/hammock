package object example {

  import hammock._

  val baseUri = "http://httpbin.org"

  val postUri               = uri"$baseUri/post"
  val putUri                = uri"$baseUri/put"
  val deleteUri             = uri"$baseUri/delete"
  val getUri                = uri"$baseUri/get"
  val getUriWithQueryString = uri"$baseUri/get?age=4&name=name"

}
