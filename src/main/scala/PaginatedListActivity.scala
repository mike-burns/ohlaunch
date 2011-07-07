package com.mike_burns.ohlaunch

import _root_.android.app.ListActivity
import _root_.android.widget.BaseAdapter
import _root_.android.widget.ListAdapter
import _root_.android.os.Bundle

import _root_.android.database.DataSetObserver
import _root_.android.view.View
import _root_.android.view.ViewGroup

class PaginatedListActivity extends AbsListActivity {
  private var pageNumber = 0 // TODO: methods to change this

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(contentView)
    // TODO: hook up next, prior page buttons
  }

  def limit = { 5 } // TODO: compute this

  override def setListAdapter(adapter : ListAdapter) {
    super.setListAdapter(new PaginatedAdapter(adapter, pageNumber * limit, limit))
  }

  protected def contentView = { R.layout.paginated_content_list }

  class PaginatedAdapter(val adapter : ListAdapter, offset : Int, limit : Int) extends ListAdapter {
    def registerDataSetObserver(observer : DataSetObserver) {
      adapter.registerDataSetObserver(observer) }
    def unregisterDataSetObserver(observer : DataSetObserver) {
      adapter.unregisterDataSetObserver(observer) }
    def getCount = { List(adapter.getCount, limit).min }
    def getItem(position : Int) = { adapter.getItem(offset + position) }
    def getItemId(position : Int) = { adapter.getItemId(offset + position) }
    def hasStableIds = { adapter.hasStableIds }
    def getView(position : Int, convertView : View, parent : ViewGroup) = {
      adapter.getView(offset + position, convertView, parent) }
    def getItemViewType(position : Int) = { adapter.getItemViewType(offset + position) }
    def getViewTypeCount = { adapter.getViewTypeCount }
    def isEmpty = { adapter.isEmpty }


    def areAllItemsEnabled = { adapter.areAllItemsEnabled }
    def isEnabled(position : Int) = { adapter.isEnabled(offset + position) }
  }
}
