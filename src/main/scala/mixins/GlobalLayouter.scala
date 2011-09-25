package com.mike_burns.ohlaunch
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

class GlobalLayoutView(v : View) {
  def onGlobalLayout(f : => Unit) {
    v.getViewTreeObserver.addOnGlobalLayoutListener(
      new OnGlobalLayoutListener() {
        override def onGlobalLayout { f }})
  }
}

object GloballyLaidOut {
  implicit def view2globalLayout(v : View) = new GlobalLayoutView(v)
}
