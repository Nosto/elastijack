/** *****************************************************************************
  * Copyright (c) 2016 Nosto Solutions Ltd All Rights Reserved.
  * <p>
  * This software is the confidential and proprietary information of
  * Nosto Solutions Ltd ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the agreement you entered into with
  * Nosto Solutions Ltd.
  * *****************************************************************************/
package com.nosto

import com.fasterxml.jackson.module.jsonSchema.factories._

import scala.reflect.ClassTag

/**
  * A class for mapping scala pojos to elasticsearch query compatible objects.
  * The mapping is marked using [[ElasticsearchProperty]] annotations. Note that
  * we rely on java reflection instead of scala reflection since the scala API
  * still seems pretty experimental. Especially fetching annotation values in
  * a robust way turned out to be a nightmare.
  *
  * This class can do the following:
  * - Generate a map from the given class that can be serialized to JSON and used in Elasticsearch mapping definitions.
  * - Generate a map from a given instance that can be serialized to JSON and used in Elasticsearch index queries.
  *
  * @tparam T The class to be mapped
  */
class ElasticsearchMapper[T]()(implicit classTag: ClassTag[T]) {
  private val jsonMapper = jsonMapperBuilder.build()
  private val schemaMapper = schemaMapperBuilder.build()

  def serializer: ElasticsearchSerializer = new ElasticsearchSerializer

  protected def schemaMapperBuilder: SchemaMapperBuilder =
    new SchemaMapperBuilder
  protected def jsonMapperBuilder: JsonMapperBuilder =
    new JsonMapperBuilder(this)

  protected def wrapperFactory: WrapperFactory =
    new ElasticsearchSchemaFactoryWrapperFactory((provider, factory) =>
      new ElasticsearchSchemaFactoryWrapper(provider, factory))

  def generateMappingProperties: String = {
    val visitor = wrapperFactory.getWrapper(schemaMapper.getSerializerProvider)
    schemaMapper.acceptJsonFormatVisitor(classTag.runtimeClass, visitor)

    schemaMapper.writerWithDefaultPrettyPrinter.writeValueAsString(visitor.finalSchema())
  }

  def map(obj: T): Map[String, _] = {
    jsonMapper.convertValue(obj, classOf[Map[String, Any]])
  }

  def fromJson(json: String): T = jsonMapper.readValue(json, classTag.runtimeClass.asInstanceOf[Class[T]])

  def toJson(obj: T): String = jsonMapper.writeValueAsString(obj)
}
