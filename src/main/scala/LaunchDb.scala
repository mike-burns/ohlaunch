package com.mike_burns.ohlaunch

import android.content.pm.PackageManager
import android.content.Intent
import android.os.Parcelable
import android.os.Parcel
import android.content.pm.ResolveInfo

import org.positronicnet.db._
import org.positronicnet.orm._

import android.util.Log

object LaunchDb extends Database("launchdb.db") {
  override def schemaUpdates =
    List("""CREATE TABLE resolve_infos
              (_id INTEGER PRIMARY KEY, 
               position INT,
               visible BOOLEAN,
               package_name VARCHAR(255),
               name VARCHAR(255),
               label VARCHAR(255),
               icon INT)""")
  override def version = 1
}

object Launcher extends RecordManager[Launcher](LaunchDb("resolve_infos")) {
  def fromResolveInfo(resolveInfo : ResolveInfo, packageManager : PackageManager) = {
    val activityInfo = resolveInfo.activityInfo
    new Launcher(packageName = activityInfo.applicationInfo.packageName,
      name = activityInfo.name,
      label = resolveInfo.loadLabel(packageManager).toString,
      icon = resolveInfo.getIconResource)
  }

  def resolveInfos = {
    this
  }

  def CREATOR = new Parcelable.Creator[Launcher] {
    def createFromParcel(in : Parcel) = {
      new Launcher(
        in.readLong,
        in.readInt,
        in.readInt == 1,
        in.readString,
        in.readString,
        in.readString,
        in.readInt)
    }

    def newArray(size : Int) = {
      new Array[Launcher](size)
    }
  }
}

case class Launcher(id: Long = ManagedRecord.unsavedId,
                    position: Int = 1,
                    visible: Boolean = true,
                    packageName: String = null,
                    name: String = null,
                    label: String = null,
                    // maybe Int will work:
                    icon: Int = 1) extends ManagedRecord(Launcher) with Parcelable {

  def loadIcon(packageManager : PackageManager) = {
    val result = packageManager.getDrawable(packageName, icon, null)

    if (result == null)
      packageManager.getDefaultActivityIcon()
    else
      result
  }

  def asLauncherIntent = {
    val i = new Intent()
    i.setClassName(packageName, name)
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    i.setAction(Intent.ACTION_MAIN)
    i.addCategory(Intent.CATEGORY_LAUNCHER)
    i
  }

  // Parcelable

  def describeContents = 0

  def writeToParcel(out : Parcel, flags : Int) {
    out.writeLong(id)
    out.writeInt(position)
    out.writeInt(if (visible) 1 else 0)
    out.writeString(packageName)
    out.writeString(name)
    out.writeString(label)
    out.writeInt(icon)
  }
}
