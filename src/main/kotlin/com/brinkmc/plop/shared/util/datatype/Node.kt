package com.brinkmc.plop.shared.util.datatype

/*
Credits to https://github.com/zuyatna/linked-list for implementation
 */
data class Node<T>(var value: T, var next: Node<T>? = null, var prev: Node<T>? = null) {
    override fun toString(): String {
        return if (next != null) {
            "$value -> ${next.toString()}"
        } else {
            "$value"
        }
    }
}