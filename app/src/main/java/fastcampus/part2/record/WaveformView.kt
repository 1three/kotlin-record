package fastcampus.part2.record

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

// @JvmOverloads
// JVM에서 Kotlin 함수를 오버로드하게 해주는 어노테이션
//
// 함수의 매개변수에 기본값을 제공할 수 있으며,
// 이 함수를 Java에서 사용할 때 필요한 매개변수만 선택적으로 제공할 수 있다.

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ampList = mutableListOf<Float>() // 원시 데이터 저장
    private val rectList = mutableListOf<RectF>() // onDraw 를 통해 그리는 데이터 저장
    private val purplePaint = Paint().apply { color = ContextCompat.getColor(context, R.color.purple) }
    private val rectWidth = 15f // 파형 너비
    private var tick = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (rectF in rectList) {
            canvas.drawRect(rectF, purplePaint)
        }
    }

    fun addAmplitude(maxAmplitude: Float) {
        val amplitudeRatio = 0.8f
        val amplitude = (maxAmplitude / Short.MAX_VALUE) * this.height * amplitudeRatio

        ampList.add(amplitude) // 원시 데이터 쌓이며 저장
        rectList.clear() // rectList 초기화

        val maxRect = this.width / rectWidth
        val amps = ampList.takeLast(maxRect.toInt()) // 화면에 보여지는 View 최신으로 제한

        for ((i, amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = this.height / 2 - amp / 2 + 5
            rectF.bottom = rectF.top + amp + 5
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth - 5f

            rectList.add(rectF)
        }
        invalidate() // UI 초기화
    }

    fun replayAmplitude() {
        rectList.clear()

        val maxRect = this.width / rectWidth
        val amps = ampList.take(tick).takeLast(maxRect.toInt())

        for ((i, amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = this.height / 2 - amp / 2 + 5
            rectF.bottom = rectF.top + amp + 5
            rectF.left = i * rectWidth
            rectF.right = rectF.left + rectWidth - 5f

            rectList.add(rectF)
        }

        tick++
        invalidate()
    }

    fun clearData() {
        ampList.clear()
    }

    fun clearWave() {
        rectList.clear()
        tick = 0
        invalidate()
    }
}