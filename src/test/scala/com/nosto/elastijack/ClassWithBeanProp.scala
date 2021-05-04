package com.nosto.elastijack

import com.nosto.ElasticsearchProperty
import com.nosto.elasticsearch.ElasticsearchMappings

import scala.beans.BeanProperty

class ClassWithBeanProp
(
  @ElasticsearchProperty(name = "foo", `type` = ElasticsearchProperty.Type.KEYWORD, index = false)
  @BeanProperty
  foo: String) extends ElasticsearchMappings {

  println(foo)
}
