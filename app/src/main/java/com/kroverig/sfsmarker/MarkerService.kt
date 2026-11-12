package com.kroverig.sfsmarker
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.TextView
import java.net.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class MarkerService : Service() {
    private val port=45872
    private val players=ConcurrentHashMap<String,Long>()
    @Volatile private var running=false
    @Volatile private var myName="Игрок"
    private var markerX=0.5f
    private var markerY=0.5f
    private var socket:DatagramSocket?=null
    private var overlay:TextView?=null
    private var wm:WindowManager?=null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT>=26) {
            val ch=NotificationChannel("sfs_marker","SFS Marker",NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        val nb=if (Build.VERSION.SDK_INT>=26) Notification.Builder(this,"sfs_marker") else Notification.Builder(this)
        startForeground(1,nb.setContentTitle("SFS Marker работает").setContentText("Поиск игроков в локальной Wi-Fi сети").setSmallIcon(android.R.drawable.ic_menu_mylocation).build())
        wm=getSystemService(WINDOW_SERVICE) as WindowManager
        addOverlay()
        startNetwork()
    }
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        myName=intent?.getStringExtra("playerName") ?: myName
        refresh()
        return START_STICKY
    }
    private fun addOverlay() {
        val v=TextView(this).apply { textSize=14f; setTextColor(Color.WHITE); setBackgroundColor(0xCC172638.toInt()); setPadding(16,12,16,12); text="SFS Marker\nПеретащи меня" }
        val type=if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val p=WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT)
        p.gravity=Gravity.TOP or Gravity.START; p.x=60; p.y=160
        v.setOnTouchListener(object:View.OnTouchListener {
            var sx=0f; var sy=0f; var ox=0; var oy=0
            override fun onTouch(view:View,event:MotionEvent):Boolean {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> { sx=event.rawX; sy=event.rawY; ox=p.x; oy=p.y; return true }
                    MotionEvent.ACTION_MOVE -> {
                        p.x=ox+(event.rawX-sx).toInt(); p.y=oy+(event.rawY-sy).toInt()
                        markerX=(p.x.toFloat()/resources.displayMetrics.widthPixels).coerceIn(0f,1f)
                        markerY=(p.y.toFloat()/resources.displayMetrics.heightPixels).coerceIn(0f,1f)
                        try { wm?.updateViewLayout(v,p) } catch (_:Exception) {}
                        return true
                    }
                }
                return true
            }
        })
        overlay=v
        try { wm?.addView(v,p) } catch (_:Exception) { stopSelf() }
    }
    private fun startNetwork() {
        running=true
        thread(name="marker-udp") {
            try {
                val s=DatagramSocket(null).apply { reuseAddress=true; broadcast=true; bind(InetSocketAddress(port)); soTimeout=500 }
                socket=s
                val buf=ByteArray(512)
                while(running) {
                    try {
                        val msg="$myName|$markerX|$markerY".toByteArray(Charsets.UTF_8)
                        s.send(DatagramPacket(msg,msg.size,InetAddress.getByName("255.255.255.255"),port))
                    } catch (_:Exception) {}
                    try {
                        val packet=DatagramPacket(buf,buf.size); s.receive(packet)
                        val fields=String(packet.data,0,packet.length,Charsets.UTF_8).split("|")
                        if(fields.size==3 && fields[0]!=myName) players[fields[0]]=System.currentTimeMillis()
                    } catch (_:SocketTimeoutException) {}
                    val now=System.currentTimeMillis()
                    players.entries.removeIf { now-it.value>5000 }
                    refresh()
                    Thread.sleep(350)
                }
                s.close()
            } catch (_:Exception) { refresh() }
        }
    }
    @Synchronized private fun refresh() {
        val names=players.keys.joinToString("\n") { "🚀 $it" }
        runOnUiThread { overlay?.text="SFS Marker: $myName\nПеретащи этот блок\n"+if(names.isEmpty()) "Ищем игроков в Wi-Fi…" else names }
    }
    override fun onDestroy() {
        running=false; socket?.close()
        try { overlay?.let { wm?.removeView(it) } } catch (_:Exception) {}
        overlay=null
        super.onDestroy()
    }
    override fun onBind(intent:Intent?):IBinder?=null
}
