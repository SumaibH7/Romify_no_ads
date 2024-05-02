package com.bluell.roomdecoration.interiordesign.ui.rewards

import android.app.AlertDialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bluell.roomdecoration.interiordesign.MainActivityViewModel
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.data.models.response.newUser
import com.bluell.roomdecoration.interiordesign.databinding.FragmentRewardsBinding
import com.bluell.roomdecoration.interiordesign.databinding.RewardDialogBinding
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.OnUserEarnedRewardListener
//import com.google.android.gms.ads.rewarded.RewardedAd
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.launch

class RewardsFragment : Fragment() {
    private var _binding:FragmentRewardsBinding ?= null
    private val binding get() = _binding!!
    private var _navController:NavController ?= null
    private val navController get() = _navController!!
    lateinit var preferenceDataStoreHelper: UserPreferencesDataStoreHelper

    private val mainActivityViewModel: MainActivityViewModel by viewModels({ requireActivity() })
    var lastClaimedTime:Long = 0L

    private var userObj: newUser?=null
    private var totalGems=0

    private var progressDialog:ProgressDialog ?= null

    val packageName = "com.bluell.roomdecoration.interiordesign"
    val baseUrl = "https://play.google.com/store/apps/details"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRewardsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _navController = findNavController()
        preferenceDataStoreHelper = UserPreferencesDataStoreHelper(requireContext())
        getTimeFromDataStore()
        initObservers()

        setEvents()

    }

    private fun getTimeFromDataStore(){

        lifecycleScope.launch {
            preferenceDataStoreHelper.getPreference(PreferenceDataStoreKeysConstants.LAST_CLAIMED_TIME,0).collect(){
                lastClaimedTime = it
            }
        }

    }

    private fun showEarnedRewardDialog(gems:String) {
        val builder = AlertDialog.Builder(requireContext())
        val binding = RewardDialogBinding.inflate(layoutInflater)
        builder.setView(binding.root)
        val alertDialog = builder.create()

        alertDialog.setCancelable(true)

        binding.closeDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        Glide.with(alertDialog.context)
            .asGif()
            .load(R.raw.earned_reward_anim)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.earnedAnimation)

        binding.gemsTxt.text = gems
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()
    }

    private fun initObservers(){
        mainActivityViewModel.user.observe(viewLifecycleOwner){user->
            when(user.deviceID){
                Constants.getDeviceId(requireContext())->{
                    userObj=user

                    binding.userGems.text = userObj?.totalGems.toString()
                }
            }

        }
    }


    fun setEvents(){
        binding.tab1.setOnClickListener {
            switchTab(binding.detailsCOnatiner, binding.claimsdetailsCOnatiner, binding.indicatorTab1, binding.indicatorTab2, binding.tab1Txt, binding.tab2Txt)
        }

        binding.tab2.setOnClickListener {
            switchTab(binding.claimsdetailsCOnatiner, binding.detailsCOnatiner, binding.indicatorTab2, binding.indicatorTab1, binding.tab2Txt, binding.tab1Txt)

        }

        binding.backBtn.setOnClickListener {
            navController.navigateUp()
        }

        binding.addPhoto.setOnClickListener {
            navController.navigate(R.id.premiumInAppFragment)
        }

        binding.purchaseGems.setOnClickListener {
            navController.navigate(R.id.premiumInAppFragment)
        }

        binding.watchAds.setOnClickListener {
            progressDialog = ProgressDialog(requireContext())
            progressDialog!!.setCancelable(false)
            progressDialog!!.setMessage("Please wait, Ad is loading")
            progressDialog?.show()

//            loadRewardAd()
        }

        binding.dailyGems.setOnClickListener {
            if (canClaimGems()){
                totalGems+=3
                userObj?.otherGems = userObj?.otherGems?.plus(totalGems)!!
                userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
                mainActivityViewModel.updateInAPPGems(userObj!!)
                mainActivityViewModel.setUser(userObj!!)
                showEarnedRewardDialog(" $totalGems gems")
                lifecycleScope.launch {
                    preferenceDataStoreHelper.putPreference(PreferenceDataStoreKeysConstants.LAST_CLAIMED_TIME,System.currentTimeMillis())
                }

                totalGems = 0


            }else{
                Toast.makeText(requireContext(),
                    getString(R.string.daily_ad_limit_reached_try_again_tomorrow), Toast.LENGTH_SHORT).show()
            }
        }

        binding.referToFriends.setOnClickListener {
            val referralLink = "$baseUrl?id=$packageName&referrer=${Constants.getDeviceId(requireContext())}"

        }
    }


    //test id for rewarded ad
    //ca-app-pub-3940256099942544/5224354917


//    private fun loadRewardAd() {
//        val adRequest = AdRequest.Builder().build()
//        RewardedAd.load(requireContext(), "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
//            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                progressDialog?.dismiss()
//                // Handle the error.
//                Log.d("loading error", loadAdError.toString())
//                Toast.makeText(requireContext(), "Ad failed to load. Please check your internet connection", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onAdLoaded(ad: RewardedAd) {
//                progressDialog?.dismiss()
//                ad.show(requireActivity(), OnUserEarnedRewardListener { rewardItem ->
//                    // Handle the reward.
//                    Log.d("TAG", "The user earned the reward.")
//                    val rewardAmount = rewardItem.amount
//                    val rewardType = rewardItem.type
//                    totalGems+=2
//                    userObj?.otherGems = userObj?.otherGems?.plus(totalGems)!!
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
//                    mainActivityViewModel.setUser(userObj!!)
//                    showEarnedRewardDialog(" $totalGems gems")
//
//                    totalGems = 0
//                    Log.e("Reward", "onUserEarnedReward: $rewardAmount")
//                    Log.e("Reward", "onUserEarnedReward: $rewardType")
//                })
//            }
//        })
//    }

    private fun switchTab(tabContainerIn: View, tabContainerOut: View, indicatorIn: View, indicatorOut: View, textIn: TextView, textOut: TextView) {
        val slideInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
        tabContainerIn.startAnimation(slideInAnimation)
        tabContainerIn.visibility = View.VISIBLE

        val slideOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right)
        tabContainerOut.startAnimation(slideOutAnimation)
        tabContainerOut.visibility = View.GONE

        indicatorOut.visibility = View.GONE
        indicatorIn.visibility = View.VISIBLE

        textIn.setTextColor(resources.getColor(R.color.home_dark_color))
        textOut.setTextColor(Color.parseColor("#999999"))
    }


    fun canClaimGems(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastClaimedTime
        return timeDifference >= 24 * 60 * 60 * 1000
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding =  null
    }

}