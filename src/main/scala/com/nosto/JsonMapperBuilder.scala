package com.nosto

import com.fasterxml.jackson.databind.{Module, ObjectMapper}

class JsonMapperBuilder(
                         private val elasticsearchMapper: ElasticsearchMapper[_],
                         protected val modules: List[Module])
  extends MapperBuilderLike {

  def this(elasticsearchMapper: ElasticsearchMapper[_]) {
    this(elasticsearchMapper, Nil)
  }

  private val annotationProcessingModule = new ESMapperModule(
    elasticsearchMapper.serializer)

  override protected def mapper: ObjectMapper =
    new ObjectMapper()
      .registerModule(annotationProcessingModule)

  def withAdditionalModule(module: Module): JsonMapperBuilder = {
    new JsonMapperBuilder(elasticsearchMapper, module :: modules)
  }
}
