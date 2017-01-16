import org.apache.http.impl.client.HttpClientBuilder

package object hammock {

  implicit val client = HttpClientBuilder.create().build()

}
