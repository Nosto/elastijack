package com.nosto

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor
import com.fasterxml.jackson.databind.{JavaType, SerializerProvider}
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.factories.{ObjectVisitor, SchemaFactoryWrapper, VisitorContext, WrapperFactory}
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema

/**
 * A customized visitor that intercepts generated [[JsonSchema]] instances
 * and uses [[ElasticsearchJsonSchemaBase]] based objects instead.
 */
class ElasticsearchSchemaFactoryWrapper(_provider: SerializerProvider,
                                        /* use only as an initial value!! */
                                        _wrapperFactory: WrapperFactory)
  extends SchemaFactoryWrapper(_provider, _wrapperFactory) {

  /**
   * Customised [[ObjectSchema]] visits:
   * - Disable reference schemas as there is no support for such things in Elasticsearch (so don't call visitorContext.addSeenSchemaUri)
   * - Put [[ElasticsearchJsonSchemaBase]] based objects to schema instead of standard [[JsonSchema]] ones.
   */
  override def expectObjectFormat(
                                   convertedType: JavaType): JsonObjectFormatVisitor = {
    val s = schemaProvider.objectSchema
    schema = new ElasticsearchRootSchema(s)

    // if we don't already have a recursive visitor context, create one
    if (visitorContext == null) visitorContext = new VisitorContext

    visitor(provider, s, _wrapperFactory, visitorContext)
  }

  def visitor(provider: SerializerProvider,
              schema: ObjectSchema,
              wrapperFactory: WrapperFactory,
              visitorContext: VisitorContext): ObjectVisitor =
    new ElasticsearchSchemaObjectVisitor(provider,
      schema,
      wrapperFactory,
      visitorContext)
}
