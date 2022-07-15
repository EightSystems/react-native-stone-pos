package com.reactnativestonepos.helpers

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap

@Suppress("UNCHECKED_CAST")
fun writableMapOf(vararg values: Pair<String, *>): WritableMap {
  val map = Arguments.createMap()
  for ((key, value) in values) {
    when (value) {
      null -> map.putNull(key)
      is Boolean -> map.putBoolean(key, value)
      is Double -> map.putDouble(key, value)
      is Int -> map.putInt(key, value)
      is String -> map.putString(key, value)
      is WritableMap -> map.putMap(key, value)
      is WritableArray -> map.putArray(key, value)
      is List<*> -> map.putArray(key, writableArrayFrom(value))
      is Map<*, *> -> map.putMap(key, writableMapFrom(value as Map<String, *>))
      else -> throw IllegalArgumentException("Unsupported value type ${value::class.java.name} for key [$key]")
    }
  }
  return map
}

@Suppress("UNCHECKED_CAST")
fun writableMapFrom(values: Map<String, *>): WritableMap {
  val map = Arguments.createMap()
  for ((key, value) in values.toList().iterator()) {
    when (value) {
      null -> map.putNull(key)
      is Boolean -> map.putBoolean(key, value)
      is Double -> map.putDouble(key, value)
      is Int -> map.putInt(key, value)
      is String -> map.putString(key, value)
      is WritableMap -> map.putMap(key, value)
      is WritableArray -> map.putArray(key, value)
      is List<*> -> map.putArray(key, writableArrayFrom(value))
      is Map<*, *> -> map.putMap(key, writableMapFrom(value as Map<String, *>))
      else -> throw IllegalArgumentException("Unsupported value type ${value::class.java.name} for key [$key]")
    }
  }
  return map
}

@Suppress("UNCHECKED_CAST")
fun writableArrayFrom(values: List<*>): WritableArray {
  val array = Arguments.createArray()
  for (value in values) {
    when (value) {
      null -> array.pushNull()
      is Boolean -> array.pushBoolean(value)
      is Double -> array.pushDouble(value)
      is Int -> array.pushInt(value)
      is String -> array.pushString(value)
      is WritableArray -> array.pushArray(value)
      is WritableMap -> array.pushMap(value)
      is List<*> -> array.pushArray(writableArrayFrom(value))
      is Map<*, *> -> array.pushMap(writableMapFrom(value as Map<String, *>))
      else -> throw IllegalArgumentException("Unsupported type ${value::class.java.name}")
    }
  }
  return array
}

@Suppress("UNCHECKED_CAST")
fun writableArrayOf(vararg values: Any?): WritableArray {
  val array = Arguments.createArray()
  for (value in values) {
    when (value) {
      null -> array.pushNull()
      is Boolean -> array.pushBoolean(value)
      is Double -> array.pushDouble(value)
      is Int -> array.pushInt(value)
      is String -> array.pushString(value)
      is WritableArray -> array.pushArray(value)
      is WritableMap -> array.pushMap(value)
      is List<*> -> array.pushArray(writableArrayFrom(value))
      is Map<*, *> -> array.pushMap(writableMapFrom(value as Map<String, *>))
      else -> throw IllegalArgumentException("Unsupported type ${value::class.java.name}")
    }
  }
  return array
}

