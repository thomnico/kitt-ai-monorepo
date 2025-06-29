package com.kitt.android

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.InputStream

class WebServerService : Service() {

    private var webServer: WebServer? = null
    private val PORT = 8080

    override fun onCreate() {
        super.onCreate()
        try {
            webServer = WebServer(PORT)
            webServer?.start()
            Log.d("WebServerService", "Web server started on port $PORT")
        } catch (e: IOException) {
            Log.e("WebServerService", "Error starting web server: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webServer?.stop()
        Log.d("WebServerService", "Web server stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    inner class WebServer(port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri
            Log.d("WebServer", "Request for URI: $uri")

            // Determine the file path within assets
            var filePath = if (uri == "/") "/index.html" else uri
            filePath = "dashboard_web_content$filePath" // Prepend the assets folder

            try {
                // Try to open the file from assets
                val inputStream: InputStream = assets.open(filePath)
                val mimeType = getMimeTypeFromExtension(filePath)
                return newFixedLengthResponse(Response.Status.OK, mimeType, inputStream, inputStream.available().toLong())
            } catch (e: IOException) {
                Log.e("WebServer", "File not found in assets: $filePath, error: ${e.message}")
                // If not found in assets, try to serve from the root of assets (for robustness)
                try {
                    val fallbackInputStream: InputStream = assets.open("dashboard_web_content/index.html")
                    val mimeType = getMimeTypeFromExtension("dashboard_web_content/index.html")
                    return newFixedLengthResponse(Response.Status.OK, mimeType, fallbackInputStream, fallbackInputStream.available().toLong())
                } catch (e2: IOException) {
                    Log.e("WebServer", "Fallback index.html not found, error: ${e2.message}")
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404: File not found or accessible.")
                }
            }
        }

        private fun getMimeTypeFromExtension(uri: String): String {
            val dot = uri.lastIndexOf('.')
            if (dot >= 0 && dot < uri.length - 1) {
                when (uri.substring(dot + 1).toLowerCase()) {
                    "htm", "html" -> return "text/html"
                    "css" -> return "text/css"
                    "js" -> return "application/javascript"
                    "png" -> return "image/png"
                    "jpg", "jpeg" -> return "image/jpeg"
                    "gif" -> return "image/gif"
                    "svg" -> return "image/svg+xml"
                    // Add more MIME types as needed
                }
            }
            return "application/octet-stream" // Default MIME type
        }
    }
}
