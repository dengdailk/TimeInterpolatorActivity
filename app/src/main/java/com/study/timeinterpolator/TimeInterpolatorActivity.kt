package com.study.timeinterpolator

import android.animation.*
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_time_interpolator.*


class TimeInterpolatorActivity : AppCompatActivity(), TimeInterpolatorAdapter.ClickListener {
    // 取1000帧
    private val frame = 1000


    private val dataList: MutableList<PointF> = ArrayList()
    private val interpolatorList: MutableList<TimeInterpolatorBean> = ArrayList()

    private var mInterpolator: TimeInterpolator? = null

    private var mAdapter: TimeInterpolatorAdapter? = null

    private var mAnimator: ObjectAnimator? = null
    private var mXAnimator: ValueAnimator? = null

    private var animatorSet: AnimatorSet? = null

    private var isRunning = false

    private var curPoint: PointF? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_interpolator)
        isRunning = false

        curPoint = PointF(0F, 0F)

        buildInterpolatorList()
        createData()

        val start: Float = dpToPx(this, 35F)
        val end: Float =
            getScreenHeight(this) - dpToPx(this, 35F + 50F) - getStatusHeight(this)

        mAnimator = ObjectAnimator.ofFloat(anim_view, "y", start, end)
        mXAnimator = ValueAnimator.ofFloat(0f, 1f)
        animatorSet = AnimatorSet()
        animatorSet!!.play(mAnimator).with(mXAnimator)

        mAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                updateState(false)
            }
        })

        mXAnimator!!.interpolator = LinearInterpolator()
        mXAnimator!!.addUpdateListener { animation ->
            val x = animation.animatedFraction
            val y = mInterpolator!!.getInterpolation(x)
            curPoint!!.x = x
            curPoint!!.y = y
            Log.i("zincTest", "onAnimationUpdate: [$x,$y】")
            time_interpolator_view?.setCurPoint(curPoint)
        }

        time_interpolator_view?.setCurPoint(curPoint)
        time_interpolator_view?.setLineData(dataList)

        tv_run!!.setOnClickListener(View.OnClickListener {
            if (isRunning) {
                Toast.makeText(this@TimeInterpolatorActivity, "动画正在进行中，请稍等", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            updateState(true)
            val durationString = et_duration.text.toString()
            val duration =
                if (TextUtils.isEmpty(durationString)) 2000L else durationString.toLong()
            animatorSet?.duration = duration
            mAnimator?.interpolator = mInterpolator
            animatorSet?.start()
        })

        mAdapter = TimeInterpolatorAdapter(this, interpolatorList)
        mAdapter!!.setListener(this)
        recycle_view?.layoutManager = LinearLayoutManager(this)
        recycle_view?.adapter = mAdapter
    }

    private fun createData() {
        dataList.clear()
        var x = 0F
        while (x <= 1F) {
            val y: Float = mInterpolator!!.getInterpolation(x)
            val pointF = PointF(x, y)
            dataList.add(pointF)
            x += 1.0f / frame
        }
    }

    /**
     * 初始化插值器，需要的可以在这里添加自己的插值器
     */
    private fun buildInterpolatorList() {
        interpolatorList.clear()
        interpolatorList.add(TimeInterpolatorBean(true, "SpringInterpolator", SpringInterpolator()))
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "AccelerateDecelerateInterpolator",
                AccelerateDecelerateInterpolator()
            )
        )
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "AccelerateInterpolator",
                AccelerateInterpolator()
            )
        )
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "AnticipateInterpolator",
                AnticipateInterpolator()
            )
        )
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "AnticipateOvershootInterpolator",
                AnticipateOvershootInterpolator()
            )
        )
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "BounceInterpolator",
                BounceInterpolator()
            )
        )
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "CycleInterpolator",
                CycleInterpolator(1F)
            )
        )
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "DecelerateInterpolator",
                DecelerateInterpolator()
            )
        )
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "LinearInterpolator",
                LinearInterpolator()
            )
        )
        interpolatorList.add(
            TimeInterpolatorBean(
                false,
                "OvershootInterpolator",
                OvershootInterpolator()
            )
        )
        mInterpolator = interpolatorList[0].timeInterpolator
    }

    private fun getScreenHeight(context: Context): Float {
        val metrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            .getMetrics(metrics)
        return metrics.heightPixels.toFloat()
    }

    private fun dpToPx(context: Context, dipValue: Float): Float {
        val density: Float = context.resources.displayMetrics.density
        return dipValue * density + 0.5f
    }

    private fun getStatusHeight(context: Context): Float {
        var result = 0
        val resourceId: Int =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result.toFloat()
    }

    override fun onTimeInterpolatorClick(position: Int) {
        for (bean in interpolatorList) {
            bean.isSelect = false
        }
        interpolatorList[position].isSelect = true
        mInterpolator = interpolatorList[position].timeInterpolator
        mAdapter!!.notifyDataSetChanged()
        createData()
        time_interpolator_view?.setLineData(dataList)
        curPoint!!.x = 0f
        curPoint!!.y = 0f
        time_interpolator_view?.setCurPoint(curPoint)
    }

    private fun updateState(isRunning: Boolean) {
        this.isRunning = isRunning
        tv_state_info!!.text = if (isRunning) "running" else "ready"
        tv_state_info.setTextColor(
            if (isRunning) Color.parseColor("#32CD32") else Color.parseColor(
                "#1E90FF"
            )
        )
    }
}
