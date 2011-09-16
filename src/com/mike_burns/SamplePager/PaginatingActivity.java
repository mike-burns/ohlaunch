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

import android.util.Log;

public class PaginatingActivity extends FragmentActivity {
  private ViewPager awesomePager;
  private static int NUM_AWESOME_VIEWS = 20;
  private Context cxt;
  private AwesomePagerAdapter awesomeAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

    PackageManager packageManager = getPackageManager();
    List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);
    Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(packageManager));

    awesomeAdapter = new AwesomePagerAdapter(
        getSupportFragmentManager(),
        resolveInfos);

    setContentView(R.layout.main);
    awesomePager = (ViewPager) findViewById(R.id.paginatorizer);
    awesomePager.setAdapter(awesomeAdapter);
  }

  private class AwesomePagerAdapter extends FragmentPagerAdapter {
    List<ResolveInfo> resolveInfos;

    public AwesomePagerAdapter(FragmentManager fm, List<ResolveInfo> resolveInfos) {
      super(fm);
      this.resolveInfos = resolveInfos;
    }

    @Override
    public int getCount() {
      return NUM_AWESOME_VIEWS;
    }

    @Override
    public Fragment getItem(int position) {
      return AppsFragment.newInstance(position, resolveInfos);
    }
  }

  public static class AppsFragment extends Fragment {
    int page = 0;
    List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();

    static AppsFragment newInstance(int page, List<ResolveInfo> resolveInfos) {
      return (new AppsFragment()).
        setPage(page).
        setResolveInfos(resolveInfos);
    }

    AppsFragment setPage(int p) {
      Log.d("AppsFragment", "setting the page to "+p);
      this.page = p;
      return this;
    }

    AppsFragment setResolveInfos(List<ResolveInfo> r) {
      this.resolveInfos = r;
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
      return 4;
    }

    private int numberOfColumns() {
      return 4;
    }
  }
}
