package resourceManager

import java.io.{File, IOException, PrintWriter}

import clients.Client
import com.typesafe.scalalogging.LazyLogging
import orders.{BuyOrder, Order, SellOrder}
import stocks.Stock

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.{Failure, Success, Try}


object ResourceManager extends LazyLogging{

  private val buy = "b"
  private val sell = "s"

  def parseOrders(filepath: String, clients: ArrayBuffer[Client]): ArrayBuffer[Order] = {
    val orders = new ArrayBuffer[Order]
    try {
      parseFromFile(filepath).foreach(line => {
        val client = clients.find(_.name == line(0)) match {
          case Some(client: Client) => client
          case None => throw ResourceParseException(s"Client ${line(0)} not found")
        }
        val orderClass = if (line(1).equals(buy)) BuyOrder
        else if (!line(1).equals(sell)) throw ResourceParseException(s"Operation ${line(1)} is not supported")
        else SellOrder
        val stock = Stock.values.find(_.toString == line(2)) match {
          case Some(stock: Stock.Value) => stock
          case None => throw ResourceParseException(s"Stock ${line(2)} not found")
        }
        orders.append(orderClass(client, stock, line(3).toInt, line(4).toInt))
      })
      orders
    } catch {
      case ex: ResourceParseException =>
        throw ex
      case ex: Exception =>
        logger.error(ex.getMessage)
        orders.clear()
        orders
    }
  }

  def parseClients(filepath: String): ArrayBuffer[Client] = {
    val clients = new ArrayBuffer[Client]
    try {
      parseFromFile(filepath).foreach(line => {
        clients.append(new Client(line(0), line(1).toInt,
          mutable.HashMap(
            Stock.A -> line(2).toInt,
            Stock.B -> line(3).toInt,
            Stock.C -> line(4).toInt,
            Stock.D -> line(5).toInt)
        ))
      })
      clients
    } catch {
      case ex: ResourceParseException =>
        throw ex
      case ex: Exception =>
        logger.error(ex.getMessage)
        clients.clear()
        clients

    }
  }

  def exportResult(filepath: String, clients: ArrayBuffer[Client]): Unit = {
    val file = new File(filepath)
    if (file.exists()) file.delete()
    val printWriter = new PrintWriter(file)
    Try(clients.foreach(a => {
      printWriter.write(s"${a.name}\t${a.usdBalance}\t")
      a.stocks.toSeq.sortBy(_._1).foreach(s => printWriter.write(s"${s._2}\t"))
      printWriter.write("\n")
    })) match {
      case Success(_) => printWriter.close()
      case Failure(ex) => logger.error(ex.getMessage); printWriter.close()
    }
  }

  private def parseFromFile(filepath: String): List[Array[String]] = {
    Try(Source.fromFile(filepath).getLines.filter(!_.isEmpty).toList.map(res => res.split("\\s+").map(_.trim))) match {
      case Success(listOfArrays) => listOfArrays
      case Failure(ex) => throw ResourceParseException("There was an error parsing the file", ex)
    }
  }
}
