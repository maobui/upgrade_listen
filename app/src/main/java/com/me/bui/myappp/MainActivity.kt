package com.me.bui.myappp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity(), View.OnClickListener {
    val REQUEST_INSTALL_UNKNOW_SOURCES_PERMISSION = 1234
    val REQUEST_STORAGE_PERMISSION = 1235

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (hasAppPermission()) {
            initApp()
        } else {
            checkAppPermission()
        }
    }

    private fun initApp() {
        button.setOnClickListener(this)
        Prefs.edit(this).putInt(Prefs.INSTALLED_VERSION, BuildConfig.VERSION_CODE).apply()
        showNotifyAfterUpgraded()
    }

    private fun checkAppPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
            Toast.makeText(this, "Permisson require!!", Toast.LENGTH_LONG).show()
        else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
    }

    fun hasAppPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    initApp()
                else
                    checkAppPermission()
                return
            }
            else -> {

            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.button -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!packageManager.canRequestPackageInstalls()) {
                        startActivityForResult(
                            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                .setData(Uri.parse(String.format("package:%s", packageName))),
                            REQUEST_INSTALL_UNKNOW_SOURCES_PERMISSION
                        )
                    } else {
                        installApp()
                    }
                } else {
                    installApp()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INSTALL_UNKNOW_SOURCES_PERMISSION && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (packageManager.canRequestPackageInstalls()) {
                    installApp()
                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
                }
            } else {
                installApp()
            }
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        }
    }

    fun installApp() {
        val toInstall = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "/app-debug.apk"
        );
        val install = /*Intent(Intent.ACTION_INSTALL_PACKAGE)*/Intent(Intent.ACTION_VIEW)
        install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        install.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val apkUri: Uri = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID + ".file_provider",
            toInstall
        )
        install.setDataAndType(apkUri, "application/vnd.android.package-archive")
        startActivity(install)
    }

    fun showNotifyAfterUpgraded() {
        if (Prefs.get(this)!!.getBoolean(Prefs.SHOW_WHATS_NEW_ON_NEXT_LAUNCH, false)) {
            val snackbar: Snackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    "New version",
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction("Ok", View.OnClickListener { })
            snackbar.show()
            Prefs.edit(this).putBoolean(Prefs.SHOW_WHATS_NEW_ON_NEXT_LAUNCH, false).apply()
        }
    }

}
