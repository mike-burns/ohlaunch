package com.mike_burns.ohlaunch

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.TextView

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.view.ViewGroup

import android.R.layout

import java.util.Collections
import android.content.pm.ResolveInfo
import java.util.List
import android.content.pm.PackageManager
import android.content.Intent
import java.util.ArrayList
import android.widget.TableRow
import android.widget.TableLayout
import android.widget.ImageView
import android.os.AsyncTask
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener

import android.util.Log

class PaginatingActivity extends FragmentActivity {
  val awesomePager : ViewPager = null

  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)
    awesomePager = findViewById(R.id.paginatorizer).asInstanceOf[ViewPager]

    vto = awesomePager.getViewTreeObserver()
    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
      override def onGlobalLayout {
        new PackageLookUpper(awesomePager, getPackageManager()).execute(1)
      }})
  }

  class PackageLookUpper extends AsyncTask[Void, Void, List[ResolveInfo]] {
    val pager : ViewPager = null
    val packageManager : PackageManager = null
    val width = 1
    val height = 1

    def this(pager : ViewPager, packageManager : packageManager) = {
      super()
      this.pager = pager
      this.packageManager = packageManager
      this.height = pager.getHeight
      this.width = pager.getWidth
    }

    override def doInBackground(ignored : Void...) : List[ResolveInfo] = {
      val mainIntent = new Intent(Intent.ACTION_MAIN, null)
      mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

      val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
      Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(packageManager))

      resolveInfos
    }

    override def onPostExecute(resolveInfos : List[ResolveInfo]) {
      val awesomeAdapter = new AwesomePagerAdapter(
          getSupportFragmentManager,
          resolveInfos,
          this.height / 88,
          this.width / 78);
      pager.setAdapter(awesomeAdapter)
    }
  }

  class AwesomePagerAdapter extends FragmentPagerAdapter {
    val resolveInfos : List[ResolveInfo] = null
    val numRows = 1
    val numCols = 1

    this(fm : FragmentManager, resolveInfos : List[ResolveInfo], numRows : Int, numCols : Int) = {
      super(fm)
      this.resolveInfos = resolveInfos
      this.numRows = numRows
      this.numCols = numCols
    }

    override getCount = {
      Math.ceil(this.resolveInfos.size / (this.numRows * this.numCols).asInstanceOf[Float]).asInstanceOf[Int]
    }

    override def getItem(position : Int) = {
      AppsFragment.newInstance(position, resolveInfos, numRows, numCols)
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


  class AppsFragment extends Fragment {
    var page = 0
    var resolveInfos = new ArrayList[ResolveInfo]()
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
      val context = getActivity()
      val packageManager = context.getPackageManager()
      val cell = inflater.inflate(R.layout.app_item, null, false)
      var tv
      var iv
      val table = new TableLayout(context)
      var tr
      var resolveInfo
      var position

      for (Int rowIndex = 0; rowIndex < numberOfRows(); rowIndex++) {
        tr = new TableRow(context);

        for (Int columnIndex = 0; columnIndex < numberOfColumns(); columnIndex++) {
          if (positionIndex(rowIndex, columnIndex) < resolveInfos.size()) {
            resolveInfo = resolveInfos.get(positionIndex(rowIndex, columnIndex));
            cell = inflater.inflate(R.layout.app_item, null, false);
            tv = (TextView)cell.findViewById(R.id.app_name);
            iv = (ImageView)cell.findViewById(R.id.app_icon);

            tv.setText(resolveInfo.loadLabel(packageManager));
            iv.setImageDrawable(resolveInfo.loadIcon(packageManager));

            cell.setOnClickListener(new AppOpener(resolveInfo));

            tr.addView(cell);
          }
        }
        table.addView(tr);
      }

      return table;
    }

    private Int positionIndex(Int rowIndex, Int columnIndex) {
      return (this.page * numberOfRows() * numberOfColumns()) +
        numberOfColumns() * rowIndex + (columnIndex + 1) - 1;
    }

    private Int numberOfRows() {
      return numRows;
    }

    private Int numberOfColumns() {
      return numCols;
    }

    class AppOpener implements View.OnClickListener {
      private ResolveInfo resolveInfo;

      AppOpener(ResolveInfo resolveInfo) {
        this.resolveInfo = resolveInfo;
      }

      public void onClick(View v) {
        Intent i = new Intent();

        i.setClassName(resolveInfo.activityInfo.applicationInfo.packageName,
            resolveInfo.activityInfo.name);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        startActivity(i);
      }
    }

  }
}
