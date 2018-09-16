import java.util.Scanner

import com.typesafe.scalalogging.LazyLogging
import exchange.{ComplexExchange, Exchange, SimpleExchange}
import resourceManager.ResourceManager

object Main extends App with LazyLogging {
  val baseDir = "src/main/resources/"
  val clients = ResourceManager.parseClients(s"${baseDir}clients.txt")
  val orders = ResourceManager.parseOrders(s"${baseDir}orders.txt", clients)
  if (clients.isEmpty || orders.isEmpty) println("Input files are empty.")
  var exchangeEngine: Exchange = null
  println("Type a number:" +
    "\nAnwar Knyane - alvinaspowa@gmail.com" +
    "\n1 - Match only when both stock price and volume match (Simple Exchange)." +
    "\n2 - Check price and volume balances for validity before processing orders (Advanced Exchange)." +
    "\n3 - Information about project." +
    "\n4 - Exit")


  val settingChoice: Int = new Scanner(System.in).nextInt()

  settingChoice match {
    case 1 => logger.info(s"Simple Exchange selected"); exchangeEngine = SimpleExchange(clients)
    case 2 => logger.info(s"Advanced Exchange selected"); exchangeEngine = ComplexExchange(clients)
    case 3 => logger.info(s"Information about project"); println("SimpleScalaProject v1 - 2018/09/16 - Anwar Knyane")
    case 4 => logger.info(s"Exiting Program"); System.exit(0)
  }
  exchangeEngine.start()
  orders.foreach(o => exchangeEngine.handleOrder(o))
  ResourceManager.exportResult(baseDir + s"result.txt", clients)
}