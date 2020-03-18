package com.me.bui.myappp

import android.R
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.me.bui.myappp.Prefs.INSTALLED_VERSION
import com.me.bui.myappp.Prefs.edit


class OnUpgrade : BroadcastReceiver() {
    val ACTION_UPGRADE_NOTIFICATION_OPENED = "ACTION_UPGRADE_NOTIFICATION_OPENED"
    val ACTION_UPGRADE_NOTIFICATION_OPT_OUT = "ACTION_UPGRADE_NOTIFICATION_OPT_OUT"
    val UPGRADE_NOTIFICATION_ID = 1

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.e("OnUpgrade", " ----------- ${p1?.action}")
        when(p1?.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.e("OnUpgrade", "-------------- upgraded new version MY_PACKAGE_REPLACED")
                onUpgraded(p0!!, BuildConfig.VERSION_CODE)
            }
            ACTION_UPGRADE_NOTIFICATION_OPENED -> {
                Log.e("OnUpgrade", "-------------- upgraded new version ACTION_UPGRADE_NOTIFICATION_OPENED")
                cancelNotification(p0!!)
                val showWhatsNewIntent = Intent(
                    p0, MainActivity::class.java
                )
                showWhatsNewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                showWhatsNewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                showWhatsNewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                p0.startActivity(showWhatsNewIntent)
            }
            ACTION_UPGRADE_NOTIFICATION_OPT_OUT -> {
                Log.e("OnUpgrade", "-------------- upgraded new version ACTION_UPGRADE_NOTIFICATION_OPT_OUT")
                cancelNotification(p0!!)
                Prefs.edit(p0).putBoolean(Prefs.NOTIFY_ON_UPGRADES, false).apply()
                Toast.makeText(p0, "upgrade_notification_turned_off", Toast.LENGTH_LONG)
                    .show()

    //            val showSettingsIntent = Intent(
    //                Constants.Actions.ACTION_SHOW_SETTINGS, null /* uri */,
    //                p0, MainActivity::class.java
    //            )
    //            showSettingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    //            p0?.startActivity(showSettingsIntent)
            }
        }
    }

    fun onUpgraded(context: Context, versionCode: Int) {
        notifyOnUpgraded(context, versionCode)
        maybeShowSnackbarOnNextAppLaunch(context, versionCode)
//        Prefs.edit(context).putInt(Prefs.INSTALLED_VERSION, versionCode).apply()
    }

    private fun notifyOnUpgraded(context: Context, versionCode: Int) {
        val openedPendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(ACTION_UPGRADE_NOTIFICATION_OPENED),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val optOutPendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(ACTION_UPGRADE_NOTIFICATION_OPT_OUT),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val upgradeNotificationBuilder = NotificationCompat.Builder(context, MyApp.NOTIFICATION_CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.ic_btn_speak_now)
            .setAutoCancel(true)
            .setContentTitle("new_features")
            .setContentText("app_version")
//            .setContentIntent(openedPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_media_play,
                    "what news",
                    openedPendingIntent
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_delete,
                    "don't show",
                    optOutPendingIntent
                )
            )

        with(NotificationManagerCompat.from(context)) {
            notify(UPGRADE_NOTIFICATION_ID,
            upgradeNotificationBuilder.build())
        }
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(UPGRADE_NOTIFICATION_ID)
    }

    fun maybeShowSnackbarOnNextAppLaunch(context: Context, versionCode: Int) {
        val lastVersionCode = Prefs.get(context)?.getInt(INSTALLED_VERSION, 0)!!
        if(versionCode > lastVersionCode)
            Prefs.edit(context).putBoolean(Prefs.SHOW_WHATS_NEW_ON_NEXT_LAUNCH, true).commit();
    }
}