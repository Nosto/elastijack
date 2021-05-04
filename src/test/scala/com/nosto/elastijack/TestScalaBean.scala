package com.nosto.elastijack

import com.fasterxml.jackson.annotation.JsonProperty
import com.nosto.elasticsearch.ElasticsearchMappings

import java.time.{LocalDate, LocalDateTime}
import java.util.Optional

class TestScalaBean(private val foo: String, private val date: LocalDate, private val dateTime: LocalDateTime) extends ElasticsearchMappings {

  @JsonProperty(required = true)
  def getFoo: Optional[String] = Optional.of(foo)

  @JsonProperty(required = true)
  def getDate: LocalDate = date

  @JsonProperty(required = true)
  def getDateTime: LocalDateTime = dateTime
}
