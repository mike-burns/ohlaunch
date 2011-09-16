package com.mike_burns.SamplePager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.view.ViewGroup;

import android.R.layout;

import java.util.Collections;
import android.content.pm.ResolveInfo;
import java.util.List;
import android.content.pm.PackageManager;
import android.content.Intent;
import java.util.ArrayList;
import android.widget.TableRow;
import android.widget.TableLayout;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import android.util.Log;

public class PaginatingActivity extends FragmentActivity {
  private ViewPager awesomePager;
  private Context cxt;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);
    awesomePager = (ViewPager) findViewById(R.id.paginatorizer);

    ViewTreeObserver vto = awesomePager.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        new PackageLookUpper(awesomePager, getPackageManager()).execute();
      }});
  }

  class PackageLookUpper extends AsyncTask<Void, Void, List<ResolveInfo>> {
    private ViewPager pager;
    private PackageManager packageManager;
    private int width, height;

    PackageLookUpper(ViewPager pager, PackageManager packageManager) {
      super();
      this.pager = pager;
      this.packageManager = packageManager;
      this.height = pager.getHeight();
      this.width = pager.getWidth();
    }

    @Override
    protected List<ResolveInfo> doInBackground(Void... ignored) {
      Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
      mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

      List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);
      Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(packageManager));

      return resolveInfos;
    }

    @Override
    protected void onPostExecute(List<ResolveInfo> resolveInfos) {
      AwesomePagerAdapter awesomeAdapter = new AwesomePagerAdapter(
          getSupportFragmentManager(),
          resolveInfos,
          this.height / 88,
          this.width / 78);
      pager.setAdapter(awesomeAdapter);
    }
  }

  private class AwesomePagerAdapter extends FragmentPagerAdapter {
    List<ResolveInfo> resolveInfos;
    private int numRows = 1;
    private int numCols = 1;

    public AwesomePagerAdapter(FragmentManager fm, List<ResolveInfo> resolveInfos, int numRows, int numCols) {
      super(fm);
      this.resolveInfos = resolveInfos;
      this.numRows = numRows;
      this.numCols = numCols;
    }

    @Override
    public int getCount() {
      return (int)Math.ceil(this.resolveInfos.size() / (float)(this.numRows * this.numCols));
    }

    @Override
    public Fragment getItem(int position) {
      return AppsFragment.newInstance(position, resolveInfos, numRows, numCols);
    }
  }

  public static class AppsFragment extends Fragment {
    int page = 0;
    List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
    private int numRows = 1;
    private int numCols = 1;

    static AppsFragment newInstance(int page, List<ResolveInfo> resolveInfos, int numRows, int numCols) {
      return (new AppsFragment()).
        setPage(page).
        setDimensions(numRows, numCols).
        setResolveInfos(resolveInfos);
    }

    AppsFragment setPage(int p) {
      this.page = p;
      return this;
    }

    AppsFragment setResolveInfos(List<ResolveInfo> r) {
      this.resolveInfos = r;
      return this;
    }

    AppsFragment setDimensions(int numRows, int numCols) {
      this.numRows = numRows;
      this.numCols = numCols;
      return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      Context context = getActivity();
      PackageManager packageManager = context.getPackageManager();
      View cell = inflater.inflate(R.layout.app_item, null, false);
      TextView tv;
      ImageView iv;
      TableLayout table = new TableLayout(context);
      TableRow tr;
      ResolveInfo resolveInfo;
      int position;

      for (int rowIndex = 0; rowIndex < numberOfRows(); rowIndex++) {
        tr = new TableRow(context);

        for (int columnIndex = 0; columnIndex < numberOfColumns(); columnIndex++) {
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

    private int positionIndex(int rowIndex, int columnIndex) {
      return (this.page * numberOfRows() * numberOfColumns()) +
        numberOfColumns() * rowIndex + (columnIndex + 1) - 1;
    }

    private int numberOfRows() {
      return numRows;
    }

    private int numberOfColumns() {
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
