package com.mike_burns.ohlaunch

import android.content.Context
import android.os.Bundle
import android.widget.TextView

import android.support.v4.view.ViewPager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast

import android.content.pm.ResolveInfo
import android.widget.TableRow
import android.widget.TableLayout
import android.widget.ImageView

import android.content.res.Configuration

import java.util.ArrayList

import android.util.Log

import scala.collection.JavaConversions._
import TypedResource._
import GloballyLaidOut._
import ResolveInfoWithIntent._
import ViewWithOnClick._

class LaunchActivity extends FragmentActivity with TypedFragmentActivity with AsyncPackages {
  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)
  }

  override def onResume {
    super.onResume

    val pager = findView(TR.paginatorizer)
    withPackages(setPagerAdapter(pager)).butFirst { () =>
      Toast.makeText(this, "Loading your apps ...", Toast.LENGTH_LONG).show
    }.go
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
    val resolveInfosArrayList = new ArrayList[ResolveInfo](resolveInfos.toIterable)

    val b = new Bundle()
    b.putInt("page", page)
    b.putInt("numRows", numRows)
    b.putInt("numCols", numCols)
    b.putParcelableArrayList("resolveInfos", resolveInfosArrayList)

    val f = new AppsFragment()
    f.setArguments(b)
    f
  }
}

class AppsFragment extends Fragment with TypedFragment {
  override def onCreateView(inflater : LayoutInflater, container : ViewGroup, savedInstanceState : Bundle) = {
    val b = getArguments

    val page = b.getInt("page", 0)
    val numRows = b.getInt("numRows", 1)
    val numCols = b.getInt("numCols", 1)
    val resolveInfosArrayList = b.getParcelableArrayList("resolveInfos")

    val tableBuilder = new TableBuilder(getActivity, inflater, numRows, numCols, page, resolveInfosArrayList.toList)
    tableBuilder.build
  }
}

class TableBuilder(context : Context, inflater : LayoutInflater, numRows : Int, numCols : Int, page : Int, resolveInfos : List[ResolveInfo]) {
  def build = {
    val table = new TableLayout(context)
    val packageManager = context.getPackageManager
    var cell = inflater.inflate(R.layout.app_item, null, false)
    var tv = null : TextView
    var iv = null : ImageView
    var tr = null : TableRow

    for (rowIndex <- 0 until this.numRows) {
      tr = new TableRow(context)

      for (columnIndex <- 0 until this.numCols) {
        val idx = positionIndex(rowIndex, columnIndex)
        if (idx < resolveInfos.size) {
          val resolveInfo = resolveInfos(idx)
          cell = inflater.inflate(R.layout.app_item, null, false)
          tv = cell.findView(TR.app_name)
          iv = cell.findView(TR.app_icon)

          tv.setText(resolveInfo.loadLabel(packageManager))
          iv.setImageDrawable(resolveInfo.loadIcon(packageManager))

          cell.onClick { view =>
            context.startActivity(resolveInfo.asLauncherIntent)
          }

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
}
