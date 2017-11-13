
lazy val copyLicenseFiles = taskKey[Seq[File]]("copies needed files for jar.")
lazy val makeModuleProperties = taskKey[Seq[File]]("Create module.properties file.")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(Seq(
      organization := "org.scala-sbt.ivy",
      homepage := Some(url("https://github.com/sbt/ivy")),
      description := "patched Ivy for sbt",
      licenses := List("Apache-2.0" -> url("https://github.com/sbt/ivy/blob/2.3.x-sbt/LICENSE")),
      scmInfo := Some(ScmInfo(url("https://github.com/sbt/ivy"), "git@github.com:sbt/ivy.git")),
      developers := List(
        Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
      ),
    )),
    name := "ivy",
    unmanagedSourceDirectories in Compile := Seq(
      baseDirectory.value / "src" / "java"
    ),
    unmanagedJars in Compile := Seq.empty,
    unmanagedResourceDirectories in Compile :=
      (unmanagedSourceDirectories in Compile).value,
    includeFilter in (unmanagedResources in Compile) :=
       "*.png" | "*.xml" | "*.properties" | "*.xsl" | "*.xsd" | "*.css" | "*.html" | "*.template" | "*.ent",
    excludeFilter in (unmanagedResources in Compile) :=
       "*.java",
    compileOrder := CompileOrder.JavaThenScala,
    copyLicenseFiles := {
      val dir = (resourceManaged in Compile).value
      val bd = baseDirectory.value
      val copies = 
        Map(
          (bd / "LICENSE") -> (dir / "META-INF" / "LICENSE"),
          (bd / "NOTICE") -> (dir / "META-INF" / "NOTICE")
        )
      IO.copy(copies)
      (copies map (_._2))(collection.breakOut)
    },
    makeModuleProperties := {
      val dir = (resourceManaged in Compile).value
      val file = dir / "module.properties"
        IO.write(file, s"version=${version.value}\n")
      Seq(file)
    },
    // TODO - copy ivysettings to ivyconf files for backwards compatibility.
    resourceGenerators in Compile += copyLicenseFiles.taskValue,
    resourceGenerators in Compile += makeModuleProperties.taskValue,
    libraryDependencies ++=
      Seq(
        "org.apache.ant" %"ant-nodeps" % "1.7.1" % "provided",
        "commons-httpclient" % "commons-httpclient" % "3.0" % "provided",
        "org.bouncycastle" % "bcpg-jdk14" % "1.45" % "provided",
        "com.jcraft" % "jsch.agentproxy.jsch" % "0.0.6" % "provided",
        "com.jcraft" % "jsch.agentproxy" % "0.0.6" % "provided",
        "com.jcraft" % "jsch.agentproxy.connector-factory" % "0.0.6" % "provided",
        "commons-vfs" % "commons-vfs" % "1.0" % "provided",
        "oro" % "oro" % "2.0.8" % "provided"
      ),
    autoScalaLibrary := false,
    crossPaths := false,
  )
