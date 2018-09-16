package orders

import com.typesafe.scalalogging.LazyLogging
import stocks.Stock

import scala.collection.mutable.ListBuffer

class OrderBook(val stock: Stock.Value, val exactMatch: Boolean, val checkBalance: Boolean) extends LazyLogging{
  var buyOrders: ListBuffer[BuyOrder] = ListBuffer[BuyOrder]()
  var sellOrders: ListBuffer[SellOrder] = ListBuffer[SellOrder]()

  def addOrder(order: Order): Boolean = {
    assert(stock == order.stock)
    order match {
      case buy: BuyOrder ⇒
        if (checkBalance && !checkBuyOrder(buy)) {
          logger.error(s"The Order #${order.serialNumber} cannot be added to order book because it costs more than the buyer's USD balance")
          return false
        }
        if (exactMatch) {
          buyOrders = buyOrders.sortBy(_.serialNumber)
          buyOrders.prepend(buy)
          sellOrders = sellOrders.sortBy(_.serialNumber)
          logger.info(s"Buy ${buy.stock} for $$ ${buy.price*buy.volume} with Exact Matching(Simple)")
        }
        else {
          buyOrders.prepend(buy)
          buyOrders.sortBy(o => (-o.price, o.serialNumber))
          logger.info(s"Buy ${buy.stock} for $$ ${buy.price*buy.volume} with Full Matching (Advanced)")
        }
      case sell: SellOrder ⇒
        if (checkBalance && !checkSellOrder(sell)){
          logger.error(s"The Order #${order.serialNumber} cannot be added to order book because the seller has less stock than he wants to sell")
          return false
        }
        if(exactMatch) {
          sellOrders = sellOrders.sortBy(_.serialNumber)
          sellOrders.prepend(sell)
          buyOrders = buyOrders.sortBy(_.serialNumber)
          logger.info(s"Sell ${sell.stock} for $$ ${sell.price*sell.volume} with Exact Matching(Simple)")
        }
        else {
          sellOrders.prepend(sell)
          sellOrders.sortBy(o => (o.price, o.serialNumber))
          logger.info(s"Sell ${sell.stock} for $$ ${sell.price*sell.volume} with Full Matching (Advanced)")

        }
    }
    true
  }

  def matchOrders(newOrder: Order, callCount: Int) {
    if (buyOrders.nonEmpty && sellOrders.nonEmpty &&
      callCount <= (if(buyOrders.length > sellOrders.length) buyOrders.length else sellOrders.length)) {
      if (buyOrders.head.client.equals(sellOrders.head.client )){
        if (newOrder.isInstanceOf[BuyOrder]){
          sellOrders.append(sellOrders.head)
          sellOrders.remove(0)
        } else {
          buyOrders.append(buyOrders.head)
          buyOrders.remove(0)
        }
        if(sellOrders.isEmpty | buyOrders.isEmpty) return
      }
      val topOfBook = (buyOrders.head, sellOrders.head)
      topOfBook match {
        case (buyOrder, sellOrder) if exactMatch ⇒ {
          if(buyOrder.price == sellOrder.price && buyOrder.volume == sellOrder.volume){
            trade(buyOrder, sellOrder, if(buyOrder == newOrder) sellOrder.price else buyOrder.price)
            buyOrders = buyOrders.tail
            sellOrders = sellOrders.tail
          }
          else {
            if(newOrder.isInstanceOf[BuyOrder]){
              sellOrders.append(sellOrders.head)
              sellOrders.remove(0)
            }else {
              buyOrders.append(buyOrders.head)
              buyOrders.remove(0)
            }
            matchOrders(newOrder, callCount + 1)
          }
        }

        case (buyOrder, sellOrder) if buyOrder.price < sellOrder.price ⇒ // no match

        case (buyOrder, sellOrder) if !exactMatch && buyOrder.price >= sellOrder.price  && buyOrder.volume == sellOrder.volume ⇒
          trade(buyOrder, sellOrder, if(buyOrder == newOrder) sellOrder.price else buyOrder.price)
          buyOrders = buyOrders.tail
          sellOrders = sellOrders.tail

        case (buyOrder, sellOrder) if !exactMatch && buyOrder.price >= sellOrder.price  && buyOrder.volume < sellOrder.volume ⇒
          val matchingSells = sellOrder.split(buyOrder.volume)
          val remainingSells = sellOrder.split(sellOrder.volume - buyOrder.volume)
          trade(buyOrder, matchingSells, if(buyOrder == newOrder) sellOrder.price else buyOrder.price)
          buyOrders = buyOrders.tail
          sellOrders.update(0, remainingSells)
          matchOrders(newOrder, callCount + 1)

        case (buyOrder, sellOrder) if !exactMatch && buyOrder.price >= sellOrder.price && buyOrder.volume > sellOrder.volume ⇒
          val matchingBuys = buyOrder.split(sellOrder.volume)
          val remainingBuys = buyOrder.split(buyOrder.volume - sellOrder.volume)
          trade(matchingBuys, sellOrder, if(buyOrder == newOrder) sellOrder.price else buyOrder.price)
          buyOrders.update(0, remainingBuys)
          sellOrders = sellOrders.tail
          matchOrders(newOrder, callCount + 1)
        case _ =>
      }
    }
  }

  private def checkBuyOrder(buyOrder: BuyOrder): Boolean =
    buyOrder.client.usdBalance >= buyOrder.price * buyOrder.volume

  private def checkSellOrder(sellOrder: SellOrder): Boolean =
   sellOrder.client.stocks(sellOrder.stock) >= sellOrder.volume

  def trade(buyOrder: BuyOrder, sellOrder: SellOrder, price: Int): Unit = {
    buyOrder.client.buy(buyOrder, price)
    sellOrder.client.sell(sellOrder, price)
  }
}