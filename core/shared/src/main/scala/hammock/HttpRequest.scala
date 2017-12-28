package hammock

import monocle.macros.Lenses

@Lenses case class HttpRequest(uri: Uri, headers: Map[String, String], entity: Option[Entity])
