package com.spark.scala.sbt.sparkudf

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{StructField, _}

class YearlyAvg() extends UserDefinedAggregateFunction {

  // Input Data Type Schema
  def inputSchema: StructType = StructType(Array(StructField("ts", TimestampType)))

  // Intermediate Schema
  def bufferSchema = StructType(Array(
    StructField("totalBookings", IntegerType),
    StructField("previousYear", IntegerType),
    StructField("currentYear", IntegerType),
    StructField("distinctYearCount", IntegerType)
  ))

  def dataType: DataType = IntegerType

  def deterministic = true

  def initialize(buffer: MutableAggregationBuffer) = {
    buffer(0) = 0
    buffer(1) = 0
    buffer(2) = 0
    buffer(3) = 1
  }

  def update(buffer: MutableAggregationBuffer, input: Row) = {
    val dateString = input(0).toString()
    val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
    val zdt = ZonedDateTime.parse(dateString, dtf.withZone(ZoneId.systemDefault))

    if(buffer.getInt(2) == 0){
      buffer(2) = zdt.getYear()
    }
    buffer(1) = buffer(2)
    buffer(2) = zdt.getYear()

    if(buffer.getInt(2) != buffer.getInt(1)){
      buffer(3) = buffer.getInt(3) + 1
    }
    buffer(0) = buffer.getInt(0) + 1

    println(buffer(0),buffer(1),buffer(2),buffer(3))
  }

  def merge(buffer1: MutableAggregationBuffer, buffer2: Row) = {
    buffer1(0) = buffer2.getInt(0)
    buffer1(3) = buffer2.getInt(3)
  }

  def evaluate(buffer: Row) = {
    buffer.getInt(0)/buffer.getInt(3)
  }

}