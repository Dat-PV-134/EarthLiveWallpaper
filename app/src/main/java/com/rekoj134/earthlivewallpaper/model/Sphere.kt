package com.rekoj134.earthlivewallpaper.model

data class SphereMesh(
    val vertices: FloatArray,
    val indices: IntArray
)

fun createSphere(radius: Float, stacks: Int, slices: Int): SphereMesh {
    val vertices = mutableListOf<Float>()
    val indices = mutableListOf<Int>()

    for (i in 0..stacks) {
        val stackAngle = Math.PI / 2 - i * Math.PI / stacks
        val xy = radius * Math.cos(stackAngle)
        val z = radius * Math.sin(stackAngle)

        for (j in 0..slices) {
            val sectorAngle = j * 2 * Math.PI / slices
            val x = xy * Math.cos(sectorAngle)
            val y = xy * Math.sin(sectorAngle)

            vertices.add(x.toFloat())
            vertices.add(y.toFloat())
            vertices.add(z.toFloat())

            val u = j.toFloat() / slices
            val v = i.toFloat() / stacks
            vertices.add(u)
            vertices.add(v)
        }
    }

    val verticesPerRow = slices + 1
    for (i in 0 until stacks) {
        for (j in 0 until slices) {
            val first = i * verticesPerRow + j
            val second = first + verticesPerRow

            indices.add(first)
            indices.add(second)
            indices.add(first + 1)

            indices.add(second)
            indices.add(second + 1)
            indices.add(first + 1)
        }
    }

    return SphereMesh(vertices.toFloatArray(), indices.toIntArray())
}

