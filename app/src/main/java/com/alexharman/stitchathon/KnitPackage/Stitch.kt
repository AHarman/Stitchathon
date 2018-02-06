package com.alexharman.stitchathon.KnitPackage

class Stitch private constructor(val type: String, val width: Int = 1, val isSplit: Boolean = false) {
    lateinit var madeOf: Array<String>
        private set

    var isDone: Boolean = false

     @JvmOverloads constructor(type: String, width: Int = 1) : this(type, width, type.contains("/")) {
         if (type.contains("/")) {
             madeOf = type.split("/").toTypedArray()
         }
     }

    override fun toString(): String = type
}
