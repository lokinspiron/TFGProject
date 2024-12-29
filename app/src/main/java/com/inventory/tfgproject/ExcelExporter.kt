package com.inventory.tfgproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.inventory.tfgproject.model.Product
import org.apache.poi.hssf.usermodel.HSSFWorkbook

class ExcelExporter(private val context: Context) {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportProducts(products: List<Product>): Boolean {
        return try {
            val workbook = HSSFWorkbook()
            val sheet = workbook.createSheet("Productos")

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("Nombre", "Stock", "Precio")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }

            // Populate data rows
            products.forEachIndexed { index, product ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(product.name)
                row.createCell(1).setCellValue(product.stock.toDouble())
                row.createCell(2).setCellValue(product.price)
            }

            for (i in headers.indices) {
                sheet.autoSizeColumn(i)
            }

            val fileName = "Productos_${System.currentTimeMillis()}.xls"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { documentUri ->
                resolver.openOutputStream(documentUri)?.use { outputStream ->
                    workbook.write(outputStream)
                }
                showNotification(context, "Archivo guardado", "El archivo se guardó en Descargas/$fileName")
                true
            } ?: false

        } catch (e: Exception) {
            Log.e("ExcelExporter", "Error exporting to excel", e)
            false
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "excel_export_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Excel Export",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}