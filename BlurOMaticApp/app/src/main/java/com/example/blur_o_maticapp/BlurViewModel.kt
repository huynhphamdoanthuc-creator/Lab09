package com.example.blur_o_maticapp

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.blur_o_maticapp.workers.BlurWorker

class BlurViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)
    
    // Lưu trữ thông tin về các công việc đang chạy
    internal val outputWorkInfos: LiveData<List<WorkInfo>> = 
        workManager.getWorkInfosByTagLiveData("TAG_OUTPUT")

    private fun createInputDataForUri(uri: Uri, blurLevel: Int): Data {
        val builder = Data.Builder()
        builder.putString("KEY_IMAGE_URI", uri.toString())
        builder.putInt("KEY_BLUR_LEVEL", blurLevel)
        return builder.build()
    }

    internal fun applyBlur(imageUri: Uri, blurLevel: Int) {
        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(createInputDataForUri(imageUri, blurLevel))
            .addTag("TAG_OUTPUT") // Thêm tag để dễ dàng theo dõi
            .build()
            
        workManager.enqueueUniqueWork(
            "IMAGE_MANIPULATION_WORK_NAME",
            ExistingWorkPolicy.REPLACE,
            blurRequest
        )
    }

    internal fun cancelWork() {
        workManager.cancelUniqueWork("IMAGE_MANIPULATION_WORK_NAME")
        workManager.pruneWork()
    }
}
