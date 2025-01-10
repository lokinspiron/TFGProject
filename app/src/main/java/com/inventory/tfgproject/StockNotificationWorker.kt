package com.inventory.tfgproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class StockNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "stock_notifications"
        const val NOTIFICATION_ID = 1
        const val PRODUCT_NAME_KEY = "product_name"

        fun scheduleNotification(context: Context, productName: String) {
            createNotificationChannel(context)

            val inputData = Data.Builder()
                .putString(PRODUCT_NAME_KEY, productName)
                .build()

            val notificationWork = OneTimeWorkRequestBuilder<StockNotificationWorker>()
                .setInputData(inputData)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "stock_notification_$productName",
                    ExistingWorkPolicy.REPLACE,
                    notificationWork
                )
        }

        private fun createNotificationChannel(context: Context) {
            val name = "Stock Notifications"
            val descriptionText = "Notifications for stock updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun doWork(): Result {
        val productName = inputData.getString(PRODUCT_NAME_KEY) ?: return Result.failure()
        showNotification(productName)
        return Result.success()
    }

    private fun showNotification(productName: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Stock Agotado")
            .setContentText("El producto $productName se ha quedado sin stock")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}