package com.boxdotsize.boxdotsize_android.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boxdotsize.boxdotsize_android.databinding.ItemAnalyzeRecordBinding
import com.boxdotsize.boxdotsize_android.room.AnalyzeResult
import com.bumptech.glide.Glide
import java.time.format.DateTimeFormatter

class RecordRecyclerAdapter:RecyclerView.Adapter<RecordRecyclerAdapter.RecordViewHolder>() {

    private val recordList= mutableListOf<AnalyzeResult>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding=ItemAnalyzeRecordBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(recordList[position])
    }

    override fun getItemCount(): Int =recordList.size

    fun setRecordList(list:List<AnalyzeResult>){
        recordList.clear()
        recordList.addAll(list)
        notifyItemRangeInserted(0,list.size)
    }

    inner class RecordViewHolder(private val binding:ItemAnalyzeRecordBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(data:AnalyzeResult){
            binding.rvRecordResult.text="가로 : ${data.width} \n세로 : ${data.height}\n높이 : ${data.tall}\n\n과제 : ${data.type}"
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
            binding.tvRecordTime.text = data.time.format(formatter)

            Glide.with(binding.ivRecordImage)
                .load(data.url)
                .into(binding.ivRecordImage)

            Glide.with(binding.ivRecordCroppedImage)
                .load(data.croppedUrl)
                .into(binding.ivRecordCroppedImage)
        }
    }
}