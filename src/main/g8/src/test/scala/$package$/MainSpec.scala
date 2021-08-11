package $package$

import zio.test.Assertion.equalTo
import zio.test.junit.JUnitRunnableSpec
import zio.test.{ assert, suite, test }

object MainSpec extends JUnitRunnableSpec:

  def spec = suite("Test environment")(
    test("expect call with input satisfying assertion") {
      assert(40 + 2)(equalTo(42))
    }
  )
