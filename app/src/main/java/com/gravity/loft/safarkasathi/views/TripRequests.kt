package com.gravity.loft.safarkasathi.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.commons.*
import com.gravity.loft.safarkasathi.databinding.*
import com.gravity.loft.safarkasathi.models.CmtTrip
import com.gravity.loft.safarkasathi.services.TripService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val TRIP_ACTION_REJECT = "reject"
const val TRIP_ACTION_ACCEPT = "accept"

class TripRequests : BaseFragment() {

    private val binding: ActivityTripRequestsBinding by lazy {
        ActivityTripRequestsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeIcon.setOnClickListener {
            requireFragmentManager().beginTransaction().remove(this).commit()
        }

        BaseRepositories.instance().tripsCmt.observe(viewLifecycleOwner, { loadViews(it) })
    }

    private fun loadViews(tripData: List<CmtTrip>) {
        tripData.filter { it.trip_status == "Lock" || it.trip_status == "Open" }.let {
            binding.openListText.text = "${it.size} Open Requests"
            binding.listOpen.adapter = TripAdapter(this, it)
        }

        binding.listReject.adapter = TripAdapter(this, tripData.filter { it.trip_status == "Reject" })
        binding.listExpire.adapter = TripAdapter(this, tripData.filter { it.trip_status == "Expiry" })
        binding.listOngoing.adapter = TripAdapter(this, tripData.filter { it.trip_status == "Ongoing" })

        binding.shimmer.visibility = View.GONE
        binding.linearContent.visibility = View.VISIBLE
    }

    fun unlockTripDialog(cmtTrip: CmtTrip) {
        val sheet = SheetUnlockTrip().apply {
            val bundle = Bundle()
            bundle.putParcelable("data", cmtTrip)
            arguments = bundle

        }
        sheet.show(requireFragmentManager(), "unlock-1")
    }

    fun unlockActionDialog(cmtTrip: CmtTrip) {
        val sheet = SheetActionTrip().apply {
            val bundle = Bundle()
            bundle.putParcelable("data", cmtTrip)
            arguments = bundle

        }
        sheet.show(requireFragmentManager(), "unlock-2")
    }

    fun openTaskActivity(cmtTrip: CmtTrip) {
       startActivity(
           Intent(activity, TaskActivity::class.java).putExtra("trip_id", cmtTrip.trip_id)
       )
    }

}

class SheetUnlockTrip : BaseSheetDialog() {

    lateinit var binding: SheetLayoutUnlockTripBinding
    lateinit var cmtTrip: CmtTrip

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SheetLayoutUnlockTripBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        cmtTrip = arguments?.getParcelable("data")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editNumber.doAfterTextChanged {
            binding.transporterName.text = it
            if (it?.length != 4) {
                binding.submitButton.alpha = 0.5f
            } else {
                binding.submitButton.alpha = 1.0f
            }

        }
        binding.submitButton.setOnClickListener {
            binding.transporterName.text = cmtTrip.trip_transporter_name
            val vehicleNumber = binding.editNumber.text.toString()
            if (vehicleNumber == cmtTrip.vehicle_id.takeLast(4)) {
                launchWithProgress {
                    val responseUnlock = try {
                        val data = mapOf<String, Any>("trip_id" to cmtTrip.trip_id, "vehicle_number" to cmtTrip.vehicle_id)
                        RetrofitBuilder.buildAuthService(TripService::class.java).unlockTrip(data)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast(R.string.API_ERROR_MESSAGE)
                        dismiss()
                        return@launchWithProgress
                    }
                    // api is successfully called
                    dismiss()
                    BaseRepositories.instance().isLoading.postValue(false)
                    unlockActionDialog(cmtTrip)
                }
            } else { toast(R.string.INVALID_LAST_DIGIT_VEHICLE_NUMBER) }
        }
    }

    private fun unlockActionDialog(cmtTrip: CmtTrip) {
        val sheet = SheetActionTrip().apply {
            val bundle = Bundle()
            bundle.putParcelable("data", cmtTrip)
            arguments = bundle
        }
        sheet.show(requireFragmentManager(), "unlock-2")
    }
}

class SheetActionTrip : BaseSheetDialog() {

    lateinit var binding: SheetLayoutUnlockActionBinding
    lateinit var cmtTrip: CmtTrip

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SheetLayoutUnlockActionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cmtTrip = arguments?.getParcelable("data")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rejectAction.setOnClickListener { unlockTripAction(cmtTrip, TRIP_ACTION_REJECT)
            dismiss()
        }

        binding.acceptAction.setOnClickListener { unlockTripAction(cmtTrip, TRIP_ACTION_ACCEPT)
            dismiss()
        }

        binding.transporterName.text = cmtTrip.trip_transporter_name
        binding.text1.text = cmtTrip.trip_startlocation
        binding.text2.text = cmtTrip.trip_pick_up_points ?: ""
        binding.fromLocation.text = cmtTrip.trip_startlocation

