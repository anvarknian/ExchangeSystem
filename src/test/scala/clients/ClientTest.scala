package clients

import orders.{BuyOrder, SellOrder}
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import stocks.Stock

import scala.collection.mutable

class ClientTest extends FunSuite with BeforeAndAfterEach {

  var client: Client = _
  var otherClient: Client = _

  override def beforeEach() {
    client = new Client("C1", 1000, mutable.HashMap(Stock.A -> 3, Stock.B -> 4, Stock.C -> 5, Stock.D -> 6))
    otherClient = new Client("C1", 1000, mutable.HashMap(Stock.A -> 3, Stock.B -> 4, Stock.C -> 5, Stock.D -> 6))
  }

  override def afterEach() {
    client = null
    otherClient = null
  }

  test("testBuy: functions properly") {
    val buyOrder = BuyOrder(client, Stock.A, 100, 2)
    client.buy(buyOrder, buyOrder.price)
    assert(client.usdBalance == (1000 - 200) && client.stocks(Stock.A) == (3+2))
  }

  test("testSell: transaction for another client should fail") {
    val sellOrder = SellOrder(otherClient, Stock.A, 100, 2)
    assertThrows[AssertionError] {
      client.sell(sellOrder, sellOrder.price)
    }
  }

}
