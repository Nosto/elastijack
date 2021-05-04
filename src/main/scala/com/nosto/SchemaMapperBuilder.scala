package com.nosto

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{Module, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class SchemaMapperBuilder(protected val modules: List[Module])
  extends MapperBuilderLike {
  def this() {
    this(Nil)
  }

  override protected def mapper: ObjectMapper =
    new ObjectMapper()
      .registerModule(DefaultScalaModule)
      .registerModule(new SimpleModule()
        .setSerializerModifier(new ElasticsearcSchemaBeanSerializerModifier))

  def withAdditionalModule(module: Module): SchemaMapperBuilder = {
    new SchemaMapperBuilder(module :: modules)
  }
}
