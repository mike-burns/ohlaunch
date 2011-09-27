package com.mike_burns.ohlaunch

import android.view.View
import android.view.View.OnClickListener

class ViewWithOnClick(view : View) {
  def onClick(f : View => Unit) {
    view.setOnClickListener(new View.OnClickListener {
        def onClick(v : View) { f(v) } })
  }
}

object ViewWithOnClick {
  implicit def view2onClick(view : View) = new ViewWithOnClick(view)
}
