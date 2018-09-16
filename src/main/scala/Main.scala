import java.util.Scanner

import com.typesafe.scalalogging.LazyLogging
import exchange.{ComplexExchangeEngine, ExchangeEngine, SimpleExchangeEngine}
import resourceManager.ResourceManager

object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {

    val baseDir = "src/main/resources/"
    val clients = ResourceManager.parseClients(s"${baseDir}clients.txt")
    val orders = ResourceManager.parseOrders(s"${baseDir}orders.txt", clients)
    if (clients.isEmpty || orders.isEmpty) println("Input files are empty.")
    var exchangeEngine: ExchangeEngine = null
    println("Type a number:" +
      "\nAnwar Knyane - alvinaspowa@gmail.com" +
      "\n1 - Match only when both stock price and volume match (Simple Exchange)." +
      "\n2 - Check price and volume balances for validity before processing orders (Advanced Exchange)." +
      "\n0 - Exit")


    val settingChoice = new Scanner(System.in).nextInt()
    settingChoice match {
      case 1 => logger.info(s"Simple Exchange selected"); exchangeEngine = SimpleExchangeEngine(clients)
      case 2 => logger.info(s"Advanced Exchange selected"); exchangeEngine = ComplexExchangeEngine(clients)
      case 0 => logger.info(s"Exiting Program"); System.exit(0)
    }
    exchangeEngine.start()
    orders.foreach(o => exchangeEngine.handleOrder(o))
    ResourceManager.exportResult(baseDir + s"result.txt", clients)
  }
}