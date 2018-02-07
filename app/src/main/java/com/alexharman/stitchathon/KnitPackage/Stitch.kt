package com.alexharman.stitchathon.KnitPackage

data class Stitch constructor(val type: String, val width: Int = 1) {
    val isSplit: Boolean = type.contains("/")
    val madeOf: Array<Stitch> =
            if (isSplit)
                type.split("/").map { Stitch(it) }.toTypedArray()
            else
                arrayOf()

    override fun toString(): String = type
}
