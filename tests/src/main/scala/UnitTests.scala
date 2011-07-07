package com.mike_burns.ohlaunch.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class UnitTests extends AndroidTestCase {
  def testPackageIsCorrect {
    assertEquals("com.mike_burns.ohlaunch", getContext.getPackageName)
  }
}