package com.kpstv.vpn.extensions

fun<T> MutableList<T>.clearAndAdd(items: List<T>) {
  clear()
  addAll(items)
}

inline fun<T> List<T>.fastForEach(block: (T) -> Unit) {
  val itr = iterator()
  while(itr.hasNext()) {
    block(itr.next())
  }
}
