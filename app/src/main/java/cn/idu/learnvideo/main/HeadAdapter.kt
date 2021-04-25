package cn.idu.learnvideo.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.idu.learnvideo.R

class HeadAdapter : RecyclerView.Adapter<HeadAdapter.HeadViewHolder>() {

    private var flushCount = 0

    class HeadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textview = view.findViewById<TextView>(R.id.text)
        fun bind(msg: String) {
            textview.text = msg
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.main_head_item, parent, false)
        return HeadViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeadViewHolder, position: Int) {
        holder.bind("刷新次数: $flushCount")
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun flushCount(flushCount: Int) {
        this.flushCount = flushCount
        notifyDataSetChanged()
    }
}