package com.mike_burns.ohlaunch

class WrappedAsyncTask(val f : () => Unit) {
  def backgrounded = {
    new Backgroundable(f)
  }
}

class Backgroundable(val f : () => Unit) {
  var progressUpdate : Option[Int => Unit] = None
  var postExecute : Option[() => Unit] = None
  var preExecute : Option[() => Unit] = None

  def butFirst(preExecute : () => Unit) = {
    this.preExecute = Some(preExecute)
    this
  }

  def onUpdate(progressUpdate : Int => Unit) = {
    this.progressUpdate = Some(progressUpdate)
    this
  }

  def andThen(postExecute : () => Unit) = {
    this.postExecute = Some(postExecute)
    this
  }

  def execute = {
    new SingleParamAsyncTask[Function0[Unit],Int,Void]  {
      override protected[ohlaunch] def doInBackground(f : () => Unit) : Void = {
        f()
        null
      }
      override protected[ohlaunch] def onProgressUpdate(progressCount : Int) {
        progressUpdate match {
          case Some(fn) => fn(progressCount)
          case None => identity[Unit](null)
        }
      }
      override protected[ohlaunch] def onPostExecute(result : Void) {
        postExecute match {
          case Some(fn) => fn()
          case None => identity[Unit](null)
        }
      }
      override protected[ohlaunch] def onPreExecute() {
        preExecute match {
          case Some(fn) => fn()
          case None => identity[Unit](null)
        }
      }
    }.execute(f)
  }
}

object WrappedAsyncTask {
  implicit def function2AsyncTask(f : () => Unit) : WrappedAsyncTask = new WrappedAsyncTask(f)
}
