package arisia.timer

import java.time.Instant

/**
 * This is a thin layer around the system clock, so that we can potentially replace it for unit testing later.
 */
trait TimeService {
  def now(): Instant
}

class TimeServiceImpl() extends TimeService {
  def now(): Instant = Instant.now()
}
