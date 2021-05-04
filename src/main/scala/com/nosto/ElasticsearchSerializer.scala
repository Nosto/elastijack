package com.nosto

import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.{BeanProperty, JsonSerializer, SerializerProvider}
import com.nosto.elasticsearch.ElasticsearchMappings

import java.beans.Introspector
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import scala.collection.mutable

class ElasticsearchSerializer()
  extends com.fasterxml.jackson.databind.ser.std.StdSerializer[
    ElasticsearchMappings](classOf[ElasticsearchMappings])
    with ContextualSerializer
    with Logging {

  private val annotatedMethods =
    new mutable.HashMap[Class[_], Array[(Method, Annotation)]]

  private def getAnnotatedMethods(c: Class[_]) = this.synchronized {
    annotatedMethods.getOrElseUpdate(
      c,
      Introspector
        .getBeanInfo(c)
        .getMethodDescriptors
        .map(_.getMethod)
        .flatMap(m => m.getDeclaredAnnotations.map(a => (m, a))))
  }

  override def serialize(
                          t: ElasticsearchMappings,
                          jsonGenerator: com.fasterxml.jackson.core.JsonGenerator,
                          serializerProvider: SerializerProvider): Unit = {
    jsonGenerator.writeStartObject()

    getAnnotatedMethods(t.getClass).foreach {
      case (m, a) if a.annotationType() == classOf[ElasticsearchProperty] =>
        m.invoke(t) match {
          case Some(obj) => {
            jsonGenerator.writeFieldName(
              a.asInstanceOf[ElasticsearchProperty].name())
            jsonGenerator.writeObject(obj)
          }
          case None => Unit
          case x if x != null => {
            jsonGenerator.writeFieldName(
              a.asInstanceOf[ElasticsearchProperty].name())
            jsonGenerator.writeObject(x)
          }
          case null => {
            println(
              s"Omitting null field ${a.asInstanceOf[ElasticsearchProperty].name()} on object ${t}")
          }
        }
      case (_, a)
        if a
          .annotationType()
          .getName == "jdk.internal.HotSpotIntrinsicCandidate" =>
      // see https://bugs.openjdk.java.net/browse/JDK-8076112
      // with scala 2.12, the jvm target compatibility is still 1.8
      // so we need to match with the string class name
    }

    jsonGenerator.writeEndObject()
  }

  override def createContextual(prov: SerializerProvider,
                                property: BeanProperty): JsonSerializer[_] = {
    new ElasticsearchSerializer
  }
}
