package com.gorkemoji.remindme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.gorkemoji.remindme.auth.BiometricActivity
import com.gorkemoji.remindme.auth.PasswordActivity
import com.gorkemoji.remindme.database.ToDo
import com.gorkemoji.remindme.database.ToDoDatabase
import com.gorkemoji.remindme.databinding.ActivityBackupRestoreBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupRestoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackupRestoreBinding
    private var isTransitioning = false

    private val database by lazy { ToDoDatabase.getDatabase(this) }

    private lateinit var requestWritePermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestReadPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* val isLocked = loadMode("is_locked", "auth") == "true"
        val biometricsEnabled = loadMode("biometrics", "auth") == "true"
        val passkeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (isLocked) navigateToAuthActivity(biometricsEnabled, passkeySet) */

        setupPermissionLaunchers()

        binding.btnBackup.setOnClickListener { handleBackup() }
        binding.btnRestore.setOnClickListener { handleRestore() }
    }

    /* private fun navigateToAuthActivity(biometricsEnabled: Boolean, passkeySet: Boolean) {
        isTransitioning = true
        var intent = Intent(this, PasswordActivity::class.java)

        if (biometricsEnabled && !passkeySet) intent = Intent(this, BiometricActivity::class.java)

        startActivity(intent)
    } */

    private fun setupPermissionLaunchers() {
        requestWritePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) result.data?.data?.let { uri -> backupData(uri) }
            // else showToast(resources.getString(R.string.permissions_denied))
        }

        requestReadPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) result.data?.data?.let { uri -> restoreData(uri) }
            // else showToast(resources.getString(R.string.permissions_denied))
        }
    }

    private fun handleBackup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requestMediaStoreWritePermission()
        else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) requestMediaStoreWritePermission()
            else ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
        }
    }

    private fun handleRestore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requestMediaStoreReadPermission()
        else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) requestMediaStoreReadPermission()
            else ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_PERMISSION)
        }
    }

    private fun requestMediaStoreWritePermission() {
        val currentTime = System.currentTimeMillis()
        val backupDate = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date(currentTime))
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, "tasks_backup_$backupDate.json") // Set filename with date
        }
        requestWritePermissionLauncher.launch(intent)
    }

    private fun requestMediaStoreReadPermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        requestReadPermissionLauncher.launch(intent)
    }

    private fun backupData(uri: Uri?) {
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    val tasks = database.getDao().getAllTasks()
                    val json = Gson().toJson(tasks)

                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            writeDataToOutputStream(outputStream, json)
                        }
                        showToast(resources.getString(R.string.backup_successful))
                    } ?: showToast(resources.getString(R.string.couldnt_create_file))
                } catch (e: Exception) { showToast(resources.getString(R.string.backup_failed) + " ${e.message}") }
            }
        }
    }

    private fun restoreData(uri: Uri?) {
        runBlocking {
            launch(Dispatchers.IO) {
                uri?.let { fileUri ->
                    try {
                        val inputStream = contentResolver.openInputStream(fileUri)
                        val json = inputStream?.bufferedReader().use { it?.readText() } ?: ""
                        val tasks = Gson().fromJson(json, Array<ToDo>::class.java).toList()
                        database.getDao().insertAll(tasks)

                        showToast(resources.getString(R.string.restore_successful))
                    } catch (e: Exception) { showToast(resources.getString(R.string.restore_failed) + " ${e.message}") }
                } ?: showToast(resources.getString(R.string.couldnt_open_file))
            }
        }
    }

    private fun writeDataToOutputStream(outputStream: OutputStream, data: String) {
        outputStream.write(data.toByteArray())
        outputStream.flush()
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        private const val REQUEST_WRITE_PERMISSION = 1
        private const val REQUEST_READ_PERMISSION = 2
    }

    override fun onStop() {
        super.onStop()
        if (!isTransitioning) saveLockState()
    }

    override fun onPause() {
        super.onPause()
        if (!isTransitioning) saveLockState()
    }

    override fun onResume() {
        super.onResume()

        if (loadMode("is_locked", "auth") == "true") {
            val intent = if (loadMode("biometrics", "auth") == "true") Intent(this, BiometricActivity::class.java)
            else Intent(this, PasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveLockState() {
        val isLocked = loadMode("is_locked", "auth") == "true"
        val isBiometricsEnabled = loadMode("biometrics", "auth") == "true"
        val isPasskeySet = !loadMode("passkey", "auth").isNullOrBlank()

        if (!isLocked && (isBiometricsEnabled || isPasskeySet)) saveMode("is_locked", "true", "auth")
    }

    private fun loadMode(type: String, file: String): String? {
        val pref: SharedPreferences = getSharedPreferences(file, Context.MODE_PRIVATE)
        return pref.getString(type, "")
    }

    private fun saveMode(type: String, data: String, file: String) {
        val pref: SharedPreferences = getSharedPreferences(file, Context.MODE_PRIVATE)
        with(pref.edit()) {
            putString(type, data)
            apply()
        }
    }
}