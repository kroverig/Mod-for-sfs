package com.kroverig.sfsmarker
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
class MainActivity : Activity() {
    private lateinit var nameInput: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(28,28,28,28) }
        root.addView(TextView(this).apply { text="SFS Marker — локальная сеть"; textSize=23f })
        root.addView(TextView(this).apply { text="Подключите телефоны к одной Wi-Fi сети. На каждом нажмите запуск."; textSize=16f; setPadding(0,18,0,18) })
        nameInput = EditText(this).apply { hint="Имя игрока"; setSingleLine(); setText("Игрок") }
        root.addView(nameInput, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(Button(this).apply { text="Запустить маркеры"; setOnClickListener { startMarkers() } })
        root.addView(Button(this).apply { text="Остановить маркеры"; setOnClickListener { stopService(Intent(this@MainActivity, MarkerService::class.java)) } })
        root.addView(TextView(this).apply { text="Это отдельные плавающие маркеры. Автоматического чтения координат ракет SFS пока нет."; textSize=14f; setPadding(0,20,0,0) })
        setContentView(root)
    }
    private fun startMarkers() {
        val name=nameInput.text.toString().trim().take(18).ifBlank { "Игрок" }
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            Toast.makeText(this,"Разреши показ поверх других приложений, затем снова нажми запуск.",Toast.LENGTH_LONG).show()
            return
        }
        val i=Intent(this,MarkerService::class.java).putExtra("playerName",name)
        if (Build.VERSION.SDK_INT>=26) startForegroundService(i) else startService(i)
        Toast.makeText(this,"Маркеры запускаются",Toast.LENGTH_SHORT).show()
    }
}
