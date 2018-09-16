package resourceManager
import clients.Client
import org.scalatest._
import stocks.Stock

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ResourceManagerTest extends FunSuite with Matchers {

  val baseDir = "src/test/resources/"


  test("testParseClients: correct parsing") {
    var clients: ArrayBuffer[Client] = ResourceManager.parseClients(baseDir + "test-clients.txt")
    // There are 9 clients in the resource file.
    assert(clients.length == 9)

  }

  test("testParseClients: Incorrect filename, throw an exception") {
      // Incorrect filename(without ".txt").
    assertThrows[ResourceParseException] {
      ResourceManager.parseClients(baseDir + "test-clients")
    }
  }

  test("testParseOrders: throw exception") {
    var clients = ArrayBuffer(
      new Client("C1", 1000, mutable.HashMap(Stock.A -> 3, Stock.B -> 4, Stock.C -> 5, Stock.D -> 6))
    )
    // The test orders contain clients that are not in the defined clients. Should throw an exception
    assertThrows[ResourceParseException]{
      ResourceManager.parseOrders(baseDir + "test-orders.txt", clients)
    }
  }

}
