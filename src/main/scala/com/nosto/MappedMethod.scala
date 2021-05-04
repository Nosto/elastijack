package com.nosto

import java.lang.reflect.Method

case class MappedMethod(method: Method,
                        elasticsearchMapping: ElasticsearchProperty)
