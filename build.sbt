import com.typesafe.sbt.SbtGit._

versionWithGit

// TODO - Read from version.properties
git.baseVersion := "2.3.0-sbt"

name := "ivy"

organization := "org.scala-sbt.ivy"

unmanagedSourceDirectories in Compile := Seq(
  baseDirectory.value / "src" / "java"
)

unmanagedJars in Compile := Seq.empty

unmanagedResourceDirectories in Compile :=
  (unmanagedSourceDirectories in Compile).value

includeFilter in (unmanagedResources in Compile) :=
   "*.png" | "*.xml" | "*.properties" | "*.xsl" | "*.xsd" | "*.css" | "*.html" | "*.template" | "*.ent"

excludeFilter in (unmanagedResources in Compile) :=
   "*.java"

compileOrder := CompileOrder.JavaThenScala

val copyLicenseFiles = taskKey[Seq[File]]("copies needed files for jar.")

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
}

val makeModuleProperties = taskKey[Seq[File]]("Create module.properties file.")

makeModuleProperties := {
	val dir = (resourceManaged in Compile).value
	val file = dir / "module.properties"
    IO.write(file, s"version=${version.value}\n")
	Seq(file)
}

// TODO - copy ivysettings to ivyconf files for backwards compatibility.

resourceGenerators in Compile <+= copyLicenseFiles

resourceGenerators in Compile <+= makeModuleProperties

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
  )

autoScalaLibrary := false

crossPaths := false

// TODO - publish settings
publishMavenStyle := false

publishTo := Some(Resolver.url("publish-typesafe-releases", url("http://private-repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns))
