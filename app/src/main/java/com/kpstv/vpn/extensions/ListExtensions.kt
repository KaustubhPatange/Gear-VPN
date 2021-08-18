package com.kpstv.vpn.extensions

fun<T> MutableList<T>.clearAndAdd(items: List<T>) {
  clear()
  addAll(items)
}