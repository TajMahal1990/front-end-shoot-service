package com.example.frame_extractions

// CameraService.kt
import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.io.File



class CameraService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var imageCapture: ImageCapture

    override fun onCreate() {
        super.onCreate()

        // Получаем текущий жизненный цикл приложения (или активности)
        val lifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get()

        // Инициализация камеры и запуск фоновой задачи
        startCamera(lifecycleOwner)
        handler.postDelayed(photoTask, 600_000) // каждые 10 минут
    }

    private val photoTask = object : Runnable {
        override fun run() {
            takePhoto()
            handler.postDelayed(this, 600_000) // каждые 10 минут
        }
    }

    private fun startCamera(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                // Инициализация ImageCapture для съемки фото
                imageCapture = ImageCapture.Builder().build()

                // Привязка камеры к жизненному циклу
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraService", "Ошибка при открытии камеры: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        try {
            val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.d("CameraService", "Фото сохранено в: ${photoFile.absolutePath}")
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraService", "Ошибка при сохранении фото: ${exception.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("CameraService", "Ошибка при попытке сделать фото: ${e.message}")

            // Обработка ошибки: пробуем сделать фото через альтернативный процесс
            fallbackPhotoCapture()
        }
    }

    private fun fallbackPhotoCapture() {
        Log.d("CameraService", "Попытка сохранить фото через резервный метод")
        // Здесь вы можете добавить свой альтернативный способ сохранения данных
        // Например, сохранить "пустое" фото или логи
        val fallbackPhotoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "fallback_photo_${System.currentTimeMillis()}.txt")
        fallbackPhotoFile.writeText("Ошибка при сохранении фото, резервная запись")
        Log.d("CameraService", "Резервная запись сохранена: ${fallbackPhotoFile.absolutePath}")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(photoTask) // Остановка фоновой задачи
    }
}
