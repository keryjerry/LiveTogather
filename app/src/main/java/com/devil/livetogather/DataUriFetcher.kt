package com.devil.livetogather

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options

/**
 * High-performance Coil Fetcher for Base64 Data URIs ("data:image/jpeg;base64,...").
 * Directly decodes into a BitmapDrawable and returns DrawableResult,
 * completely bypassing external network calls and disk I/O.
 */
class DataUriFetcher(private val uriString: String, private val context: Context) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        return try {
            val base64Data = if (uriString.contains("base64,")) {
                uriString.substringAfter("base64,")
            } else {
                uriString
            }
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            DrawableResult(
                drawable = BitmapDrawable(context.resources, bitmap),
                isSampled = false,
                dataSource = DataSource.MEMORY
            )
        } catch (e: Throwable) {
            Log.e("DataUriFetcher", "Error decoding data URI: ${e.message}", e)
            null
        }
    }

    /**
     * Intercepts android.net.Uri with "data" scheme (Coil's StringMapper converts String -> Uri first).
     */
    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            val str = data.toString()
            if (data.scheme == "data" || str.startsWith("data:image")) {
                return DataUriFetcher(str, context)
            }
            return null
        }
    }

    /**
     * Fallback interceptor directly for raw String data URIs.
     */
    class StringFactory(private val context: Context) : Fetcher.Factory<String> {
        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.startsWith("data:image")) {
                return DataUriFetcher(data, context)
            }
            return null
        }
    }
}
