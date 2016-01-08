import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.scaladsl._
import akka.stream.stage._
import akka.stream.{Supervision, ActorMaterializer, FlowShape}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}

import scala.concurrent.ExecutionContextExecutor

object HandleWebsocketMessagesServer extends App {

  self =>

  val port = 8080
  val config = ConfigFactory.empty().withValue("http.listen.port", ConfigValueFactory.fromAnyRef(port))

  implicit val system = ActorSystem("HandleWebsocketMessagesServer", config)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val restEndpoints = new RestEndpoints() {
    override implicit val materializer = self.materializer
    override implicit val system: ActorSystem = self.system
    override implicit def executor: ExecutionContextExecutor = system.dispatcher
  }

  Http().bindAndHandleAsync(Route.asyncHandler(restEndpoints.routes), "0.0.0.0", config.getInt("http.listen.port"))
  println("Server started, listening on: " + port)
}



trait RestEndpoints extends WsEventGraphFlowFactory {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  val routes: Route =
    path("stream") {
      get {
        println("Got request for streaming")
        handleWebsocketMessages(graphFlow())
      }
    }
}

trait WsEventGraphFlowFactory {

  def graphFlow(): Flow[Message, Message, Unit] = {

    import GraphDSL.Implicits._
//      import FlowGraph.Implicits._

    Flow.fromGraph(GraphDSL.create() { implicit b =>
//    Flow() { implicit b =>

        // Graph elements we'll use
        val merge = b.add(Merge[Int](2))

        // convert to int so we can connect to merge
        val mapMsgToInt = b.add(Flow[Message].map[Int] { msg => -1 })
        val mapIntToMsg = b.add(Flow[Int].map[Message]( x => TextMessage.Strict(s"Sending: $x")))

        // source we want to use to send message to the connected websocket sink
        val rangeSource = b.add(Source(1 to 100))

        // connect the graph
        mapMsgToInt ~> merge // this part of the merge will never provide msgs
        rangeSource ~> merge ~> mapIntToMsg

        // expose ports
        FlowShape(mapMsgToInt.in, mapIntToMsg.out)
//        (mapMsgToInt.inlet, mapIntToMsg.outlet)
      }
    )
    .via(completeFlow)
  }

  def completeFlow[T]: Flow[T, T, Unit] =
    Flow[T].transform(() ⇒ new PushStage[T, T] {
      def onPush(elem: T, ctx: Context[T]): SyncDirective = {
        println(s"onPush: $elem")
        ctx.push(elem)
      }

      override def onDownstreamFinish(ctx: Context[T]): TerminationDirective = {
        println("onDownstreamFinish")
        super.onDownstreamFinish(ctx)
      }

      @throws[Exception](classOf[Exception])
      override def preStart(ctx: LifecycleContext): Unit = {
        println("preStart")
        super.preStart(ctx)
      }

      override def onUpstreamFinish(ctx: Context[T]): TerminationDirective = {
        println("onUpstreamFinish")
        super.onUpstreamFinish(ctx)
      }

      override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
        println("onUpstreamFailure")
        super.onUpstreamFailure(cause, ctx)
      }

      @throws[Exception](classOf[Exception])
      override def postStop(): Unit = {
        println("postStop")
        super.postStop()
      }

      override def decide(t: Throwable): Supervision.Directive = {
        println("decide")
        super.decide(t)
      }

      override def restart(): Stage[T, T] = {
        println("restart")
        super.restart()
      }
    })

}


