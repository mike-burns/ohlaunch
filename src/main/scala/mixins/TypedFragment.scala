package com.mike_burns.ohlaunch
import android.support.v4.app.FragmentActivity
import android.support.v4.app.Fragment

trait TypedFragmentActivity extends FragmentActivity with TypedActivityHolder {
  def activity = this
}

trait TypedFragment extends Fragment with TypedActivityHolder {
  def activity = getActivity
}
