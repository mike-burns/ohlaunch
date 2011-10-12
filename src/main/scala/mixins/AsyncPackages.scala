package com.mike_burns.ohlaunch

import android.app.Activity
import android.content.pm.ResolveInfo
import android.content.pm.PackageManager
import android.content.Intent
import android.content.pm.ResolveInfo.DisplayNameComparator

import org.positronicnet.orm.Actions._

import java.util.Collections
import scala.collection.JavaConversions._

trait AsyncPackages extends Activity {
  def withPackages(onPostExecute : List[Launcher] => Unit) = {
    new PackageGetter(getPackageManager, onPostExecute)
  }
}

class PackageGetter(val packageManager : PackageManager, val postExecuter : List[Launcher] => Unit) {
  var preExecuter : () => Unit = {() => ()}
  def butFirst(f : () => Unit) = { this.preExecuter = f; this}

  def go {
    new SingleParamAsyncTask[Unit,Unit,List[Launcher]]  {
      override protected[ohlaunch] def doInBackground(ignored : Unit) = {
        primeDatabase
        Launcher.resolveInfos.fetchOnThisThread.toList
      }
      override protected[ohlaunch] def onProgressUpdate(ignored : Unit) = {
        null
      }
      override protected[ohlaunch] def onPostExecute(result : List[Launcher]) {
        postExecuter(result)
      }
      override protected[ohlaunch] def onPreExecute() {
        preExecuter()
      }

      private def primeDatabase {
        val mainIntent = new Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(packageManager))

        Launcher.onThisThread(DeleteAll)
        resolveInfos.toList.foreach { resolveInfo => 
          Launcher.onThisThread(Save(
            Launcher.fromResolveInfo(resolveInfo, packageManager)))
        }
      }
    }.execute(Array(1))
  }
}
