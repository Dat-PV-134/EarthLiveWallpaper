package com.rekoj134.earthlivewallpaper.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object ShaderReader {
    // Phương thức đọc text từ file raw
    fun readTextFileFromResource(context: Context, resourceId: Int) : String {
        val body = StringBuilder()
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var nextLine = bufferedReader.readLine()
            while (nextLine != null) {
                body.append(nextLine)
                body.append('\n')
                nextLine = bufferedReader.readLine()
            }
        } catch (e: Exception) {
            // resource not found
            e.printStackTrace()
        }
        return body.toString()
    }
}