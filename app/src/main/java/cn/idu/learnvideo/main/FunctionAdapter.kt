package cn.idu.learnvideo.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.idu.learnvideo.R

class FunctionAdapter(private val funcArray: Array<String>, private val funcClick: (Int) -> Unit) :
    RecyclerView.Adapter<FunctionAdapter.HeadViewHolder>() {

    class HeadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textview = view.findViewById<TextView>(R.id.text)
        fun bind(msg: String) {
            textview.text = msg
        }

        fun bindClick(funcClick: (Int) -> Unit, index: Int) {
            textview.setOnClickListener { funcClick(index) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.main_func_item, parent, false)
        return HeadViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeadViewHolder, position: Int) {
        holder.bind(funcArray[position])
        holder.bindClick(funcClick, position)
    }

    override fun getItemCount(): Int {
        return funcArray.size
    }
}