import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "ProductMatchingWebApp"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "mysql" % "mysql-connector-java" % "5.1.19", 
      "org.mindrot" % "jbcrypt" % "0.3m"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here     
      templatesImport += "com.walmart.productgenome.pairComparison._" 
    )

}
