---
id: overview_installation
title: "Installation"
---

To use ZIO Elasticsearch in your project, add the following to your `build.sbt` file:

```scala mdoc:passthrough
println(s"""```scala""")
println(s"""resolvers += "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"""")
println()
println(s"""libraryDependencies += "${zio.elasticsearch.BuildInfo.organization}" %% "${zio.elasticsearch.BuildInfo.name}" % "${zio.elasticsearch.BuildInfo.version.replaceAll("\\+[0-9]{8}-[0-9]{4}", "")}"""")
println(s"""```""")
```
