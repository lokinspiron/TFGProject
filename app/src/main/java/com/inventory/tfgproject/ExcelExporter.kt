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
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExcelExporter(private val context: Context) {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportProducts(products: List<Product>): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Productos")

            val headerRow = sheet.createRow(0)
            val headers = arrayOf("Nombre", "Stock", "Precio","Unidad")
            headers.forEachIndexed { index, header ->
                createCell(headerRow, index, header)
            }

            products.forEachIndexed { index, product ->
                val row = sheet.createRow(index + 1)
                createCell(row, 0, product.name)
                createCell(row, 1, product.stock.toString())
                createCell(row, 2, product.price.toString())
                createCell(row, 3, product.currencyUnit)
            }

            val byteStream = ByteArrayOutputStream()
            workbook.write(byteStream)
            val byteArray = byteStream.toByteArray()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Inventario_$timestamp.xlsx"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { documentUri ->
                resolver.openOutputStream(documentUri)?.use { outputStream ->
                    outputStream.write(byteArray)
                }
                showNotification(context, "Archivo guardado", "El archivo se guardó en Descargas/$fileName")
                workbook.close()
                true
            } ?: false

        } catch (e: Exception) {
            Log.e("ExcelExporter", "Error exporting to excel", e)
            false
        }
    }

    private fun createCell(row: Row, index: Int, value: String) {
        val cell: Cell = row.createCell(index)
        cell.setCellValue(value)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "excel_export_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Excel Export",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}