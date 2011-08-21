package com.mike_burns.ohlaunch

import android.app.Activity
import android.content.pm.ResolveInfo
import android.content.pm.PackageManager
import android.content.Intent
import android.content.pm.ResolveInfo.DisplayNameComparator

import java.util.Collections
import scala.collection.JavaConversions._

trait AsyncPackages extends Activity {
  def withPackages(onPostExecute : List[ResolveInfo] => Unit) = {
    new PackageGetter(getPackageManager, onPostExecute)
  }
}

class PackageGetter(val packageManager : PackageManager, val postExecuter : List[ResolveInfo] => Unit) {
  var preExecuter : () => Unit = {() => ()}
  def butFirst(f : () => Unit) = { this.preExecuter = f; this}

  def go {
    new SingleParamAsyncTask[Unit,Unit,List[ResolveInfo]]  {
      override protected[ohlaunch] def doInBackground(ignored : Unit) = {
        val mainIntent = new Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(packageManager))

        resolveInfos.toList
      }
      override protected[ohlaunch] def onProgressUpdate(ignored : Unit) = {
        null
      }
      override protected[ohlaunch] def onPostExecute(result : List[ResolveInfo]) {
        postExecuter(result)
      }
      override protected[ohlaunch] def onPreExecute() {
        preExecuter()
      }
    }.execute(Array(1))
  }
}
