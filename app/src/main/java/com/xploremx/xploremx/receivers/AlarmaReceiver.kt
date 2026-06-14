package com.xploremx.xploremx.receivers

/*
 * CORRECCIÓN: antes creaba el canal "alarmas" localmente con una
 * cadena literal. Ahora importa CANAL_ALARMAS_ID desde AlarmasFragment
 * para garantizar que ambos usen EXACTAMENTE el mismo canal.
 * Si el canal no existe cuando llega el broadcast, la notificación
 * se pierde silenciosamente — este era el motivo por el que no sonaba.
 */

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.xploremx.xploremx.R
import com.xploremx.xploremx.fragments.CANAL_ALARMAS_ID
import com.xploremx.xploremx.fragments.CANAL_ALARMAS_NAME

class AlarmaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Recordatorio"

        val manager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ALARMAS_ID,
                CANAL_ALARMAS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(canal)
        }

        val notification = NotificationCompat.Builder(context, CANAL_ALARMAS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Recordatorio!!!")
            .setContentText(titulo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}