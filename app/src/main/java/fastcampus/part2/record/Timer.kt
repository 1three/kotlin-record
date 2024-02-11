package fastcampus.part2.record

import android.os.Handler
import android.os.Looper

class Timer(listener: OnTimerTickListener) {
    private var duration = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            duration += 20L
            handler.postDelayed(this, 20L) // 100L마다 무한 루프
            listener.onTick(duration)
        }
    }

    fun start() {
        handler.postDelayed(runnable, 20L)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
        duration = 0
    }
}

interface OnTimerTickListener {
    fun onTick(duration: Long)
}