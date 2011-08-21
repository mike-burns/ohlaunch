package com.mike_burns.ohlaunch

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.content.pm.ResolveInfo

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

import android.util.DisplayMetrics

import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.content.ComponentName

class LaunchActivity extends Activity with AsyncPackages with TypedActivity {
  var allPackages = List[ResolveInfo]()
  var offset = 0
  var adapter = null : ArrayAdapter[ResolveInfo]

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.paginated_activity_list)

    adapter = new PackageAdapter(this)
    val list = findView(TR.list)
    list.setAdapter(adapter)

    val gestureDetector = new GestureDetector(
      new LeftRightDetector().
        onLeft({ () => flingLeft}).
        onRight({ () => flingRight})
    )
    list.setOnTouchListener(new View.OnTouchListener {
      def onTouch(v : View, event : MotionEvent) = {
        gestureDetector.onTouchEvent(event)
        false
      }})

    list.setOnItemClickListener(new OnItemClickListener {
      def onItemClick(parent : AdapterView[_], v : View, position : Int, id : Long) {
        val item = parent.getItemAtPosition(position).asInstanceOf[ResolveInfo]
  
        val intent = getPackageManager.getLaunchIntentForPackage(
          item.activityInfo.packageName)

        startActivity(intent)
      }})

    withPackages { packages =>
      allPackages = packages
      packages.take(perPage).foreach(adapter.add(_))
    }.go
  }

  private def perPage = {
    val heightPixels = findView(TR.list).getHeight

    val perRow = 4
    val cellContentHeight = 88
    val padding = 10
    val paddingTop = 3
    val fudge = 10

    val totalCellHeight = cellContentHeight + padding + paddingTop// + fudge

    (heightPixels / totalCellHeight) * perRow
  }

  private def flingLeft {
    offset = List(offset + perPage, lastPageOffset).min
    adapter.clear
    allPackages.slice(offset, offset+perPage).foreach(adapter.add(_))
  }

  private def flingRight {
    offset = List(offset - perPage, 0).max
    adapter.clear
    allPackages.slice(offset, offset+perPage).foreach(adapter.add(_))
  }

  private def pageCount = {
    val count = allPackages.length / perPage
    if (allPackages.length % perPage > 0)
      count + 1
    else
      count
  }

  private def lastPageOffset = {
    (perPage * (allPackages.length.asInstanceOf[Float] / perPage).ceil - perPage).asInstanceOf[Int]
  }

  class PackageAdapter(activity : Activity) extends ArrayAdapter[ResolveInfo](activity.asInstanceOf[Context], R.layout.activity_item, R.id.app_name) {
    override def getView(position : Int, convertView : View, parent : ViewGroup) = {
      val inflater = activity.getLayoutInflater
      val packageManager = activity.getPackageManager
      val cell = inflater.inflate(R.layout.activity_item, parent, false)


      val item = getItem(position).asInstanceOf[ResolveInfo]
      val iconView = cell.findViewById(R.id.app_icon).asInstanceOf[ImageView]
      iconView.setImageDrawable(item.loadIcon(packageManager))
      val nameView = cell.findViewById(R.id.app_name).asInstanceOf[TextView]
      nameView.setText(item.loadLabel(packageManager))

      cell
    }
  }

  class LeftRightDetector extends SimpleOnGestureListener {
    var leftCallback = { () => () }
    var rightCallback = { () => () }

    override def onFling(event1 : MotionEvent, event2 : MotionEvent, xVelocity : Float, yVelocity : Float) = {
      if (leftFling(event1, event2, xVelocity, yVelocity)) {
        leftCallback()
        false
      } else if (rightFling(event1, event2, xVelocity, yVelocity)) {
        rightCallback()
        false
      } else {
        true
      }
    }

    def onLeft(f : () => Unit) = {
      leftCallback = f
      this
    }

    def onRight(f : () => Unit) = {
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
