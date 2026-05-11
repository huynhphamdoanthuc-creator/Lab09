package com.example.blur_o_maticapp.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString("KEY_IMAGE_URI")
        val blurLevel = inputData.getInt("KEY_BLUR_LEVEL", 1)

        makeStatusNotification("Blurring image", appContext)

        return try {
            if (resourceUri.isNullOrEmpty()) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver
            var picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )

            // Apply blur multiple times based on the level
            repeat(blurLevel) {
                picture = blurBitmap(picture, appContext)
            }

            // Write bitmap to a temp file
            val outputUri = writeBitmapToFile(appContext, picture)

            makeStatusNotification("Output is $outputUri", appContext)

            val outputData = workDataOf("KEY_IMAGE_URI" to outputUri.toString())

            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error applying blur", throwable)
            Result.failure()
        }
    }
}
