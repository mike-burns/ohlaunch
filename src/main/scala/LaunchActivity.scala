package com.mike_burns.ohlaunch

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.TextView

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.ListFragment
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.view.ViewGroup

import android.R.layout

import android.content.pm.ResolveInfo
import android.content.pm.PackageManager
import android.content.Intent
import android.widget.TableRow
import android.widget.TableLayout
import android.widget.ImageView
import android.os.AsyncTask
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener

import android.content.res.Configuration
import java.lang.ClassLoader
import android.widget.LinearLayout

import android.util.Log

import TypedResource._
import GloballyLaidOut._

class LaunchActivity extends FragmentActivity with TypedFragmentActivity with AsyncPackages {
  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)

    val pager = findView(TR.paginatorizer)
    pager.onGlobalLayout {
      withPackages(setPagerAdapter(pager)).go
    }
  }

  override def onConfigurationChanged(c : Configuration) {
    // ViewPager does not support rotations.
    super.onConfigurationChanged(c)
  }

  def setPagerAdapter(pager : ViewPager)(resolveInfos : List[ResolveInfo]) {
    pager.setAdapter(
      new ResolveInfosPagerAdapter(
        getSupportFragmentManager,
        resolveInfos,
        pager.getHeight / 88,
        pager.getWidth / 78))
  }
}

class ResolveInfosPagerAdapter(fragmentManager : FragmentManager, resolveInfos : List[ResolveInfo], numRows : Int, numCols : Int) extends FragmentPagerAdapter(fragmentManager) {
  override def getCount = {
    (this.resolveInfos.size / (this.numRows * this.numCols).asInstanceOf[Float]).ceil.asInstanceOf[Int]
  }

  override def getItem(position : Int) = {
    AppsFragment.newInstance(position, resolveInfos, this.numRows, numCols)
  }
}

object AppsFragment {
  def newInstance(page : Int, resolveInfos : List[ResolveInfo], numRows : Int, numCols : Int) = {
    (new AppsFragment()).
      setPage(page).
      setDimensions(numRows, numCols).
      setResolveInfos(resolveInfos)
  }
}

class AppsFragment extends Fragment with TypedFragment {
  var page = 0
  var resolveInfos = null : List[ResolveInfo]
  var numRows = 1
  var numCols = 1

  def setPage(p : Int) = {
    this.page = p
    this
  }

  def setResolveInfos(r : List[ResolveInfo]) = {
    this.resolveInfos = r
    this
  }

  def setDimensions(numRows : Int, numCols : Int) = {
    this.numRows = numRows
    this.numCols = numCols
    this
  }

  override def onCreateView(inflater : LayoutInflater, container : ViewGroup, savedInstanceState : Bundle) = {
    val context = getActivity
    val table = new TableLayout(context)
    val packageManager = context.getPackageManager
    var cell = inflater.inflate(R.layout.app_item, null, false)
    var tv = null : TextView
    var iv = null : ImageView
    var tr = null : TableRow
    var resolveInfo = null : ResolveInfo

    for (rowIndex <- 0 until this.numRows) {
      tr = new TableRow(context)

      for (columnIndex <- 0 until this.numCols) {
        if (positionIndex(rowIndex, columnIndex) < resolveInfos.size) {
          resolveInfo = resolveInfos(positionIndex(rowIndex, columnIndex))
          cell = inflater.inflate(R.layout.app_item, null, false)
          tv = cell.findView(TR.app_name)
          iv = cell.findView(TR.app_icon)

          tv.setText(resolveInfo.loadLabel(packageManager))
          iv.setImageDrawable(resolveInfo.loadIcon(packageManager))

          cell.setOnClickListener(new AppOpener(resolveInfo))

          tr.addView(cell)
        }
      }
      table.addView(tr)
    }

    table
  }

  def positionIndex(rowIndex : Int, columnIndex : Int) = {
    (this.page * this.numRows * this.numCols) +
      this.numCols * rowIndex + (columnIndex + 1) - 1;
  }

  class AppOpener(val resolveInfo : ResolveInfo) extends View.OnClickListener {
    def onClick(v : View) {
      val i = new Intent()

      i.setClassName(resolveInfo.activityInfo.applicationInfo.packageName,
          resolveInfo.activityInfo.name)
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      i.setAction(Intent.ACTION_MAIN)
      i.addCategory(Intent.CATEGORY_LAUNCHER)

      startActivity(i)
    }
  }

}
