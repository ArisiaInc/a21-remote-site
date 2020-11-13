package arisia.models

import arisia.schedule.ScheduleServiceImpl
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import better.files._

/**
 * Tests our ability to read in a standard KonOpas-formatted dump file from Zambia.
 */
class KonOpasReadSpec
  extends AnyWordSpec
  with Matchers
{
  "KonOpas data" should {
    "parse from a dump file" in {
      val svc = new ScheduleServiceImpl

      // This file is the actual dump from A'14. It's large, complex, realistic, and we believe nothing has materially
      // changed in the format since then:
      val jsonp: String = Resource.getAsString("konopastest.jsonp")

      svc.parseKonOpas(jsonp)
    }
  }
}
