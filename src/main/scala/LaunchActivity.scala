package com.mike_burns.ohlaunch

import _root_.android.app.ListActivity
import _root_.android.os.Bundle
import _root_.android.widget.ArrayAdapter
import _root_.android.content.pm.PackageInfo

class LaunchActivity extends PaginatedListActivity with AsyncPackages {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val adapter = new ArrayAdapter[PackageInfo](this, R.layout.activity_item)

    setListAdapter(adapter)
    withPackages { packages => packages.foreach(adapter.add(_)) }.go
  }

  override protected def contentView = {
    R.layout.paginated_activity_list
  }
}
