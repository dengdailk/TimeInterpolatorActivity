package com.study.timeinterpolator

import android.animation.TimeInterpolator

/**
 * @description 数据结构
 */
data class TimeInterpolatorBean(
    var isSelect: Boolean,
    val name: String,
    val timeInterpolator: TimeInterpolator
)