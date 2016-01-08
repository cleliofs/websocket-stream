import java.net.URI
import javax.websocket._

import scala.collection.immutable.Seq

object HandleWebsocketMessagesClient extends App {

  val port = 8080

  (1 to 500).foreach { i =>
    println(s"Invoking stream endpoint: $i")
    val streamResult = consumeStream(port)
    streamResult.foreach(println)
  }

  private def consumeStream(port: Int): Seq[String] = {
    val wsClient = new WSClient()
    val url = s"ws://localhost:$port/stream"

    wsClient.consume(url)
    Thread.sleep(150)
    wsClient.messages
  }

  Thread.sleep(1000)
}


class WSClient extends Endpoint {

  var messages = Seq.empty[String]

  override def onOpen(s: Session, ec: EndpointConfig) = {
    s.addMessageHandler(new MessageHandler.Whole[String]() {
      override def onMessage(message: String): Unit = {
        messages = messages :+ message
      }
    })
  }

  def consume(url: String): Unit = {
    ContainerProvider.getWebSocketContainer.connectToServer(this, new URI(url))
  }

}
