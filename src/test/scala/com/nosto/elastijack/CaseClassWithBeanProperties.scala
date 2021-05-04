package com.nosto.elastijack

import scala.beans.BeanProperty

case class CaseClassWithBeanProperties
(
  @BeanProperty val name: String,
  @BeanProperty val age: Int,
  @BeanProperty val hobbies: Float
)
