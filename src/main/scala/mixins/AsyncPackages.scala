package com.mike_burns.ohlaunch

import _root_.android.app.Activity
import _root_.android.content.pm.PackageInfo
import _root_.android.content.pm.PackageManager

import scala.collection.JavaConversions._

trait AsyncPackages extends Activity {
  def withPackages(onPostExecute : List[PackageInfo] => Unit) = {
    new PackageGetter(getPackageManager, onPostExecute)
  }
}

class PackageGetter(val packageManager : PackageManager, val postExecuter : List[PackageInfo] => Unit) {
  var preExecuter : Option[() => Unit] = None
  def butFirst(f : () => Unit) = { this.preExecuter = Some(f); this}

  def go {
    new SingleParamAsyncTask[Unit,Unit,List[PackageInfo]]  {
      override protected[ohlaunch] def doInBackground(ignored : Unit) = {
        packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES).toList
      }
      override protected[ohlaunch] def onProgressUpdate(ignored : Unit) = {
        null
      }
      override protected[ohlaunch] def onPostExecute(result : List[PackageInfo]) {
        postExecuter(result)
      }
      override protected[ohlaunch] def onPreExecute() {
        preExecuter match {
          case Some(fn) => fn()
          case None     => identity[Unit](null)
        }
      }
    }.execute(Array(1))
  }
}
