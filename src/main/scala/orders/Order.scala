package orders

import stocks.Stock
import clients.Client

object Order {

  var counter: Int = -1
  def increment: Int = {
    counter += 1
    counter
  }
}

trait Order {

  def client: Client
  def stock: Stock.Value
  def price: Int
  def volume: Int
  def serialNumber: Int
  def split(newVolume: Int): Order
}

case class BuyOrder(client: Client, stock: Stock.Value, price: Int, volume: Int) extends Order {

  var serialNumber: Int = Order.increment

  override def split(newVolume: Int): BuyOrder = BuyOrder(client, stock, price, newVolume)
}

case class SellOrder(client: Client, stock: Stock.Value, price: Int, volume: Int) extends Order {

  var serialNumber: Int = Order.increment

  override def split(newVolume: Int): SellOrder = {
    SellOrder(client, stock, price, newVolume)
  }
}
