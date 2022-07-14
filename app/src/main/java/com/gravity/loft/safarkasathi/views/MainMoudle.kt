package com.gravity.loft.safarkasathi.views

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.google.gson.Gson
import com.gravity.loft.safarkasathi.R
import com.gravity.loft.safarkasathi.commons.*
import com.gravity.loft.safarkasathi.databinding.*
import com.gravity.loft.safarkasathi.migrated.CreateTrip
import com.gravity.loft.safarkasathi.models.CmtTrip
import com.gravity.loft.safarkasathi.services.LoginService
import com.gravity.loft.safarkasathi.services.TripService
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle
import java.util.*

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.e("T", Settings.getBearerToken()!!)

        //drawer
        with(DuoDrawerToggle(this, binding.drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)){
            binding.drawerLayout.apply {
                setDrawerListener(this@with)
                setDrawerListener(object : DrawerListener {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

                    override fun onDrawerOpened(drawerView: View) { vibrate() }


                    override fun onDrawerClosed(drawerView: View) { vibrate() }

                    override fun onDrawerStateChanged(newState: Int) {}
                })

                binding.homeButton.setOnClickListener { if (isDrawerOpen) closeDrawer() else openDrawer() }
            }
            syncState()
        }

        // set other ui elements
        binding.includeDrawerLayout.logoutText.setOnClickListener {
            Settings.clearAll()
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }

        binding.newItem.setOnClickListener { startActivity(Intent(this, CreateTrip::class.java)) }

        binding.includeDrawerLayout.avatarImage.setOnClickListener {
            ImagePicker.create(this@MainActivity)
                .single()
                .showCamera(true)
                .theme(R.style.Theme_SafarKaSathi)
                .start()
        }

        Settings.getAvatarImage()?.let { Glide.with(this).load(Uri.parse(it)).into(binding.includeDrawerLayout.avatarImage) }

        // fragments
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            supportFragmentManager.beginTransaction()
                .apply {
                    val fragment = when(it.itemId){
                        R.id.page_earn -> FragmentEarn()
                        R.id.page_history -> FragmentHistory()
                        R.id.page_wallet -> FragmentWallet()
                        else -> null
                    }
                    commitFragment(fragment!!, false)
                }.also { vibrate() }
                .addToBackStack(null)
                .commit()
            true
        }

        binding.bottomNavigation.selectedItemId = R.id.page_earn

        updateProfile()

    }

    fun commitFragment(fragmentToBeCommit: Fragment, fullScreen: Boolean){
        supportFragmentManager.beginTransaction()
            .apply {
                if(fullScreen)
                    replace(R.id.fragmentContainerFull, fragmentToBeCommit)
                else
                    replace(R.id.fragmentContainer, fragmentToBeCommit)
            }
            .also {
                vibrate()
            }
            .addToBackStack(null)
            .commit()
    }

    fun unlockDocumentDialog(cmtTrip: CmtTrip) {
        val sheet = SheetDocumentTrip().apply {
            val bundle = Bundle()
            bundle.putParcelable("data", cmtTrip)
            arguments = bundle
        }
        sheet.show(supportFragmentManager, "unlock-2")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // or get a single image only
            val image = ImagePicker.getFirstImageOrNull(data)
            val uri = image.uri
            Settings.setAvatarImage(uri.toString())
            Glide.with(this).load(uri).into(binding.includeDrawerLayout.avatarImage)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() { finish() }

    private fun updateProfile(){
        suspend fun loadProfileData(){
            val response = try { RetrofitBuilder.buildAuthService(LoginService::class.java).getProfile()
            }catch (e: Exception){
                e.printStackTrace()
                toast(R.string.API_ERROR_MESSAGE)
                return
            }

            BaseRepositories.instance().profile.postValue(response.body())
        }

        launchWithoutProgress { loadProfileData() }

        BaseRepositories.instance().profile.observe(this, {
            binding.includeDrawerLayout.nameProfile.text = it.name
            binding.includeDrawerLayout.mobileProfile.text = it.mobile
        })
    }

}

class FragmentEarn: BaseFragment(){

    private lateinit var binding: FragmentEarnBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEarnBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //register live-data data loader
        BaseRepositories.instance().isLoading.observe(viewLifecycleOwner, {
            loadAPIData()
        })

        BaseRepositories.instance().isLoading.postValue(true)

        binding.swipeLayout.setOnRefreshListener {
            BaseRepositories.instance().isLoading.postValue(false)
            binding.shimmer.startShimmer()
            binding.shimmer.visibility = View.VISIBLE
            binding.linearContent.visibility = View.GONE
        }

        binding.allTrip.setOnClickListener {
            val mainActivity = activity as MainActivity
            val tripRequestsFragment = TripRequests()
            mainActivity.commitFragment(tripRequestsFragment, true)
        }

        binding.ongoingTrip.setOnClickListener {
            val mainActivity = activity as MainActivity
            val tripRequestsFragment = TripRequests()
            mainActivity.commitFragment(tripRequestsFragment, true)
        }

        BaseRepositories.instance().rewardPoints.observe(viewLifecycleOwner, {
            if (it != 0) binding.tvRewardPoints.text = "Trips $it Points"
            else binding.tvRewardPoints.text = "Trips 0 Points"
        })

        BaseRepositories.instance().tripsCmt.observe(viewLifecycleOwner, {

            binding.swipeLayout.isRefreshing = false
            it.filter { it.trip_status == "Lock" || it.trip_status == "Open" }.let {
                if (it.isNotEmpty()) {
                    binding.tripCount.text = "${it.size} Trip Requests"
                    val firstItem = it.first()
                    binding.overviewTitle.text = firstItem.trip_transporter_name

                    binding.overviewSummary.text = Utility.getPrettyTime(firstItem.trip_starttime)
                }
            }
        })

