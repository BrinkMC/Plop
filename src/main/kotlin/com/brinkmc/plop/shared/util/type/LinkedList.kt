package com.brinkmc.plop.shared.util.type

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*
Credits to https://github.com/zuyatna/linked-list for part of the implementation
 */
class LinkedList<T> {
    private var head: Node<T>? = null
    private var tail: Node<T>? = null
    var size = 0

    // A private mutex to protect the shared state
    private val mutex = Mutex()

    // suspend functions must be used for all operations that modify the list
    suspend fun push(value: T): LinkedList<T> {
        mutex.withLock {
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
    }

    suspend fun append(value: T) {
        mutex.withLock {
            if (isEmpty()) {
                push(value)
                return
            }
            val newNode = Node(value = value, prev = tail)
            tail?.next = newNode
            tail = newNode
            size++
        }
    }

    // Read-only operations can be suspending to fit into a coroutine flow
    suspend fun nodeAt(index: Int): Node<T>? {
        // We still use a lock for consistency to prevent a race condition if the list is being modified
        mutex.withLock {
            var currentNode = head
            var currentIndex = 0

            while (currentNode != null && currentIndex < index) {
                currentNode = currentNode.next
                currentIndex++
            }
            return currentNode
        }
    }

    // This method is read-only, but protecting it with a lock ensures consistency with the rest of the file
    suspend fun first(): Node<T>? {
        mutex.withLock {
            return head
        }
    }

    suspend fun last(): Node<T>? {
        mutex.withLock {
            return tail
        }
    }

    suspend fun insert(value: T, afterNode: Node<T>): Node<T> {
        mutex.withLock {
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
    }

    suspend fun remove(node: Node<T>) {
        mutex.withLock {
            // Handle removing the head node
            if (head == node) {
                head = node.next
            }
            // Handle removing the tail node
            if (tail == node) {
                tail = node.prev
            }
            // Redirect the surrounding nodes' pointers
            node.prev?.next = node.next
            node.next?.prev = node.prev
            size--
        }
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

    suspend fun clear() {
        mutex.withLock {
            head = null
            tail = null
            size = 0
        }
    }
}