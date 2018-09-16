package clients

import com.typesafe.scalalogging.LazyLogging
import orders.{BuyOrder, SellOrder}
import stocks.Stock

import scala.collection.mutable


class Client(val name: String, var usdBalance: Int, var stocks: mutable.HashMap[Stock.Value, Int]) extends LazyLogging {

  require(stocks.size == 4 && Stock.values.forall(s => stocks.contains(s)), "There must be exactly 4 stocks A, B, C and D")

  def buy(buyOrder: BuyOrder, price: Int): Unit = {
    assert(buyOrder.client == this)
    usdBalance -= price * buyOrder.volume
    stocks.update(buyOrder.stock, stocks(buyOrder.stock) + buyOrder.volume)
    logger.debug(s"Buy Order of $$ ${price * buyOrder.volume}")
  }

  def sell(sellOrder: SellOrder, price: Int): Unit = {
    assert(sellOrder.client == this)
    usdBalance += price * sellOrder.volume
    stocks.update(sellOrder.stock, stocks(sellOrder.stock) - sellOrder.volume)
    logger.debug(s"Sell Order of $$ ${price * sellOrder.volume}")

  }
}
