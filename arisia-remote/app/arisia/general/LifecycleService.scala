package arisia.general

import java.util.concurrent.atomic.AtomicReference

import arisia.util.Done
import play.api.Logging

import scala.concurrent.Future

trait LifecycleItem {
  def lifecycleService: LifecycleService
  def lifecycleName: String
  def init(): Future[Done] = { Future.successful(Done) }
  def shutdown(): Future[Done] = { Future.successful(Done) }
}

/**
 * Provides a very simplistic lifecycle abstraction, so that things can be initialized and terminated in an
 * orderly way.
 *
 * This is nowhere near a proper lifecycle mechanism (Querki's Ecology system has a much more fully-fledged version
 * of this), but it should hopefully suffice for our needs. Note that it does not provide any sort of initialization
 * order management! Init and shutdown order is basically random, so don't count on this for much.
 *
 * Services that want to use this should extend LifecycleItem, and register themselves here in their *constructor*.
 *
 * Note that it is quite possble that we should instead be using Play's own lifecycle-management system, but that is
 * *way* more complicated, so we're keeping this simple for now.
 */
trait LifecycleService {
  def register(item: LifecycleItem): Unit

  def init(): Future[Done]
  def shutdown(): Future[Done]
}

class LifecycleServiceImpl(

) extends LifecycleService with Logging {

  val _items: AtomicReference[Set[LifecycleItem]] = new AtomicReference(Set.empty)

  def register(item: LifecycleItem): Unit = {
    logger.info(s"Registering LifecycleItem ${item.lifecycleName}")
    _items.accumulateAndGet(Set(item), _ ++ _)
  }

  def init(): Future[Done] = {
    _items.get().foldLeft(Future.successful(Done)) { (_, item) =>
      logger.info(s"Initializing ${item.lifecycleName}")
      item.init()
    }
  }
  def shutdown(): Future[Done] = {
    _items.get().foldLeft(Future.successful(Done)) { (_, item) =>
      logger.info(s"Shutting down ${item.lifecycleName}")
      item.shutdown()
    }
  }
}
