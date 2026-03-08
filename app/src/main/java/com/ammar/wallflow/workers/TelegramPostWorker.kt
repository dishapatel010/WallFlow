package com.ammar.wallflow.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ammar.wallflow.IoDispatcher
import com.ammar.wallflow.data.preferences.TelegramPreferences
import com.ammar.wallflow.data.repository.AppPreferencesRepository
import com.ammar.wallflow.extensions.TAG
import com.ammar.wallflow.extensions.workManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

@HiltWorker
class TelegramPostWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val okHttpClient: OkHttpClient,
) : CoroutineWorker(context, params) {

    override suspend fun doWork() = withContext(ioDispatcher) {
        val fileUriStr = inputData.getString(INPUT_KEY_FILE_URI)
        if (fileUriStr.isNullOrBlank()) {
            return@withContext Result.failure(workDataOf(OUTPUT_KEY_ERROR to "Missing file URI"))
        }
        val fileUri = Uri.parse(fileUriStr)

        val prefs = appPreferencesRepository.appPreferencesFlow.first().telegramPreferences
        if (!prefs.enabled || !prefs.isConfigured) {
            return@withContext Result.success(workDataOf(OUTPUT_KEY_SKIPPED to true))
        }

        val fileBytes = readFileBytes(fileUri) ?: run {
            Log.e(TAG, "TelegramPostWorker: Could not read file from $fileUri")
            return@withContext Result.failure(workDataOf(OUTPUT_KEY_ERROR to "Could not read file"))
        }

        val fileName = inputData.getString(INPUT_KEY_FILE_NAME) ?: fileUri.lastPathSegment ?: "image.jpg"
        val caption = buildCaption(
            prefs = prefs,
            fileName = fileName,
            tags = inputData.getStringArray(INPUT_KEY_TAGS)?.joinToString(", "),
            source = inputData.getString(INPUT_KEY_SOURCE),
            sourceUrl = inputData.getString(INPUT_KEY_SOURCE_URL),
        )

        val success = postToTelegram(
            prefs = prefs,
            fileBytes = fileBytes,
            fileName = fileName,
            caption = caption,
        )
        if (success) {
            Result.success(workDataOf(OUTPUT_KEY_SUCCESS to true))
        } else {
            Result.failure(workDataOf(OUTPUT_KEY_ERROR to "Upload failed after retries"))
        }
    }

    private fun readFileBytes(uri: Uri): ByteArray? {
        return when (uri.scheme) {
            "file" -> uri.path?.let { path -> java.io.File(path).takeIf { it.exists() && it.canRead() }?.readBytes() }
            "content" -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            else -> null
        }
    }

    private fun buildCaption(
        prefs: TelegramPreferences,
        fileName: String,
        tags: String?,
        source: String?,
        sourceUrl: String?,
    ): String {
        val currentDateTime = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault(),
        ).format(java.util.Date())
        return buildString {
            if (prefs.includeFileName) append("File: $fileName\n")
            if (prefs.includeDate) append("Downloaded on: $currentDateTime\n")
            if (prefs.includeTags && !tags.isNullOrBlank()) append("Tags: $tags\n")
            if (prefs.includeSourceUrl && source != null && sourceUrl != null) append("$source : $sourceUrl")
        }.trimEnd()
    }

    private suspend fun postToTelegram(
        prefs: TelegramPreferences,
        fileBytes: ByteArray,
        fileName: String,
        caption: String,
    ): Boolean {
        val maxPhotoSize = 10 * 1024 * 1024 // 10 MB
        val maxDocumentSize = 50 * 1024 * 1024 // 50 MB
        if (fileBytes.size > maxDocumentSize) {
            Log.e(TAG, "File size exceeds 50 MB, skipping Telegram upload")
            return false
        }

        val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", prefs.chatId)
            .apply {
                if (prefs.messageThreadId.isNotBlank()) addFormDataPart("message_thread_id", prefs.messageThreadId)
                if (fileBytes.size <= maxPhotoSize) {
                    addFormDataPart(
                        "photo",
                        fileName,
                        fileBytes.toRequestBody(mimeType.toMediaTypeOrNull()),
                    )
                } else {
                    addFormDataPart(
                        "document",
                        fileName,
                        fileBytes.toRequestBody(mimeType.toMediaTypeOrNull()),
                    )
                }
                if (caption.isNotBlank()) addFormDataPart("caption", caption)
                if (prefs.silentNotification) addFormDataPart("disable_notification", "true")
                if (prefs.disableWebPagePreview) addFormDataPart("disable_web_page_preview", "true")
            }
            .build()

        val endpoint = if (fileBytes.size <= maxPhotoSize) "Photo" else "Document"
        val request = Request.Builder()
            .url("https://api.telegram.org/bot${prefs.botToken}/send$endpoint")
            .post(requestBody)
            .build()

        val clientWithTimeout = okHttpClient.newBuilder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val maxRetries = 3
        var attempt = 0
        while (attempt < maxRetries) {
            try {
                val response = clientWithTimeout.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.i(TAG, "Image successfully posted to Telegram")
                    return true
                }
                Log.e(TAG, "Failed to post to Telegram: ${response.code} - ${response.message}")
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxRetries) {
                    Log.e(TAG, "Failed to post to Telegram after $maxRetries attempts", e)
                    return false
                }
                Log.w(TAG, "Retrying Telegram upload, attempt $attempt", e)
                delay((2000 * attempt).toLong())
            }
        }
        return false
    }

    companion object {
        const val INPUT_KEY_FILE_URI = "file_uri"
        const val INPUT_KEY_FILE_NAME = "file_name"
        const val INPUT_KEY_TAGS = "tags"
        const val INPUT_KEY_SOURCE = "source"
        const val INPUT_KEY_SOURCE_URL = "source_url"
        const val OUTPUT_KEY_ERROR = "error"
        const val OUTPUT_KEY_SUCCESS = "success"
        const val OUTPUT_KEY_SKIPPED = "skipped"

        fun enqueue(
            context: Context,
            fileUri: Uri,
            fileName: String? = null,
            tags: Array<String>? = null,
            source: String? = null,
            sourceUrl: String? = null,
        ) {
            val dataBuilder = androidx.work.Data.Builder()
                .putString(INPUT_KEY_FILE_URI, fileUri.toString())
                .putString(INPUT_KEY_FILE_NAME, fileName ?: fileUri.lastPathSegment ?: "image.jpg")
            if (!tags.isNullOrEmpty()) dataBuilder.putStringArray(INPUT_KEY_TAGS, tags)
            if (source != null) dataBuilder.putString(INPUT_KEY_SOURCE, source)
            if (sourceUrl != null) dataBuilder.putString(INPUT_KEY_SOURCE_URL, sourceUrl)
            val request = androidx.work.OneTimeWorkRequestBuilder<TelegramPostWorker>()
                .setInputData(dataBuilder.build())
                .build()
            context.workManager.enqueue(request)
        }
    }
}
