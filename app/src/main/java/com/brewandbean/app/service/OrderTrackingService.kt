package com.brewandbean.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.brewandbean.app.R
import com.brewandbean.app.data.repository.MenuRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderTrackingService : Service() {

    @Inject
    lateinit var repository: MenuRepository

    @Inject
    lateinit var authRepository: com.brewandbean.app.data.repository.AuthRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val CHANNEL_ID_SILENT = "ORDER_TRACKING_SILENT"
        const val CHANNEL_ID_ALERT = "ORDER_TRACKING_ALERT"
        const val NOTIFICATION_ID_BG = 1001
        const val NOTIFICATION_ID_READY = 1002
        const val EXTRA_TOKEN = "customer_token"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra(EXTRA_TOKEN) ?: return START_NOT_STICKY
        val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value

        // Arka plan servisi her zaman sessiz kanalda calismali
        startForeground(NOTIFICATION_ID_BG, buildSilentNotification(if(isEn) "Order received, awaiting confirmation..." else "Siparisiniz alindi, onay bekleniyor..."))
        startPolling(token)

        return START_STICKY
    }

    private fun startPolling(token: String) {
        serviceScope.launch {
            var isReadyNotified = false
            var lastStatus = ""
            while (isActive) {
                delay(4000)
                try {
                    val statusRes = repository.getOrderStatus(token)
                    val status = statusRes.status ?: continue
                    val name = statusRes.customerName?.takeIf { it.isNotBlank() } ?: statusRes.orderNo ?: "Siparisiniz"

                    if (status == lastStatus) continue // Durum degismediyse bildirim spamini engelle
                    lastStatus = status

                    when (status) {
                        "hazirlaniyor" -> {
                            updateSilentNotification(if(com.brewandbean.app.util.LanguageManager.isEnglish.value) "$name, your order is preparing..." else "$name, siparisiniz hazirlaniyor...")
                        }
                        "hazir" -> {
                            if (!isReadyNotified) {
                                // Arka plan bildirimini sessizce guncelle
                                updateSilentNotification(if(com.brewandbean.app.util.LanguageManager.isEnglish.value) "$name, your order is ready!" else "$name, siparisiniz hazir!")
                                // Kullaniciyi uyarmak icin YENI sesli bir bildirim firlat
                                fireReadyAlert(if(com.brewandbean.app.util.LanguageManager.isEnglish.value) "$name, your coffee is ready! Please pick it up from the barista. ☕" else "$name, kahveniz hazir! Lutfen baristadan teslim alin. ☕")
                                isReadyNotified = true
                            }
                        }
                        "teslim_edildi" -> {
                            // Tum sesli/sessiz bildirimleri ekrandan temizle
                            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancelAll()
                            
                            // Profildeki yildizlari guncelle (UI otomatik yenilenecek)
                            authRepository.fetchAndSaveProfile()
                            
                            // Servisi bitir ve arkaplan bildirimini kaldir
                            stopForeground(true)
                            stopSelf()
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    private fun updateSilentNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_BG, buildSilentNotification(text))
    }

    private fun buildSilentNotification(text: String): Notification {
        val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value
        return NotificationCompat.Builder(this, CHANNEL_ID_SILENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if(isEn) "Order Tracking" else "Siparis Takibi")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun fireReadyAlert(text: String) {
        val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alert = NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if(isEn) "Your Order is Ready! ☕" else "Siparisiniz Hazir! ☕")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID_READY, alert)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            
            // Sessiz kanal (Foreground service icin mecburi, ses/titresim yok)
            val silentChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                "Arkaplan Takibi",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(silentChannel)

            // Sesli kanal (Siparis hazir oldugunda uyaracak)
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERT,
                "Siparis Uyarilari",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(alertChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
