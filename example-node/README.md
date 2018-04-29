# A Simple Example of Nodejs using Hammock

## Start
```sh
npm i
npm run build
npm start
```

you should able to see console output `hello: Resp(world)`

## Sbt settings
```scala
lazy val exampleNode = project
  .in(file("example-node"))
  .settings(
    scalaJSModuleKind := ModuleKind.CommonJSModule, // <- enable commonjs module system
    scalaJSUseMainModuleInitializer := true,        // <- use main function `def main(args: Array[String]): Unit`
  )
  .dependsOn(coreJS, circeJS)
```
## 
