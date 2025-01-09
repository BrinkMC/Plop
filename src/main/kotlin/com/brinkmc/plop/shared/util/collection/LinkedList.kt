package com.brinkmc.plop.shared.util.collection

/*
Credits to https://github.com/zuyatna/linked-list for implementation
 */
class LinkedList<T> {
    private var head: Node<T>? = null
    private var tail: Node<T>? = null
    var size = 0
        private set

    fun push(value: T): LinkedList<T> {
        val newNode = Node(value = value, next = head)
        if (head != null) {
            head?.prev = newNode
        }
        head = newNode
        if (tail == null) {
            tail = head
        }
        size++
        return this
    }

    fun append(value: T) {
        if (isEmpty()) {
            push(value)
            return
        }
        val newNode = Node(value = value, prev = tail)
        tail?.next = newNode
        tail = newNode
        size++
    }

    fun nodeAt(index: Int): Node<T>? {
        var currentNode = head
        var currentIndex = 0

        while (currentNode != null && currentIndex < index) {
            currentNode = currentNode.next
            currentIndex++
        }

        return currentNode
    }

    fun first(): Node<T>? {
        return head
    }

    fun last(): Node<T>? {
        return tail
    }

    fun insert(value: T, afterNode: Node<T>): Node<T> {
        if (tail == afterNode) {
            append(value)
            return tail!!
        }

        val newNode = Node(value = value, next = afterNode.next, prev = afterNode)
        afterNode.next?.prev = newNode
        afterNode.next = newNode
        size++
        return newNode
    }

    private fun isEmpty(): Boolean {
        return size == 0
    }

    override fun toString(): String {
        return if (isEmpty()) {
            "Empty List"
        } else {
            head.toString()
        }
    }

    fun clear() {
        head = null
        tail = null
        size = 0
    }
}