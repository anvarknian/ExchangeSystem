package orders

import clients.Client
import org.scalactic.source.Position
import org.scalatest._
import stocks.Stock

import scala.collection.mutable

class OrderBookTest extends FunSuite with BeforeAndAfterEach {

  var orderBook: OrderBook = _
  var client1: Client = _
  var client2: Client = _

  override def beforeEach(): Unit = {
    orderBook = new OrderBook(Stock.A, true, true)
    client1 = new Client("C1", 1000, mutable.HashMap(Stock.A -> 3, Stock.B -> 4, Stock.C -> 5, Stock.D -> 6))
    client2 = new Client("C2", 500, mutable.HashMap(Stock.A -> 0, Stock.B -> 4, Stock.C -> 1, Stock.D -> 6))
  }

  override def afterEach(): Unit = {
    orderBook = null
    client1 = null
    client2 = null
  }

  test("testAddOrder: order is correctly added") {
    orderBook.addOrder(BuyOrder(client1, Stock.A, 100, 5))
    assertResult(orderBook.buyOrders.length)(1)
  }

  test("testAddOrder: don't accept deficit orders since checkBalance is true") {
    // client1 does not have up to 10 Stock A so his sell order should not be added
    orderBook.addOrder(SellOrder(client1, Stock.A, 100, 10))
    assertResult(orderBook.buyOrders.length)(0)
  }

  test("testMatchOrders: match is done") {
    val sellOrder = SellOrder(client1, Stock.A, 100, 2)
    val buyOrder = BuyOrder(client2, Stock.A, 100, 2)
    orderBook.addOrder(sellOrder)
    orderBook.addOrder(buyOrder)
    orderBook.matchOrders(buyOrder, 0)
    assert(
        client1.usdBalance == (1000 + 200) &&
        client1.stocks(Stock.A) == (3 - 2) &&
        client2.usdBalance == (500 - 200) &&
        client2.stocks(Stock.A) == (0 + 2)
    )
  }

  test("testMatchOrders: self match is not allowed") {
    val sellOrder = SellOrder(client1, Stock.A, 100, 2)
    val buyOrder = BuyOrder(client1, Stock.A, 100, 2)
    orderBook.addOrder(sellOrder)
    orderBook.addOrder(buyOrder)
    orderBook.matchOrders(buyOrder, 0)
    assert(client1.usdBalance == 1000 && client1.stocks(Stock.A) == 3)
  }

}
