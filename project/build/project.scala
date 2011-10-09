import sbt._

trait Defaults {
  def androidPlatformName = "android-4"
}
class Parent(info: ProjectInfo) extends ParentProject(info) {
  override def shouldCheckOutputDirectories = false
  override def updateAction = task { None }

  lazy val main  = project(".", "Oh! Launch!", new MainProject(_))

  class MainProject(info: ProjectInfo) extends AndroidProject(info) with Defaults with MarketPublish with TypedResources {
    val keyalias  = "ohlaunch"
    val scalatest = "org.scalatest" % "scalatest" % "1.3" % "test"

    // https://github.com/jberkel/android-plugin/issues/24 :
    val toKeep = List(
      "scala.Function1",
      "org.xml.sax.EntityResolver",
      "scala.runtime.BoxedUnit",
      "scala.runtime.ObjectRef",
      "android.support.v4.**",
      "scala.collection.immutable.List",
      "scala.Function0"
    )

    val keepOptions = toKeep.map { "-keep public class " + _ }

    val dontNote = List(
      "scala.Enumeration"
    )
    val dontNoteOptions = dontNote.map { "-dontnote " + _ }

    override def proguardOption = {
      (super.proguardOption ++ keepOptions ++ dontNoteOptions) mkString " "
    }
  }
}
