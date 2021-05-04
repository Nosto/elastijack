package com.nosto

import com.fasterxml.jackson.databind.{Module, ObjectMapper}

trait MapperBuilderLike {
  protected def mapper: ObjectMapper

  protected val modules: Seq[Module]

  def build(): ObjectMapper = {
    val builtMapper = mapper
    modules.foreach(builtMapper.registerModule)
    builtMapper
  }
}
