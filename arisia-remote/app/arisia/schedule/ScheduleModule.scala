package arisia.schedule

import com.softwaremill.macwire._

trait ScheduleModule {
  lazy val scheduleService: ScheduleService = wire[ScheduleServiceImpl]
}
