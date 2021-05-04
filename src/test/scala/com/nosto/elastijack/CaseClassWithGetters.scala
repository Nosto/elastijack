package com.nosto.elastijack

import com.nosto.ElasticsearchProperty
import com.nosto.elasticsearch.ElasticsearchMappings

import scala.beans.BeanProperty

case class CaseClassWithGetters(foo: String, @BeanProperty bar: Float) extends ElasticsearchMappings {

  @ElasticsearchProperty(name = "foo",
    `type` = ElasticsearchProperty.Type.KEYWORD,
    index = false)
  def getName: Option[String] = Some("some")
}
