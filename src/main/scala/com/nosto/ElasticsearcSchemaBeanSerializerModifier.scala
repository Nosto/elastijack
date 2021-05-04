package com.nosto

import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.{BeanDescription, JsonSerializer, SerializationConfig}

class ElasticsearcSchemaBeanSerializerModifier extends BeanSerializerModifier {
  override def modifySerializer(
                                 config: SerializationConfig,
                                 beanDesc: BeanDescription,
                                 serializer: JsonSerializer[_]): JsonSerializer[_] = {
    if (beanDesc.getBeanClass == classOf[ElasticsearchRootSchema]) {
      new TypeOmittingSerializer(
        serializer.asInstanceOf[JsonSerializer[ElasticsearchRootSchema]])
    } else {
      serializer
    }
  }
}
