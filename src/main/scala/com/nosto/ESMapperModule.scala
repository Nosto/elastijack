package com.nosto

import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.{BeanDescription, JavaType, JsonSerializer, SerializationConfig}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.nosto.elasticsearch.ElasticsearchMappings

class ESMapperModule(serializer: ElasticsearchSerializer)
  extends DefaultScalaModule
    with Serializable {

  +=(
    new Serializers.Base() {
      override def findSerializer(
                                   config: SerializationConfig,
                                   `type`: JavaType,
                                   beanDesc: BeanDescription): JsonSerializer[_] = {
        if (classOf[ElasticsearchMappings].isAssignableFrom(
          `type`.getRawClass)) {
          serializer
        } else {
          super.findSerializer(config, `type`, beanDesc)
        }
      }
    })
}
