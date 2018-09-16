package exchange

import clients.Client
import orders.{Order, OrderBook}
import resourceManager.ResourceParseException
import stocks.Stock

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class ExchangeEngine {

  def clients: ArrayBuffer[Client]
  def start()

  var orderBooks = new mutable.HashMap[Stock.Value, OrderBook]

  def handleOrder(order: Order): Unit = {
    orderBooks.get(order.stock) match {
      case Some(orderBook) =>
        if (orderBook.addOrder(order))
          orderBook.matchOrders(order, 0)
      case None => throw ResourceParseException(s"No order book found for such stock ${order.stock.toString}")
    }
  }
}
