package com.devil.livetogather

import android.content.Context
import android.util.Base64
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer

class DataUriFetcher(private val uriString: String, private val context: Context) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        return try {
            val base64Data = uriString.substringAfter("base64,")
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            SourceResult(
                source = ImageSource(Buffer().write(bytes), context),
                mimeType = "image/jpeg",
                dataSource = DataSource.MEMORY
            )
        } catch (e: Exception) {
            null
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<String> {
        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.startsWith("data:image")) {
                return DataUriFetcher(data, context)
            }
            return null
        }
    }
}
