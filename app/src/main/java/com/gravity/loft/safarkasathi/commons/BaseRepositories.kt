package com.gravity.loft.safarkasathi.commons

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gravity.loft.safarkasathi.models.CmtTrip
import com.gravity.loft.safarkasathi.models.Profile
import com.gravity.loft.safarkasathi.models.Trip

class BaseRepositories: ViewModel() {
    var progressCount = 1

    val tripsCmt: MutableLiveData<List<CmtTrip>> by lazy { MutableLiveData<List<CmtTrip>>() }

    val trips: MutableLiveData<List<Trip>> by lazy { MutableLiveData<List<Trip>>() }

    val rewardPoints: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val onGoingTrips: MutableLiveData<List<CmtTrip>> by lazy { MutableLiveData<List<CmtTrip>>() }

    val isLoading = MutableLiveData<Boolean>()

    val profile: MutableLiveData<Profile> by lazy { MutableLiveData<Profile>() }

    companion object{
        private var viewModel: BaseRepositories? = null

        fun instance(): BaseRepositories {
            if(viewModel == null) viewModel = BaseRepositories()
            return viewModel!!
        }
    }

}