        BaseRepositories.instance().onGoingTrips.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                binding.ongoingCount.text = it.size.toString() + " Ongoing Trips"
                binding.ongoingTitle.text = it[0].trip_startlocation.substringBefore(",") + " - " + it[0].trip_endlocation.substringBefore(",")
                //binding.ongoingSummary.text = "200 of " + it[0].trip_totalpoints + " earned"
                binding.linearTasks.visibility = View.GONE
                binding.ongoingSummary.text = ""
            }else {
                binding.ongoingCount.text = "0 Ongoing Trips"
                binding.ongoingTitle.text = "No Ongoing Trips"
                binding.linearTasks.visibility = View.GONE
            }
        })

        BaseRepositories.instance().trips.observe(viewLifecycleOwner, {
            binding.ongoingCount.text = "${it.size} Ongoing Trips"
            if (it.isNotEmpty()) {
                val firstItem = it.first()
                binding.ongoingTitle.text = "${firstItem.trip_startlocation}-${firstItem.trip_endlocation}"
            }
        })
    }

    private fun loadAPIData(){
        //api call
        suspend fun finalLoadApiData(){
            val responseCmtTrips =
                try {
                    RetrofitBuilder.buildAuthService(TripService::class.java).getCmtTrips()
                } catch (e: Exception){
                    e.printStackTrace()
                    toast(R.string.API_ERROR_MESSAGE)
                    return
            }

            getRewardEligibleTrips(responseCmtTrips.body())

            getOnGoingTrips(responseCmtTrips.body())


            Log.e("code", "" + responseCmtTrips.code())
            Log.e("body", "" + Gson().toJson(responseCmtTrips.body()))

            binding.shimmer.stopShimmer()
            binding.shimmer.visibility = View.GONE
            binding.linearContent.visibility = View.VISIBLE

            val responseTrips = try { RetrofitBuilder.buildAuthService(TripService::class.java).getTrips()
            }
            catch (e: Exception){ toast(R.string.API_ERROR_MESSAGE)
                return
            }

            Log.e("codeTrips", "" + responseTrips.code())
            Log.e("bodyTrips", "" + Gson().toJson(responseTrips.body()))

            if(responseCmtTrips.isSuccessful) BaseRepositories.instance().tripsCmt.postValue(responseCmtTrips.body())

        }

        //if(showProgress) launchWithProgress { finalLoadApiData() }
        //else launchWithoutProgress { finalLoadApiData() }
        launchWithoutProgress { finalLoadApiData() }
    }

    private fun getOnGoingTrips(cmtTripsList: List<CmtTrip>?){
        val onGoingList: MutableList<CmtTrip>
        if (cmtTripsList != null){
            onGoingList = mutableListOf()
            for(cmtTrip in cmtTripsList){
                if (cmtTrip.trip_status == "Ongoing"){
                    onGoingList.add(cmtTrip)
                    Log.e("ongoingListSize", onGoingList.size.toString())
                }
            }

            Log.e("ongoingListSize", onGoingList.size.toString())
            BaseRepositories.instance().onGoingTrips.postValue(onGoingList)
        }
    }

    private suspend fun getRewardEligibleTrips(cmtTripsList: List<CmtTrip>?){
        if (cmtTripsList != null) {
            val rewardEligibleTripsList = mutableListOf<Int>()
            for (item in cmtTripsList){
                if (item.trip_status == "Complete" || item.trip_status == "Ongoing") {
                    rewardEligibleTripsList.add(item.trip_id)
                    Log.e("trip_id", item.trip_id.toString())
                }
            }
            if (rewardEligibleTripsList.size > 0) {
                getTotalRewardedPoints(rewardEligibleTripsList) }
            else{ BaseRepositories.instance().rewardPoints.postValue(0) }
        }
    }

    private suspend fun getTotalRewardedPoints(rewardEligibleTripsList: MutableList<Int>){
        var rewardPoints = 0
        val service = RetrofitBuilder.buildAuthService(TripService::class.java)
        for (completedTripId in rewardEligibleTripsList){
            launchWithoutProgress {
                val map = mapOf<String, Any>("trip_id" to completedTripId)
                // val map = mapOf<String, Any>("trip_id" to 77)0
                val response = service.getCmtTripTask(map)
                if (response.isSuccessful){
                    if (response.body() != null)
                        for (task in response.body()!!){
                            if (task.status == "Complete") {
                                rewardPoints = rewardPoints.plus(task.triptask_points.toInt())
                                BaseRepositories.instance().rewardPoints.postValue(rewardPoints)
                            }
                        }
                }
            }
        }
    }
}

class FragmentHistory: BaseFragment(){

    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        return binding.root
    }
}

class FragmentWallet: BaseFragment(){

    private lateinit var binding: FragmentWalletBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWalletBinding.inflate(layoutInflater)
        return binding.root
    }

    fun withTemp(){
        val str: String = "jj jj"
        var rev = ""

        val length = str.length

        for (i in length - 1 downTo 0)
            rev += str[i]

        if (str == rev)
            println("$str is a palindrome")
        else
            println("$str is not a palindrome")
    }

    fun withoutTemp(){
        val str: String = "racecacr"

        var forward = 0
        var backward = str.length - 1

        while (backward > forward) {
            if (str[forward++] != str[backward++])
                print("No")
        }
        print("Yes")

    }}