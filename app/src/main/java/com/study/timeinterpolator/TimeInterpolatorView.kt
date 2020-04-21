package com.study.timeinterpolator

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.*

/**
 * @description 插值器的坐标显示
 */
class TimeInterpolatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // 速率的数据
    private val mLineDataList: MutableList<PointF> = ArrayList()

    // 坐标的画笔
    private var mLinePaint: Paint? = null

    // 速率的轨迹
    private val mDataPath = Path()

    // 字体画笔
    private var mTextPaint: Paint? = null

    // 点的笔
    private var mPointPaint: Paint? = null

    // 数据的最低点
    private var mMinPoint = DEFAULT_MIN_POINT

    // 数据的最高点
    private var mMaxPoint = DEFAULT_MAX_POINT

    // 视图的宽
    private var mViewWidth = 0f

    // 视图的高
    private var mViewHeight = 0f

    // 坐标的宽
    private var mWidth = 0f

    // 坐标中每个下标 的宽度
    private var mEachItemWidth = 0f
    private var mPositiveCount = 0
    private var mNegativeCount = 0

    // 当前的点
    private var mCurPoint: PointF? = null
    init{
        mLinePaint = Paint()
        mLinePaint!!.isAntiAlias = true
        mLinePaint!!.style = Paint.Style.STROKE
        mTextPaint = Paint()
        mTextPaint!!.isAntiAlias = true
        mTextPaint!!.textSize = TEXT_SIZE.toFloat()
        mPointPaint = Paint()
        mPointPaint!!.isAntiAlias = true
        mPointPaint!!.style = Paint.Style.FILL
    }

    /**
     * 设置当前移动的点
     *
     * @param curPoint
     */
    fun setCurPoint(curPoint: PointF?) {
        mCurPoint = curPoint
        invalidate()
    }

    fun setLineData(lineDataList: MutableList<PointF>?) {
        mLineDataList.clear()
        mLineDataList.addAll(lineDataList!!)
        mMinPoint = DEFAULT_MIN_POINT
        mMaxPoint = DEFAULT_MAX_POINT
        // 构建 路径，并选出最高和最低的point
        for (i in mLineDataList.indices) {
            val curPoint = mLineDataList[i]

            // 选最低点
            if (curPoint.y < mMinPoint.y) {
                mMinPoint = curPoint
            }

            // 选最高点
            if (curPoint.y > mMaxPoint.y) {
                mMaxPoint = curPoint
            }
        }
        calculateEachItemWidth()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w.toFloat()
        mViewHeight = h.toFloat()

        // 需要减去 padding 的宽度 和 字体的大小
        mWidth = min(
            w,
            h
        ) - 2 * PADDING - TEXT_SIZE.toFloat()
        calculateEachItemWidth()
    }

    /**
     * 计算每个格子的大小
     */
    private fun calculateEachItemWidth() {

        // 获取 y正半轴 的分割个数
        mPositiveCount =
            abs(ceil(mMaxPoint.y / GRID_INTERVAL_LENGTH.toDouble())).toInt()
        // 获取 y负半轴 的分割个数
        mNegativeCount =
            abs(floor(mMinPoint.y / GRID_INTERVAL_LENGTH.toDouble())).toInt()

        // 计算需要分割的数量，最少十个
        var intervalCount = mPositiveCount + mNegativeCount
        intervalCount =
            max(intervalCount, GRID_INTERVAL_COUNT)
        mEachItemWidth = mWidth / intervalCount
    }

    override fun onDraw(canvas: Canvas) {

        // 构建轨迹
        buildDataPath()
        canvas.save()

        // 移至原点
        moveToTheOrigin(canvas)

        // 画坐标
        drawCoordination(canvas)
        // 画网格
        drawGrid(canvas)

        // 画数据线
        drawDataLine(canvas)

        // 画下标
        drawText(canvas)

        // 画当前的点
        drawPoint(canvas)
        canvas.restore()
    }

    /**
     * 画点
     *
     * @param canvas
     */
    private fun drawPoint(canvas: Canvas) {
        if (mCurPoint == null) {
            return
        }
        mPointPaint!!.color = CUR_POINT_COLOR
        canvas.drawCircle(
            mCurPoint!!.x * mEachItemWidth * GRID_INTERVAL_COUNT,
            -mCurPoint!!.y * mEachItemWidth * GRID_INTERVAL_COUNT,
            CUR_POINT_RADIUS.toFloat(),
            mPointPaint!!
        )
    }

    /**
     * 画下标
     *
     * @param canvas
     */
    private fun drawText(canvas: Canvas) {
        canvas.drawText(
            "0", -PADDING.toFloat(), 0f,
            mTextPaint!!
        )
        mTextPaint!!.textAlign = Paint.Align.RIGHT
        for (i in 1..mPositiveCount) {
            if (i <= 10) {
                mTextPaint!!.color = COORDINATION_LINE_COLOR
            } else {
                mTextPaint!!.color = DATA_LINE_COLOR
            }
            canvas.drawText(
                getNumString(i * 0.1f), -PADDING / 2.toFloat(),
                -i * mEachItemWidth,
                mTextPaint!!
            )
        }
        mTextPaint!!.color = DATA_LINE_COLOR
        for (i in 1..mNegativeCount) {
            canvas.drawText(
                getNumString(i * -0.1f), -PADDING / 2.toFloat(),
                i * mEachItemWidth,
                mTextPaint!!
            )
        }
        mTextPaint!!.textAlign = Paint.Align.CENTER
        mTextPaint!!.color = COORDINATION_LINE_COLOR
        for (i in 1..GRID_INTERVAL_COUNT) {
            canvas.drawText(
                getNumString(i * 0.1f), i * mEachItemWidth,
                PADDING / 2 + TEXT_SIZE.toFloat(),
                mTextPaint!!
            )
        }
    }

    private fun getNumString(num: Float): String {
        return String.format("%.1f", num)
    }

    /**
     * 画网格线
     *
     * @param canvas
     */
    private fun drawGrid(canvas: Canvas) {
        mLinePaint!!.strokeWidth = dpToPx(0.5f).toFloat()
        mLinePaint!!.color = GRID_LINE_COLOR

        // 画y正轴横线
        for (i in 1..mPositiveCount) {
            canvas.drawLine(
                0f,
                -i * mEachItemWidth,
                GRID_INTERVAL_COUNT * mEachItemWidth,
                -i * mEachItemWidth,
                mLinePaint!!
            )
        }

        // 画y负轴横线
        for (i in 1..mNegativeCount) {
            canvas.drawLine(
                0f,
                i * mEachItemWidth,
                GRID_INTERVAL_COUNT * mEachItemWidth,
                i * mEachItemWidth,
                mLinePaint!!
            )
        }

        // 画x正轴竖线
        for (i in 1..GRID_INTERVAL_COUNT) {
            canvas.drawLine(
                i * mEachItemWidth,
                -mPositiveCount * mEachItemWidth,
                i * mEachItemWidth,
                mNegativeCount * mEachItemWidth,
                mLinePaint!!
            )
        }
    }

    /**
     * 画数据线
     *
     * @param canvas
     */
    private fun drawDataLine(canvas: Canvas) {
        mLinePaint!!.strokeWidth = dpToPx(1f).toFloat()
        mLinePaint!!.color = DATA_LINE_COLOR
        canvas.drawPath(mDataPath, mLinePaint!!)
    }

    /**
     * 画 x、y 轴
     *
     * @param canvas
     */
    private fun drawCoordination(canvas: Canvas) {
        mLinePaint!!.strokeWidth = dpToPx(1f).toFloat()
        mLinePaint!!.color = COORDINATION_LINE_COLOR

        // 画 y 轴
        canvas.drawLine(
            0f,
            -mPositiveCount * mEachItemWidth, 0f,
            mNegativeCount * mEachItemWidth,
            mLinePaint!!
        )

        // 画 x 轴
        canvas.drawLine(
            0f, 0f,
            GRID_INTERVAL_COUNT * mEachItemWidth, 0f,
            mLinePaint!!
        )
    }

    /**
     * 构建数据路径
     */
    private fun buildDataPath() {
        mDataPath.reset()
        val width =
            mEachItemWidth * GRID_INTERVAL_COUNT
        // 构建 路径，并选出最高和最低的point
        for (i in mLineDataList.indices) {
            val curPoint = mLineDataList[i]
            if (i == 0) {
                mDataPath.moveTo(curPoint.x * width, -curPoint.y * width)
            } else {
                mDataPath.lineTo(curPoint.x * width, -curPoint.y * width)
            }
        }
    }

    /**
     * 将画布移至 原点
     */
    private fun moveToTheOrigin(canvas: Canvas) {
        val verHeight = mEachItemWidth * mPositiveCount

        // 计算 横向移动距离
        val horPadding =
            mViewWidth - mEachItemWidth * GRID_INTERVAL_COUNT - 2 * PADDING
        val verPadding =
            mViewHeight - mEachItemWidth * (mPositiveCount + mNegativeCount) - 2 * PADDING - TEXT_SIZE / 2
        canvas.translate(
            horPadding / 2 + PADDING,
            verPadding / 2 + verHeight + PADDING
        )
    }

    /**
     * 获取视图的宽
     *
     * @return 视图宽 - 左右的内边距
     */
    private val viewEnableWidth: Float
        get() = mViewWidth - PADDING * 2

    /**
     * 获取视图的高
     *
     * @return 视图高 - 上下的内边距
     */
    private val viewEnableHeight: Float
        get() = mViewHeight - PADDING * 2

    companion object {
        // 外边距
        private val PADDING = dpToPx(5f)

        // 字体大小
        private val TEXT_SIZE = dpToPx(8f)

        // 点的半径
        private val CUR_POINT_RADIUS = dpToPx(4.5f)

        // X、Y 轴色
        private const val COORDINATION_LINE_COLOR = Color.BLACK

        // 网格线色
        private const val GRID_LINE_COLOR = Color.LTGRAY

        // 数据线色
        private val DATA_LINE_COLOR = Color.parseColor("#DB001B")

        // 当前点的色
        private val CUR_POINT_COLOR = Color.parseColor("#DC143C")

        // 默认的最低点
        private val DEFAULT_MIN_POINT = PointF(0F, 0F)

        // 默认的最高点
        private val DEFAULT_MAX_POINT = PointF(0F, 1F)

        // 10个间隔
        private const val GRID_INTERVAL_COUNT = 10

        // 每个间隔的跨幅
        private const val GRID_INTERVAL_LENGTH = 0.1f

        /**
         * 转换 dp 至 px
         *
         * @param dpValue dp值
         * @return px值
         */
        private fun dpToPx(dpValue: Float): Int {
            val metrics =
                Resources.getSystem().displayMetrics
            return (dpValue * metrics.density + 0.5f).toInt()
        }
    }

}