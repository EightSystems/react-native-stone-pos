package com.reactnativestonepos.helpers

class CodedException(code: String, internalMessage: String) : Throwable() {
  val code: String = code
  val internalMessage: String = internalMessage
  override val message: String = String.format("%s: %s", code, internalMessage)
}
