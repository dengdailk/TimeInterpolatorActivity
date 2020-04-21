package com.study.timeinterpolator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * @description 列表适配
 */
class TimeInterpolatorAdapter(
    context: Context?,
    private val mDataList: MutableList<TimeInterpolatorBean>
) : RecyclerView.Adapter<TimeInterpolatorAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mListener: ClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            mInflater.inflate(
                R.layout.item_time_interpolator,
                parent,
                false
            )
        )
    }

    fun setListener(listener: ClickListener?) {
        mListener = listener
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val timeInterpolatorBean = mDataList[position]
        holder.checkbox.isChecked = timeInterpolatorBean.isSelect
        holder.name.text = timeInterpolatorBean.name
        holder.llItem.setOnClickListener {
            if (mListener != null) {
                mListener!!.onTimeInterpolatorClick(position)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val name: TextView = itemView.findViewById(R.id.name)
        val llItem: LinearLayout = itemView.findViewById(R.id.ll_item)
    }

    interface ClickListener {
        fun onTimeInterpolatorClick(position: Int)
    }

}