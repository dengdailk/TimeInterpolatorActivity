package com.study.timeinterpolator

import android.animation.TimeInterpolator
import kotlin.math.pow
import kotlin.math.sin

/**
 * @description 震旦效果
 */
class SpringInterpolator : TimeInterpolator {
    /**
     * 参数 x，即为 x轴的值
     * 返回值 便是 y 轴的值
     */
    override fun getInterpolation(x: Float): Float {
        val factor = 0.4f
        return (2.0.pow(-10 * x.toDouble()) * sin((x - factor / 4) * (2 * Math.PI) / factor) + 1).toFloat()
    }
}