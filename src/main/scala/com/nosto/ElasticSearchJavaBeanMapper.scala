package com.nosto

import java.beans.Introspector
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{BeanProperty, JsonSerializer, SerializerProvider}
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.jsonSchema.factories.{ObjectVisitor, VisitorContext, WrapperFactory}
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.nosto.elasticsearch.ElasticsearchMappings

import scala.reflect.ClassTag

class ElasticSearchJavaBeanMapper[T]()(implicit classTag: ClassTag[T])
    extends ElasticsearchMapper[T]
    with JavaBeanSupport[T]

trait JavaBeanSupport[T] { this: ElasticsearchMapper[T] =>
  override def serializer: ElasticsearchSerializer =
    new ElasticsearchSerializer with JavaESAnnotatedBeanSupport

  override def wrapperFactory: WrapperFactory =
    new ElasticsearchSchemaFactoryWrapperFactory((provider, factory) =>
      new ElasticsearchJavaBeanSchemaFactoryWrapper(provider, factory))

  override protected def schemaMapperBuilder: SchemaMapperBuilder =
    (new SchemaMapperBuilder)
      .withAdditionalModule(new JavaTimeModule)
      .withAdditionalModule(new Jdk8Module)

  override protected def jsonMapperBuilder: JsonMapperBuilder =
    (new JsonMapperBuilder(this))
      .withAdditionalModule(new JavaTimeModule)
      .withAdditionalModule(new Jdk8Module)
}

trait JavaESAnnotatedBeanSupport { this: ElasticsearchSerializer =>

  private def getAnnotatedMethods(c: Class[_]): Array[(Method, Annotation)] = {
    val properties = Introspector.getBeanInfo(c);

    val fieldAnnotations =
      properties.getBeanDescriptor.getBeanClass.getDeclaredFields
        .map(x => (x.getName, x.getDeclaredAnnotations))
        .toMap

    properties.getPropertyDescriptors.flatMap(
      descriptor =>
        fieldAnnotations
          .getOrElse(descriptor.getName, Array.empty)
          .map(annotation => (descriptor.getReadMethod, annotation)))
  }

  private val MAX_ID_LENGTH = 256

  private def getIdTrimmed(id: String): String = {
    id.substring(0, Math.min(id.length(), MAX_ID_LENGTH));
  }

  override def serialize(t: ElasticsearchMappings,
                         jsonGenerator: JsonGenerator,
                         serializerProvider: SerializerProvider): Unit = {

    def writeObject(obj: Any, annotation: ElasticsearchProperty): Unit = {
      jsonGenerator.writeFieldName(annotation.name())

      (obj, annotation) match {
        case (d: LocalDateTime, prop) if prop.format().isEmpty =>
          jsonGenerator.writeNumber(d.toInstant(ZoneOffset.UTC).toEpochMilli)
        case (t: Temporal, prop) if !prop.format().isEmpty =>
          jsonGenerator.writeString(
            DateTimeFormatter.ofPattern(prop.format()).format(t))
        case (s: String, prop)
            if prop.name() == ElasticSearchJavaBeanMapper.ID =>
          jsonGenerator.writeString(getIdTrimmed(s))
        case (o, _) => jsonGenerator.writeObject(o)
      }
    }

    jsonGenerator.writeStartObject()
    getAnnotatedMethods(t.getClass).foreach {
      case (m, a: ElasticsearchProperty) =>
        m.invoke(t) match {
          case Some(obj) => {
            writeObject(obj, a)
          }
          case None => Unit
          case x if x != null => {
            writeObject(x, a)
          }
          case null =>
        }
      case (_, a)
          if a
            .annotationType()
            .getName == "jdk.internal.HotSpotIntrinsicCandidate" =>
      // see https://bugs.openjdk.java.net/browse/JDK-8076112
      // with scala 2.12, the jvm target compatibility is still 1.8
      // so we need to match with the string class name
      case (_, a)
          if a
            .annotationType()
            .getName == "com.fasterxml.jackson.annotation.JsonProperty" =>
    }

    jsonGenerator.writeEndObject()
  }

  override def createContextual(prov: SerializerProvider,
                                prop: BeanProperty): JsonSerializer[_] = {
    new ElasticsearchSerializer with JavaESAnnotatedBeanSupport
  }
}

class ElasticsearchJavaBeanSchemaFactoryWrapper(provider: SerializerProvider,
                                                wrapperFactory: WrapperFactory)
    extends ElasticsearchSchemaFactoryWrapper(provider, wrapperFactory) {

  override def visitor(provider: SerializerProvider,
                       schema: ObjectSchema,
                       wrapperFactory: WrapperFactory,
                       visitorContext: VisitorContext): ObjectVisitor =
    new ElasticsearchJavaBeanSchemaObjectVisitor(provider,
                                                 schema,
                                                 wrapperFactory,
                                                 visitorContext)
}

class ElasticsearchJavaBeanSchemaObjectVisitor(provider: SerializerProvider,
                                               schema: ObjectSchema,
                                               wrapperFactory: WrapperFactory,
                                               visitorContext: VisitorContext)
    extends ElasticsearchSchemaObjectVisitor(provider,
                                             schema,
                                             wrapperFactory,
                                             visitorContext) {

  /**
    * Falling back to the same property handling as for optional property
    * (needed for Java bean support)
    */
  override def property(prop: BeanProperty): Unit = {
    optionalProperty(prop)
  }
}

object ElasticSearchJavaBeanMapper {
  val ID = "id"
  val ID_CONFIG = Map("es.mapping.id" -> ID)
}