        Log.e("pickupPoints", cmtTrip.trip_pick_up_points ?: "Null")

        val serverDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

        val startTime = formatter.format(LocalDateTime.parse(cmtTrip.trip_starttime, serverDateFormatter))
        val endTime = formatter.format(LocalDateTime.parse(cmtTrip.trip_validity, serverDateFormatter))

        binding.text3.text = cmtTrip.trip_endlocation
        binding.startdate.text = startTime?.toString()
        binding.enddate .text = endTime
        binding.totalpoints.text = "Earn ${cmtTrip.trip_totalpoints} Points"
        binding.totaltask.text = "${cmtTrip.trip_task.toString()} tasks to complete"

    }

    private fun unlockTripAction(cmtTrip: CmtTrip, status: String) {
        val activity = activity as MainActivity

        activity.launchWithProgress {
            val data = mapOf<String, Any>("trip_id" to cmtTrip.trip_id, "status" to status)
            val service = RetrofitBuilder.buildAuthService(TripService::class.java)
            service.unlockTripAction(data)

            //refresh data
            BaseRepositories.instance().isLoading.postValue(false)

            // go to docs
            if (status == TRIP_ACTION_ACCEPT) {
                val driverDocument = service.checkAadhar().body()!!
                Log.e("DRIVER", Gson().toJson(driverDocument))
                when {
                    driverDocument.driver_lincensephoto == null ->  activity.unlockDocumentDialog(cmtTrip)
                    driverDocument.driver_lincensephoto.isEmpty() -> activity.unlockDocumentDialog(cmtTrip)
                    else -> activity.startActivity(Intent(activity, TaskActivity::class.java).putExtra("trip_id", cmtTrip.trip_id))
                }
            }
        }
    }
}

class SheetDocumentTrip : BaseSheetDialog() {
    lateinit var binding: SheetLayoutDocumentDriverBinding
    lateinit var cmtTrip: CmtTrip

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SheetLayoutDocumentDriverBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        cmtTrip = arguments?.getParcelable("data")!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.transporterName.text = cmtTrip.trip_transporter_name
        binding.text1.text = cmtTrip.trip_startlocation
        binding.text3.text = cmtTrip.trip_endlocation
        binding.text2.text = cmtTrip.trip_pick_up_points ?: ""
        binding.totalpoints.text = "Earn ${cmtTrip.trip_totalpoints} Points"
        binding.tasks.text = "${cmtTrip.trip_task.toString()} tasks to complete"
        binding.tstartdate.text = cmtTrip.trip_starttime
        binding.enddate.text =   cmtTrip.trip_validity
        binding.tvFromAddress.text = cmtTrip.trip_startlocation

        binding.addDocs.setOnClickListener {
            startActivity(Intent(activity, DocumentActivity::class.java).putExtra("trip_id", cmtTrip.trip_id))
            dismiss()
        }
    }
}

class TripAdapter(private val tripRequestFragment: TripRequests, val data: List<CmtTrip>) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTripDetail1Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: CmtTrip = data[position]
        holder.binding.tv1.text = item.trip_transporter_name

        when (item.trip_status) {
            "Lock" -> {
                holder.binding.tv2.text = "Started ${Utility.getPrettyTime(item.trip_starttime)}"
                holder.binding.iv1.setImageResource(R.drawable.icon_5)
                holder.binding.root.setOnClickListener { tripRequestFragment.unlockTripDialog(item) }
            }
            "Reject" -> {
                holder.binding.tv1.setTextColor(Color.parseColor("#d12a52"))
                holder.binding.tv2.setTextColor(Color.parseColor("#8C000000"))
                holder.binding.cardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
                holder.binding.tv2.text = "Rejected in ${Utility.getPrettyTime(item.trip_starttime)}"
            }
            "Expiry" -> {
                holder.binding.tv1.setTextColor(Color.parseColor("#cc333333"))
                holder.binding.tv2.setTextColor(Color.parseColor("#8C000000"))
                holder.binding.cardView.setCardBackgroundColor(Color.parseColor("#ffffff"))
                holder.binding.iv1.setImageResource(R.drawable.icon_6)
                holder.binding.tv2.text = "Expired in ${Utility.getPrettyTime(item.trip_starttime)}"
            }
            "Open" -> {
                holder.binding.tv2.text = "Starting in ${Utility.getPrettyTime(item.trip_starttime)}"
                holder.binding.root.setOnClickListener { tripRequestFragment.unlockActionDialog(item) }
            }
            "Ongoing" -> {
                holder.binding.tv2.text = "Started ${Utility.getPrettyTime(item.trip_starttime)}"
                holder.binding.root.setOnClickListener { tripRequestFragment.openTaskActivity(item) }
            }
        }
    }

    inner class ViewHolder(val binding: ItemTripDetail1Binding) : RecyclerView.ViewHolder(binding.root)

}
