package org.jetbrains.plugins.scala.testingSupport.scalatest

trait ScalaTestGoToSourceTest extends ScalaTestTestCase {

  val goToSourceClassName = "GoToSourceTest"
  val goToSourceTemplateClassName = "GoToSourceTestTemplate"

  addSourceFile(goToSourceClassName + ".scala",
    s"""import org.scalatest._
       |
       |class $goToSourceClassName extends $goToSourceTemplateClassName {
       | "Successful test" should "run fine" in {
       | }
       |
       | "pending test" should "be pending" in {
       |   pending
       | }
       |
       | ignore should "be ignored" in {
       | }
       |
       | "failed test" should "fail" in {
       |   fail
       | }
       |}
       |""".stripMargin
  )

  addSourceFile(goToSourceTemplateClassName + ".scala",
    s"""import org.scalatest._
       |
       |trait $goToSourceTemplateClassName extends FlatSpec with GivenWhenThen {
       |  "Successful in template" should "run fine" in {
       |  }
       |}
       |""".stripMargin
  )


  def getSuccessfulTestPath: List[String]

  def getPendingTestPath: List[String]

  def getIgnoredTestPath: List[String]

  def getFailedTestPath: List[String]

  def getTemplateTestPath: List[String]

  def getSuccessfulLocationLine: Int

  def getPendingLocationLine: Int

  def getIgnoredLocationLine: Int

  def getFailedLocationLine: Int

  def getTemplateLocationLine: Int

  def testGoToSuccessfulLocation(): Unit = {
    runGoToSourceTest(3, 5, goToSourceClassName + ".scala",
      assertConfigAndSettings(_, goToSourceClassName, "Successful test should run fine"),
      getSuccessfulTestPath, getSuccessfulLocationLine)
  }

  def testGoToPendingLocation(): Unit = {
    runGoToSourceTest(6, 5, goToSourceClassName + ".scala",
      assertConfigAndSettings(_, goToSourceClassName, "pending test should be pending"),
      getPendingTestPath, getPendingLocationLine)
  }

  def testGoToIgnoredLocation(): Unit = {
    //since finders API ignored ignored tests and provides neighbours for the same scope instead of ignored test poitned to
    //we run all the tests
    runGoToSourceTest(2, 5, goToSourceClassName + ".scala",
      assertConfigAndSettings(_, goToSourceClassName),
      //notice that runConfig test name and testTree test name differ by !!! IGNORED !!! suffix
      getIgnoredTestPath, getIgnoredLocationLine)
  }

  def testGoToFailedTest(): Unit = {
    runGoToSourceTest(13, 5, goToSourceClassName + ".scala",
      assertConfigAndSettings(_, goToSourceClassName, "failed test should fail"),
      getFailedTestPath, getFailedLocationLine)
  }

  def testGoToTemplateTest(): Unit = {
    runGoToSourceTest(2, 5, goToSourceClassName + ".scala",
      assertConfigAndSettings(_, goToSourceClassName),
      getTemplateTestPath, getTemplateLocationLine, Some(goToSourceTemplateClassName + ".scala")
    )
  }
}
