// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.devkit.inspections

import com.intellij.testFramework.TestDataPath
import org.jetbrains.idea.devkit.DevkitJavaTestsUtil
import org.jetbrains.idea.devkit.inspections.quickfix.RetrievingLightServiceInspectionTestBase

@TestDataPath("\$CONTENT_ROOT/testData/inspections/retrievingLightService")
class RetrievingLightServiceInspectionTest : RetrievingLightServiceInspectionTestBase() {

  override fun getBasePath() = DevkitJavaTestsUtil.TESTDATA_PATH + "inspections/retrievingLightService/"

  override fun getFileExtension() = "java"

  fun testAppLevelServiceAsProjectLevel() {
    doTest()
  }

  fun testProjectLevelServiceAsAppLevel() {
    doTest()
  }

  fun testReplaceWithGetInstanceApplicationLevel() {
    doTest()
  }

  fun testReplaceWithGetInstanceProjectLevel() {
    doTest()
  }
}
