package com.example.blur_o_maticapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.blur_o_maticapp.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

private const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
private const val CHANNEL_NAME = "Verbose WorkManager Notifications"
private const val CHANNEL_DESCRIPTION = "Shows notifications whenever work starts"
private const val NOTIFICATION_TITLE = "WorkRequest Starting"
private const val NOTIFICATION_ID = 1

fun makeStatusNotification(message: String, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        channel.description = CHANNEL_DESCRIPTION

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_menu_info_details)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

fun blurBitmap(bitmap: Bitmap, context: Context): Bitmap {
    // This is a simplified blur for the sake of the example
    // In a real lab, you might use RenderScript or a library
    return Bitmap.createScaledBitmap(
        bitmap,
        bitmap.width / 4,
        bitmap.height / 4,
        true
    )
}

@Throws(IOException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    val name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString())
    val outputDir = File(applicationContext.filesDir, "blur_filter_outputs")
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    val outputFile = File(outputDir, name)
    var out: FileOutputStream? = null
    try {
        out = FileOutputStream(outputFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
    } finally {
        out?.close()
    }
    return Uri.fromFile(outputFile)
}
