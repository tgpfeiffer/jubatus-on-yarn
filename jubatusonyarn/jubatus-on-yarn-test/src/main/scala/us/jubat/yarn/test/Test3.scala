// Jubatus: Online machine learning framework for distributed environment
// Copyright (C) 2014-2015 Preferred Networks and Nippon Telegraph and Telephone Corporation.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License version 2.1 as published by the Free Software Foundation.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
package us.jubat.yarn.test

import us.jubat.yarn.common.{Location, LearningMachineType}
import us.jubat.yarn.client.{Resource, JubatusYarnApplication}
import java.net.InetAddress
import org.apache.hadoop.fs.Path
import scala.util.{Success, Failure}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

// インスタンス名の長さ / 変な文字含む
object Test3 extends App {
  def testcase(machineName: String): Unit = {
    println("アプリケーションを起動します")

    val tApplicationFuture = JubatusYarnApplication.start(
      machineName,
      LearningMachineType.Classifier,
      List(Location(InetAddress.getLocalHost, 2181)),
      new Path(s"hdfs:///jubatus-on-yarn/sample/shogun.json"),
      Resource(priority = 0, memory = 512, virtualCores = 1),
      1
    ).andThen {
      case Failure(e) =>
        println(e.getMessage)
        e.printStackTrace()
      case Success(tApplication) =>
        try {
          println(
            "アプリケーションが起動しました\n"
              + s"\t${tApplication.jubatusProxy}\n"
              + s"\t${tApplication.jubatusServers}"
          )

          println("アプリケーションの状態を取得します")
          val tStatus = tApplication.status
          println(s"\t${tStatus.jubatusProxy}")
          println(s"\t${tStatus.jubatusServers}")
          println(s"\t${tStatus.yarnApplication}")

          println("モデルデータを保存します")
          tApplication.saveModel(new Path("hdfs:///tmp/"), "test").get

          // Thread.sleep(1000)

          println("モデルデータを読み込みます")
          tApplication.loadModel(new Path("hdfs:///tmp/"), "test").get
        } finally {
          println("アプリケーションを停止します")
          Await.ready(
            tApplication.stop().andThen {
              case Failure(e) =>
                println(e.getMessage)
                e.printStackTrace()
              case Success(_) =>
            },
            Duration.Inf
          )

          println("アプリケーションを停止しました")
        }
    }
    Await.ready(tApplicationFuture, Duration.Inf)
    Thread.sleep(1000)
  }

  println("==========================================================")
  println("No1")
  testcase("shogun")
  println("==========================================================")
  println("No2")
  testcase("sho-gun_")

  println("プログラムを終了します")
  System.exit(0)
}
