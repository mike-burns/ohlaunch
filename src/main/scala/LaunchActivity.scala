package com.mike_burns.ohlaunch

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.content.pm.PackageInfo

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.Toast
import android.view.View
import android.view.View.OnTouchListener
import android.view.MotionEvent

import android.util.Log

import android.widget.TextView
import android.widget.ImageView
import android.view.ViewGroup
import android.content.Context

class LaunchActivity extends Activity with AsyncPackages with TypedActivity {
  var allPackages = List[PackageInfo]()
  var offset = 0
  var adapter = null : ArrayAdapter[PackageInfo]
  val perPage = 5

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.paginated_activity_list)

    adapter = new PackageAdapter(this)
    val list = findView(TR.list)
    list.setAdapter(adapter)

    val gestureDetector = new GestureDetector(
      new LeftRightDetector().
        onLeft({ () => pageLeft}).
        onRight({ () => pageRight})
    )
    list.setOnTouchListener(new View.OnTouchListener {
      def onTouch(v : View, event : MotionEvent) = {
        gestureDetector.onTouchEvent(event) } })

    withPackages { packages =>
      allPackages = packages
      packages.take(perPage).foreach(adapter.add(_))
    }.go
  }

  private def pageLeft {
    offset = List(offset + perPage, allPackages.length-1).min
    adapter.clear
    allPackages.slice(offset, offset+perPage).foreach(adapter.add(_))
  }

  private def pageRight {
    offset = List(offset - perPage, 0).max
    adapter.clear
    allPackages.slice(offset, offset+perPage).foreach(adapter.add(_))
  }
    // package.applicationInfo.icon - or loadIcon(PackageManager)
    // package.applicationInfo.name - or loadLabel(PackageManager)

  class PackageAdapter(activity : Activity) extends ArrayAdapter[PackageInfo](activity.asInstanceOf[Context], R.layout.activity_item, R.id.app_name) {
    override def getView(position : Int, convertView : View, parent : ViewGroup) = {
      val inflater = activity.getLayoutInflater
      val packageManager = activity.getPackageManager
      val cell = inflater.inflate(R.layout.activity_item, parent, false)


      val item = getItem(position).asInstanceOf[PackageInfo]
      val iconView = cell.findViewById(R.id.app_icon).asInstanceOf[ImageView]
      iconView.setImageDrawable(item.applicationInfo.loadIcon(packageManager))
      val nameView = cell.findViewById(R.id.app_name).asInstanceOf[TextView]
      nameView.setText(item.applicationInfo.loadLabel(packageManager))

      cell
    }
  }

  class LeftRightDetector extends SimpleOnGestureListener {
    var leftCallback = { () => () }
    var rightCallback = { () => () }

    override def onFling(event1 : MotionEvent, event2 : MotionEvent, xVelocity : Float, yVelocity : Float) = {
      Log.d("onFling", "inside onFling")
      if (leftFling(event1, event2, xVelocity, yVelocity)) {
      Log.d("onFling", "left fling")
        leftCallback()
        false
      } else if (rightFling(event1, event2, xVelocity, yVelocity)) {
      Log.d("onFling", "right fling")
        rightCallback()
        false
      } else {
      Log.d("onFling", "no fling")
        true
      }
    }

    def onLeft(f : () => Unit) = {
      Log.d("onLeft", "about to set leftCallback")
      leftCallback = f
      this
    }

    def onRight(f : () => Unit) = {
      Log.d("onRight", "about to set rightCallback")
      rightCallback = f
      this
    }
    
    override def onDown(ignored : MotionEvent) = { true }

    private def leftFling(event1 : MotionEvent, event2 : MotionEvent, xVelocity : Float, yVelocity : Float) = {
      event1.getX() - event2.getX() > 120 && Math.abs(xVelocity) > 200
    }

    private def rightFling(event1 : MotionEvent, event2 : MotionEvent, xVelocity : Float, yVelocity : Float) = {
      event2.getX() - event1.getX() > 120 && Math.abs(xVelocity) > 200
    }
  }
}
