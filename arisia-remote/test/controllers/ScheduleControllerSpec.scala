package controllers

import arisia.schedule.ScheduleServiceImpl
import better.files.Resource
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}
import play.api.libs.json.Json

class ScheduleControllerSpec
  extends AsyncWordSpec
  with Matchers
{
  "ScheduleController" should {
    "be able to write the schedule correctly formatted" in {
      // TODO: this is fake right now -- we're copying the code instead of calling it. Set up a proper
      // test infrastructure so that we can get the ScheduleController from DI, and call that to get
      // the real output, and check that.
      val scheduleService = new ScheduleServiceImpl
      scheduleService.currentSchedule().map { fullSchedule =>
        val schedule = fullSchedule.copy(
          program = fullSchedule.program.take(1),
          people = fullSchedule.people.take(1)
        )
        val json = Json.toJson(schedule)
        println(json)

        succeed
      }
    }
  }
}
