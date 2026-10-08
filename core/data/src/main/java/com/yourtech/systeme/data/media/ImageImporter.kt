package com.yourtech.systeme.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Secure image intake for uploads (request photos, products, portfolio):
 *  - only image/jpeg, image/png, image/webp, image/heic(f) are accepted, max 20 MB;
 *  - the image is fully decoded and re-encoded as JPEG (max 1600 px), which drops EXIF/GPS
 *    metadata and any non-image payload;
 *  - files are stored in app-private storage with random names.
 */
@Singleton
class ImageImporter @Inject constructor(@ApplicationContext private val context: Context) {

    sealed interface Result {
        data class Ok(val path: String) : Result
        data object UnsupportedType : Result
        data object TooLarge : Result
        data object Unreadable : Result
    }

    suspend fun import(uri: Uri, folder: String): Result = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri)
        if (mime != null && mime !in ALLOWED) return@withContext Result.UnsupportedType
        val size = runCatching { resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } }.getOrNull() ?: -1L
        if (size > MAX_BYTES) return@withContext Result.TooLarge
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return@withContext Result.Unreadable
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext Result.Unreadable
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return@withContext Result.Unreadable
        val scaled = scaleDown(bitmap)
        val dir = File(context.filesDir, "images/$folder").apply { mkdirs() }
        val out = File(dir, UUID.randomUUID().toString() + ".jpg")
        out.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()
        Result.Ok(out.absolutePath)
    }

    fun delete(path: String?) {
        val file = path?.let(::File) ?: return
        // Only ever delete inside our own images folder.
        if (file.canonicalPath.startsWith(File(context.filesDir, "images").canonicalPath)) file.delete()
    }

    private fun scaleDown(b: Bitmap): Bitmap {
        val longest = maxOf(b.width, b.height)
        if (longest <= MAX_SIDE) return b
        val f = MAX_SIDE.toFloat() / longest
        return Bitmap.createScaledBitmap(b, (b.width * f).toInt(), (b.height * f).toInt(), true)
    }

    companion object {
        const val MAX_SIDE = 1600
        const val MAX_BYTES = 20L * 1024 * 1024
        val ALLOWED = setOf("image/jpeg", "image/png", "image/webp", "image/heic", "image/heif")
    }
}
