package com.nosto

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable
import com.fasterxml.jackson.databind.{BeanProperty, JavaType, SerializerProvider}
import com.fasterxml.jackson.module.jsonSchema.factories.{ObjectVisitor, VisitorContext, WrapperFactory}
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.SingleItems
import com.fasterxml.jackson.module.jsonSchema.types.{ArraySchema, ObjectSchema}

class ElasticsearchSchemaObjectVisitor(provider: SerializerProvider,
                                       schema: ObjectSchema,
                                       wrapperFactory: WrapperFactory,
                                       visitorContext: VisitorContext)
  extends ObjectVisitor(provider, schema, wrapperFactory) {
  setVisitorContext(visitorContext)

  override def optionalProperty(prop: BeanProperty): Unit = {
    val ann = prop.getAnnotation(classOf[ElasticsearchProperty])
    if (ann != null) {
      val ps = propertySchema(prop)

      ps match {
        case _: ElasticsearchJsonSchemaBase =>
          schema.putOptionalProperty(ann.name(), ps)
        case a: ArraySchema => {
          val inner = a.getItems match {
            case s: SingleItems =>
              s.getSchema match {
                case rootSchema: ElasticsearchRootSchema =>
                  new ElasticsearchObjectSchema(ann, rootSchema.backing)
                case os: ObjectSchema =>
                  new ElasticsearchObjectSchema(ann, os)
                case _ => new ElasticsearchAnnotatedSchema(ann)
              }
            case null => new ElasticsearchAnnotatedSchema(ann)
          }

          schema.putOptionalProperty(ann.name(), inner)
        }
        case os: ObjectSchema => {
          schema.putOptionalProperty(ann.name(),
            new ElasticsearchObjectSchema(ann, os))
        }
        case _ =>
          schema.putOptionalProperty(ann.name(),
            new ElasticsearchAnnotatedSchema(ann))
      }
    }
  }

  /**
   * Not implemented as there has not been a use-case.
   * Throws to avoid accidenta use of the default implementation.
   */
  override def optionalProperty(name: String,
                                handler: JsonFormatVisitable,
                                propertyTypeHint: JavaType): Unit = ???

  /**
   * Not implemented as there has not been a use-case.
   * Throws to avoid accidenta use of the default implementation.
   */
  override def property(prop: BeanProperty): Unit = ???

  /**
   * Not implemented as there has not been a use-case.
   * Throws to avoid accidenta use of the default implementation.
   */
  override def property(name: String,
                        handler: JsonFormatVisitable,
                        propertyTypeHint: JavaType): Unit = ???
}
