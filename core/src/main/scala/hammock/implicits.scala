package hammock

import org.apache.http.impl.client.HttpClientBuilder

object implicits {
  implicit val client = HttpClientBuilder.create().build()
}
