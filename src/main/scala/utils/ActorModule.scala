package utils

import akka.actor.ActorSystem


trait ActorModule {
  val system: ActorSystem
}


trait ActorModuleImpl extends ActorModule {
  val system = ActorSystem("sprayingslick")
}