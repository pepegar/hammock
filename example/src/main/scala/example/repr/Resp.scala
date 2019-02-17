package example.repr

case class Resp(data: String)

case class GetResp(url: String, origin: String)
case class GetRespArg(name: String, age: Int)
case class GetRespWithQueryString(url: String, origin: String, args: GetRespArg)
