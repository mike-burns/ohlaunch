package com.mike_burns.ohlaunch;

import android.os.AsyncTask;

public abstract class SingleParamAsyncTask<T1,T2,T3> extends AsyncTask<T1,T2,T3>{
  protected T3 doInBackground(T1 ...f) {
    return doInBackground(f[0]);
  }
  abstract protected T3 doInBackground(T1 f);

  protected void onProgressUpdate(T2 ...f) {
    onProgressUpdate(f[0]);
  }
  abstract protected void onProgressUpdate(T2 f);
}
