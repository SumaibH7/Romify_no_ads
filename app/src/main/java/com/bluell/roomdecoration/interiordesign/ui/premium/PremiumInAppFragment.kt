package com.bluell.roomdecoration.interiordesign.ui.premium

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.bluell.roomdecoration.interiordesign.common.interfaces.SelectPlan
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOPlans
import com.bluell.roomdecoration.interiordesign.databinding.FragmentPremiumInAppBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PremiumInAppFragment : Fragment() {
    private var _binding:FragmentPremiumInAppBinding ?= null
    private val binding get() = _binding!!

    private var _navController: NavController?=null
    private val navController get() = _navController!!

    private var arrayListPlans: ArrayList<DTOPlans>? = null
    private lateinit var plansAdapter: InAppAdapter

    var selectedPlan = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentPremiumInAppBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _navController=findNavController()

        initPlans()
//        initObservers()
        purchasesPrice()
        setEvents()

    }


    fun setEvents(){

        binding.backBtn.setOnClickListener {
            navController.navigateUp()
        }

    }

    private fun initPlans() {
        arrayListPlans = arrayListOf(
            DTOPlans("inapp_plan1","Rs 300",false,false,20,true),
            DTOPlans("inapp_plan2","Rs 500",false,false,40,true),
            DTOPlans("inapp_plan3","Rs 1000",false,false,100,true),
            DTOPlans("inapp_plan4","Rs 2000",false,true,500,true),
            DTOPlans("inapp_plan5","Rs 3500",false,true,500,true),
            DTOPlans("inapp_plan6","Rs 4000",false,true,500,true)
        )

        plansAdapter = InAppAdapter(arrayListPlans!!,2,object:SelectPlan{
            override fun selectedPlan(dtoPlans: DTOPlans) {
                when(dtoPlans.planName){
                    "inapp_plan1"->{
                        purchaseGemsFromGP(0)
                    }
                    "inapp_plan2"->{

                        purchaseGemsFromGP(1)
                    }
                    "inapp_plan3"->{

                        purchaseGemsFromGP(2)
                    }
                    "inapp_plan4"->{

                        purchaseGemsFromGP(3)
                    }
                    "inapp_plan5"->{

                        purchaseGemsFromGP(4)
                    }
                    "inapp_plan6"->{
                        purchaseGemsFromGP(5)
                    }
                }
            }

        })
        binding.premiumRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.premiumRv.adapter = plansAdapter

    }


    private fun purchasesPrice() {

        lifecycleScope.launch {


            val billingClient = BillingClient.newBuilder(requireActivity())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
            billingClient.startConnection(object : BillingClientStateListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        Log.e("TAG", "ready to purchess")
                        val skuList: MutableList<String> = ArrayList()
                        skuList.add("inapp_plan1")
                        skuList.add("inapp_plan2")
                        skuList.add("inapp_plan3")
                        skuList.add("inapp_plan4")
                        val params = SkuDetailsParams.newBuilder()
                        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                        billingClient.querySkuDetailsAsync(params.build()) {
                                billingResult, skuDetailsList ->
                            try {

                                lifecycleScope.launch(Dispatchers.Main) {
                                    arrayListPlans = arrayListOf(
                                        DTOPlans("Watch Ad and Earn Gems","Watch Ad and Earn Gems",true,false,2,true),
                                        DTOPlans("Daily Gems","Daily Gems",false,false,2,true),
                                        DTOPlans("inapp_plan1",skuDetailsList?.get(0)!!.price,false,false,20,true),
                                        DTOPlans("inapp_plan2",skuDetailsList?.get(1)!!.price,false,false,40,true),
                                        DTOPlans("inapp_plan3",skuDetailsList?.get(2)!!.price,false,false,100,true),
                                        DTOPlans("inapp_plan4",skuDetailsList?.get(3)!!.price,false,true,500,true),
                                        DTOPlans("inapp_plan4",skuDetailsList?.get(4)!!.price,false,true,500,true),
                                        DTOPlans("inapp_plan4",skuDetailsList?.get(5)!!.price,false,true,500,true),
                                    )

                                    plansAdapter.newData(arrayListPlans!!)

                                }







                            } catch (c: java.lang.Exception) {
                                c.printStackTrace()
                            }
                            Log.e("TAG", "sku details " + skuDetailsList!!.size)
                            // Process the result.
                            Log.e("TAG", "skuDetailsList.get(0).getTitle() " + skuDetailsList[0].title)
                        }
                    }
                }
                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    Log.e("TAG", "service disconnected")
                }
            })

        }
    }


    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            when(selectedPlan){
                0->{
//                    totalGems+=20
//                    userObj?.otherGems=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }
                1->{
//                    totalGems+=40
//                    userObj?.otherGems=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }
                2->{
//                    totalGems+=100
//                    userObj?.otherGems=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }
                3->{
//                    totalGems+=500
//                    userObj?.otherGems=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }

                4->{
//                    totalGems+=500
//                    userObj?.otherGems=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }
                5->{
//                    totalGems+=500
//                    userObj?.otherGems=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }



            }
//            mainActivityViewModel.setUser(userObj!!)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(requireContext(), "Purchases Error ", Toast.LENGTH_SHORT).show()
//                laodAd =true
        } else {
            // Handle any other error codes.
        }

    }


    private fun purchaseGemsFromGP(index:Int) {
//        laodAd = false
        selectedPlan = index
        val billingClient = BillingClient.newBuilder(requireActivity())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.e("TAG", "ready to purchess")
                    val skuList: MutableList<String> = ArrayList()
                    skuList.add("inapp_plan1")
                    skuList.add("inapp_plan2")
                    skuList.add("inapp_plan3")
                    skuList.add("inapp_plan4")
                    skuList.add("inapp_plan5")
                    skuList.add("inapp_plan6")



                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(
                        params.build()
                    ) { billingResult, skuDetailsList ->
                        try {
//                            progressDialog.dismiss()
                        } catch (c: java.lang.Exception) {
                            c.printStackTrace()
                        }
                        Log.e("TAG", "sku details " + skuDetailsList!!.size)
                        // Process the result.
                        Log.e(
                            "TAG",
                            "skuDetailsList.get(0).getTitle() " + skuDetailsList[0].title
                        )
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList[index])
                            .build()
                        val responseCode = billingClient.launchBillingFlow(
                            requireActivity(),
                            billingFlowParams
                        ).responseCode
                        Log.e("TAG", "responseCode $responseCode")
                    }
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.e("TAG", "service disconnected")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}