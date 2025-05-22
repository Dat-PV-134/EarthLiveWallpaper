package com.rekoj134.earthlivewallpaper

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

            vertices.add(((x / radius) + 1).toFloat() / 2)
            vertices.add(((y / radius) + 1).toFloat() / 2)
            vertices.add(((z / radius) + 1).toFloat() / 2)
            vertices.add(1.0f)
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
