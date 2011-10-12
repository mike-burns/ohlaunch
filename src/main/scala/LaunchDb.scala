package com.mike_burns.ohlaunch

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
}

case class Launcher(id: Long = ManagedRecord.unsavedId,
                    position: Long = 1,
                    visible: Boolean = true,
                    package_name: String = null,
                    name: String = null,
                    label: String = null,
                    icon: Long = 0) extends ManagedRecord(Launcher) {
}
