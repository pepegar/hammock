# 0.9.0

- bump a lot of dependencies (thanks to @scala-steward)

	update sbt-tpolecat-0.1.4 [#138](https://github.com/pepegar/hammock/pull/138)
	update simulacrum-0.14.0 [#133](https://github.com/pepegar/hammock/pull/133)
	update sbt-crossproject-0.6.0 [#141](https://github.com/pepegar/hammock/pull/141)
	update circe-core-0.11.1 [#159](https://github.com/pepegar/hammock/pull/159)
	update circe-core-0.11.0 [#154](https://github.com/pepegar/hammock/pull/154)
	update httpclient-4.5.6 [#139](https://github.com/pepegar/hammock/pull/139)
	update akka-http-10.0.15 [#135](https://github.com/pepegar/hammock/pull/135)
	update circe-core-0.10.1 [#136](https://github.com/pepegar/hammock/pull/136)
	update scala-java-time-2.0.0-RC1 [#137](https://github.com/pepegar/hammock/pull/137)
	update mockito-all-1.10.19 [#140](https://github.com/pepegar/hammock/pull/140)
	update scalajs-dom-0.9.6 [#143](https://github.com/pepegar/hammock/pull/143)
	update scalacheck-1.14.0 [#144](https://github.com/pepegar/hammock/pull/144)
	update kind-projector-0.9.9 [#145](https://github.com/pepegar/hammock/pull/145)
	update discipline-0.10.0 [#149](https://github.com/pepegar/hammock/pull/149)
	update tut-plugin-0.6.10 [#146](https://github.com/pepegar/hammock/pull/146)
	update alleycats-core-1.5.0 [#147](https://github.com/pepegar/hammock/pull/147)

- split hammock-core [#156](https://github.com/pepegar/hammock/pull/156) thanks to @ipsq
- add automatic releasing from travis with sbt-ci-release
- Support fetch api from browser, not just node [#127](https://github.com/pepegar/hammock/pull/127) thanks to @triggerNZ


# 0.8.5

- Enable posfix operators for docs [#109](https://github.com/pepegar/hammock/pull/109)
- Add support for PATCH method [#106](https://github.com/pepegar/hammock/pull/106)
- Replace Codec requirement for requests that include a body by an Encode requirement [#107](https://github.com/pepegar/hammock/pull/107)

# 0.8.4

- add interpreter for NodeJS! [#105](https://github.com/pepegar/hammock/pull/105)

# 0.8.3

- update dependencies in [#103](https://github.com/pepegar/hammock/pull/103)
- implementing query parameters building methods [#99](https://github.com/pepegar/hammock/pull/99)
- fix nullpointer in jvm.Interpreter [#102](https://github.com/pepegar/hammock/pull/102)
- make default charset utf-8 for `application/json` content type [#94](https://github.com/pepegar/hammock/pull/94)

# 0.8.2

- Make ExampleJS project compile and link correctly [#92](https://github.com/pepegar/hammock/pull/92)
- Fix a `MatchError` bug in the JS interpreter. We introduced it in [#69](https://github.com/pepegar/hammock/pull/92). [#92](https://github.com/pepegar/hammock/pull/92)

# 0.8.1

- Bump version of cats-effect to 0.8 [#88](https://github.com/pepegar/hammock/pull/88)
- Bump version of httpclient to 4.5.4 [#88](https://github.com/pepegar/hammock/pull/88)

# 0.8.0

- Created a changelog [#86](https://github.com/pepegar/hammock/pull/86)
- Update to cats `1.0.0` [#80](https://github.com/pepegar/hammock/pull/80)
- Asynchttpclient interpreter. [#73](https://github.com/pepegar/hammock/pull/73)
- New Entity datatype for representing Http[Request|Response] entites. [#69](https://github.com/pepegar/hammock/pull/69)
- Create a new ByteArray entity [#72](https://github.com/pepegar/hammock/pull/72)
- Refactoring in HttpRequestF. [#69](https://github.com/pepegar/hammock/pull/69)
- Simplification of imports.[#69](https://github.com/pepegar/hammock/pull/69)
- New Marshalling algebra.[#74](https://github.com/pepegar/hammock/pull/74)
- Use stricter `scalacOptions`. [#79](https://github.com/pepegar/hammock/pull/79)
- Restructure docs. [#76](https://github.com/pepegar/hammock/pull/76)
