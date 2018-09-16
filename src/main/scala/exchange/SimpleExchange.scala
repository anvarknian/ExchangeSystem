package exchange

import clients.Client
import orders.OrderBook
import stocks.Stock

import scala.collection.mutable.ArrayBuffer

case class SimpleExchange(clients: ArrayBuffer[Client]) extends Exchange {
  def start(): Unit ={
    Stock.values.foreach(s => orderBooks += s -> new OrderBook(s, exactMatch = true, checkBalance = false))
  }
}
