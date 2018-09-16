package exchange

import clients.Client
import orders.OrderBook
import stocks.Stock

import scala.collection.mutable.ArrayBuffer

case class SimpleExchangeEngine(clients: ArrayBuffer[Client]) extends ExchangeEngine{
  def start(): Unit ={
    Stock.values.foreach(s => orderBooks += s -> new OrderBook(s, exactMatch = true, checkBalance = false))
  }
}
