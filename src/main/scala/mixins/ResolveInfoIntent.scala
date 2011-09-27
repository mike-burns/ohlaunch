package com.mike_burns.ohlaunch

import android.content.pm.ResolveInfo
import android.content.Intent

class ResolveInfoWithIntent(resolveInfo : ResolveInfo) {
  def asLauncherIntent = {
    val i = new Intent()
    i.setClassName(resolveInfo.activityInfo.applicationInfo.packageName,
      resolveInfo.activityInfo.name)
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    i.setAction(Intent.ACTION_MAIN)
    i.addCategory(Intent.CATEGORY_LAUNCHER)
    i
  }
}

object ResolveInfoWithIntent {
  implicit def resolveInfo2launcherIntent(resolveInfo : ResolveInfo) = {
    new ResolveInfoWithIntent(resolveInfo)
  }
}
