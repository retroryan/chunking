import org.apache.log4j.{Level, Logger}

import org.apache.spark.sql.cassandra.CassandraSQLContext
//import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark._
//import org.apache.spark.SparkContext._
import com.datastax.spark.connector._

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.utils.UUIDs

/* --- */

object SparkChunking {


  def createSchema(cc:CassandraConnector, keySpaceName:String, tableName1:String, tableName2:String) = {
    cc.withSessionDo { session =>
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS ${keySpaceName} WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1}")

      session.execute("CREATE TABLE IF NOT EXISTS " +
                      s"${keySpaceName}.${tableName1} (test_id text, chunk_count int, primary key( test_id ));")

      session.execute("CREATE TABLE IF NOT EXISTS " +
                      s"${keySpaceName}.${tableName2} (test_id text, filename text, seqnum int, " +
                      s"bytes blob, primary key ((test_id, filename, seqnum)));")
    }
  }

  /* Set the logger level. Optionally increase value from Level.ERROR to LEVEL.WARN or more verbose yet, LEVEL.INFO */
  Logger.getRootLogger.setLevel(Level.ERROR)

   def main(args: Array[String]) {

    val sparkMasterHost = "127.0.0.1"
    val cassandraHost = "127.0.0.1"
    val cassandraKeyspace = "benchmark"
    val cassandraTable1 = "chunk_meta"
    val cassandraTable2 = "chunk_data"

    // Tell Spark the address of one Cassandra node:
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", cassandraHost)
      .set("spark.cleaner.ttl", "3600")
      .setMaster("local[10]")
      .setAppName(getClass.getSimpleName)

    // Connect to the Spark cluster:
    lazy val sc = new SparkContext(conf)
    lazy val cc = CassandraConnector(sc.getConf)

    createSchema(cc, cassandraKeyspace, cassandraTable1, cassandraTable2)

    val bigfile = sc.binaryFiles(s"file:///home/dse/Chunking")

  }

}
