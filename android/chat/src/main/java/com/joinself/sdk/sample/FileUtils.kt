package com.joinself.sdk.sample

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileUtils {
    companion object {
        fun writeToFile(inputStream: InputStream, file: File, doProgress: (bytesRead: Int) -> Unit): Boolean {
            try {
                val out = FileOutputStream(file)
                val buf = ByteArray(1024)
                var bytesRead = 0
                var len: Int = inputStream.read(buf)
                while (len > 0) {
                    out.write(buf, 0, len)
                    bytesRead += len
                    doProgress(bytesRead)
                    len = inputStream.read(buf)
                }
                out.close()
                inputStream.close()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }
}