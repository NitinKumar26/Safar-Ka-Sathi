package com.gravity.loft.safarkasathi.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.commons.*
import com.gravity.loft.safarkasathi.databinding.ActivityTaskBinding
import com.gravity.loft.safarkasathi.databinding.ItemTaskDetailBinding
import com.gravity.loft.safarkasathi.models.CmtTask
import com.gravity.loft.safarkasathi.services.TripService
import kotlin.properties.Delegates


class TaskActivity : BaseActivity() {

    private var trip_id by Delegates.notNull<Int>()

    private val binding: ActivityTaskBinding by lazy { ActivityTaskBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        trip_id = intent.getIntExtra("trip_id", 0)
        binding.hom.setOnClickListener { finish() }
        loadData()
        Log.e("trip_id", trip_id.toString())
    }

    private fun loadData() {
        launchWithProgress {
            val service = RetrofitBuilder.buildAuthService(TripService::class.java)
            val map = mapOf<String, Any>("trip_id" to trip_id)
            // val map = mapOf<String, Any>("trip_id" to 77)
            val response = service.getCmtTripTask(map)
            if (response.isSuccessful) {
                binding.taskList.adapter = TaskAdapter(this@TaskActivity, response.body()!!)
                updateViews(response.body()!!)
            }
        }
    }

    private fun updateViews(cmtTask: List<CmtTask>){
        cmtTask.count { it.status == Constant.TASK_STATUS_COMPLETE }.let {
            val progressValue =  it * 100 / cmtTask.size
            binding.taskProgressBar.progress = progressValue
            binding.taskProgressText.text = "$it of ${cmtTask.size} Task Complete"
        }
        cmtTask.firstOrNull()?.let {
            binding.transpotername.text = it.trip_transporter_name
        }
    }

}

class TaskAdapter(val activity: TaskActivity, val data: List<CmtTask>) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    var expandItemPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.taskTitle.text =  item.triptask_taskname
        holder.binding.tasktitleExpand.text = item.triptask_taskname
        holder.binding.taskPoints.text = item.triptask_points
        holder.binding.taskPointsExpand.text = item.triptask_points
        holder.binding.textDetail1.text = getHtmlText(item.triptask_message)

        /*
       if(position == expandItemPosition) holder.binding.swicherTask.displayedChild = 1
       else holder.binding.swicherTask.displayedChild = 0
         */

       if(item.status == Constant.TASK_STATUS_COMPLETE){
           // change icon to check
           val imageStaus = R.drawable.ic_complete
           holder.binding.iconStatus.setImageResource(imageStaus)
           holder.binding.iconStatusSecond.setImageResource(imageStaus)
       }
    }

    fun getHtmlText(html: String): Spanned{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(html);
        }
    }

    inner class ViewHolder(val binding: ItemTaskDetailBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val item = data[adapterPosition]
                if(adapterPosition == expandItemPosition){
                    //Item is expanded
                    when(item.status){
                        Constant.TASK_STATUS_DRAFT -> {
                            val intent = Intent(activity, UploadTaskActivity::class.java)
                            intent.putExtra("cmt_task", item)
                            activity.startActivity(intent)
                        }
                        Constant.TASK_STATUS_PENDING -> {
                            val intent = Intent(activity, TaskStatusPending::class.java)
                            intent.putExtra("cmt_task", item)
                            activity.startActivity(intent)
                        }
                    }
                }else{
                    //Item is not Expanded
                    when (item.status) {
                        Constant.TASK_STATUS_COMPLETE -> {
                            val intent = Intent(activity, TaskStatusAccept::class.java)
                            intent.putExtra("cmt_task", item)
                            activity.startActivity(intent)
                        }
                        Constant.TASK_STATUS_REJECT -> {
                            val intent = Intent(activity, UploadTaskActivity::class.java)
                            intent.putExtra("cmt_task", item)
                            activity.startActivity(intent)
                        }
                        else -> {
                            //Else Expand Item
                            expandItemPosition = adapterPosition
                            binding.swicherTask.displayedChild = 1
                        }
                    }
                }
                notifyDataSetChanged()

            }
        }
    }
}