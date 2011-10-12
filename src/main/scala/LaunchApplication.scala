package com.mike_burns.ohlaunch

import android.app.Application
import android.util.Log

class LaunchApplication extends Application {
  override def onCreate = {
    super.onCreate
    LaunchDb.openInContext(this)
  }
  override def onTerminate = LaunchDb.close
}
