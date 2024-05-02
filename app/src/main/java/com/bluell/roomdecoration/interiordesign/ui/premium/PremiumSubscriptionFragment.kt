package com.bluell.roomdecoration.interiordesign.ui.premium

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
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.databinding.FragmentPremiumBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PremiumSubscriptionFragment : Fragment() {
    private var _binding:FragmentPremiumBinding ?= null
    private val binding get() = _binding!!

    var productsList = ArrayList<ProductDetails>()

    private var _navController: NavController?=null
    private val navController get() = _navController!!

    var priceWeekly = ""
    var priceMonthly = ""
    var priceYearly = ""


    var resposne = ""
    var sku = ""
    var des = ""

    var selectedIndex = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPremiumBinding.inflate(inflater,container,false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        _navController=findNavController()
        setEvents()
        getPrices()
    }


    fun initUi(){
        Glide.with(this)
            .asGif() // Specify that you're loading a GIF
            .load(R.raw.earned_reward_anim) // Replace with the resource ID of your GIF
            .transition(DrawableTransitionOptions.withCrossFade()) // Optional crossfade animation
            .into(binding.premiumAnimation)
    }


    fun setEvents(){
        binding.weekly.setOnClickListener {
            selectedIndex = 0
            updateSelection(0)

        }

        binding.yearly.setOnClickListener {
            selectedIndex = 2
            updateSelection(2)

        }

        binding.monthly.setOnClickListener {
            selectedIndex = 1
            updateSelection(1)


        }


        binding.continePurchase.setOnClickListener {
            launchSubscriptionFlowFromGp()
        }

        binding.backBtn.setOnClickListener {
            navController.navigateUp()
        }
    }


    fun updateSelection(selectedIndex: Int) {
        val selectedColor = resources.getColor(R.color.premium_stroke_selected)
        val unselectedColor = resources.getColor(R.color.premium_stroke_unselected)
        val bulletPointsColor = resources.getColor(R.color.bullet_points_color)

        when (selectedIndex) {
            0 -> {
                binding.weekly.strokeColor = selectedColor
                binding.weeklyPrice.setTextColor(selectedColor)
                binding.weeklyTxt.setTextColor(selectedColor)
                binding.monthly.strokeColor = unselectedColor
                binding.monthlyPrice.setTextColor(bulletPointsColor)
                binding.monthlyTxt.setTextColor(bulletPointsColor)
                binding.yearly.strokeColor = unselectedColor
                binding.yearlyPrice.setTextColor(bulletPointsColor)
                binding.yearlyTxt.setTextColor(bulletPointsColor)
                binding.saleText.setTextColor(bulletPointsColor)
            }
            1 -> {
                binding.monthly.strokeColor = selectedColor
                binding.monthlyTxt.setTextColor(selectedColor)
                binding.monthlyPrice.setTextColor(selectedColor)
                binding.weekly.strokeColor = unselectedColor
                binding.weeklyPrice.setTextColor(bulletPointsColor)
                binding.weeklyTxt.setTextColor(bulletPointsColor)
                binding.yearly.strokeColor = unselectedColor
                binding.yearlyPrice.setTextColor(bulletPointsColor)
                binding.yearlyTxt.setTextColor(bulletPointsColor)
                binding.saleText.setTextColor(selectedColor)
            }
            2 -> {
                binding.yearly.strokeColor = selectedColor
                binding.yearlyPrice.setTextColor(selectedColor)
                binding.yearlyTxt.setTextColor(selectedColor)
                binding.monthly.strokeColor = unselectedColor
                binding.monthlyPrice.setTextColor(bulletPointsColor)
                binding.monthlyTxt.setTextColor(bulletPointsColor)
                binding.weekly.strokeColor = unselectedColor
                binding.weeklyPrice.setTextColor(bulletPointsColor)
                binding.weeklyTxt.setTextColor(bulletPointsColor)
                binding.saleText.setTextColor(bulletPointsColor)
            }
        }
    }


    private fun launchSubscriptionFlowFromGp(){
        Log.e("TAG", "launchSubscriptionFlowFromGp: $selectedIndex")
        val billingClient = BillingClient.newBuilder(requireActivity())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Handle billing service disconnect
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val productList: List<QueryProductDetailsParams.Product> = when(selectedIndex) {
                    0 -> listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("weekly_sub_plan")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )

                    1 -> listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("monthly_sub_plan")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )

                    2 -> listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("yearly_sub_plan")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )

                    else -> listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("monthly_sub_plan")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                }

                val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
                billingClient.queryProductDetailsAsync(params) { billingResult1, productDetailsList ->
                    for (productDetails in productDetailsList) {
                        val offerToken =
                            productDetails.subscriptionOfferDetails?.get(0)?.offerToken
                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken!!)
                                .build()
                        )
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()
                        billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
                    }
                }
            }
        })
    }

//    private fun setSelection(selectedView: View, selectedText: TextView) {
//        val unselectedStrokeColor = resources.getColor(R.color.premium_card_stroke_color)
//        val selectedStrokeColor = resources.getColor(R.color.premium_card_selected_color)
//        val unselectedBackground = resources.getDrawable(R.drawable.premium_text_bg_unselected)
//        val selectedBackground = resources.getDrawable(R.drawable.premium_text_background)
//
//        val views = listOf(
//            binding.weekly to binding.weeklyTxt,
//            binding.monthly to binding.monthlyTxt,
//            binding.yearly to binding.yearlyTxt
//        )
//
//        for ((view, text) in views) {
//            if (view == selectedView) {
//                view.strokeColor = selectedStrokeColor
//                text.background = selectedBackground
//            } else {
//                view.strokeColor = unselectedStrokeColor
//                text.background = unselectedBackground
//            }
//        }
//    }


    fun getPrices(){
        lifecycleScope.launch {
            val billingClient = BillingClient.newBuilder(requireActivity())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
            billingClient.startConnection(object : BillingClientStateListener {


                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.e("TAG", "onBillingSetupFinished: ", )
                        val productList = listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("weekly_sub_plan")
                                .setProductType(BillingClient.SkuType.SUBS)
                                .build(),
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("monthly_sub_plan")
                                .setProductType(BillingClient.SkuType.SUBS)
                                .build(),
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("yearly_sub_plan")
                                .setProductType(BillingClient.SkuType.SUBS)
                                .build()
                        )

                        val params = QueryProductDetailsParams.newBuilder()
                            .setProductList(productList)
                            .build()

                        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                            productDetailsList.forEach { productDetails ->
                                resposne = productDetails.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice!!
                                sku = productDetails.name
                                val id = productDetails.productId
                                val ds = productDetails.description
                                des = "$sku $ds Price $resposne"

                                Log.e(
                                    "TAG",
                                    "onProductDetailsResponse: respinse  $resposne"
                                )
                                Log.e(
                                    "TAG",
                                    "onProductDetailsResponse: sku $sku"
                                )
                                Log.e(
                                    "TAG",
                                    "onProductDetailsResponse: ds$ds"
                                )
                                Log.e(
                                    "TAG",
                                    "onProductDetailsResponse: des$des"
                                )

                                when (id) {
                                    "weekly_sub_plan" -> priceWeekly = resposne
                                    "monthly_sub_plan" -> priceMonthly = resposne
                                    "yearly_sub_plan" -> priceYearly = resposne
                                }

                                productsList.add(productDetails)

                                lifecycleScope.launch(Dispatchers.Main) {
                                    binding.monthlyPrice.text = priceMonthly
                                    binding.weeklyPrice.text = priceWeekly
                                    binding.yearlyPrice.text = priceYearly

                                }






                                Log.e("TAG", "run: onProductDetailsResponse weekly:$priceWeekly")
                                Log.e("TAG", "run: onProductDetailsResponse monthly:$priceMonthly")
                                Log.e("TAG", "run: onProductDetailsResponse yearly:$priceYearly")

                            }
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Handle billing service disconnect
                }
            })

        }

    }


    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, Purchase ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && Purchase != null) {

            when(selectedIndex){
                0->{
//                    totalGems+=1400
//                    userObj?.gemsFromSub=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }
                1->{
//                    totalGems+=6000
//                    userObj?.gemsFromSub=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }
                2->{
//                    totalGems+=20000
//                    userObj?.gemsFromSub=totalGems
//                    userObj?.totalGems = userObj?.gemsFromSub?.plus(userObj?.otherGems!!)!!
//                    mainActivityViewModel.updateInAPPGems(userObj!!)
                }
            }
//            mainActivityViewModel.setUser(userObj!!)

            for (purchase in Purchase) {
//                    handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Toast.makeText(requireContext(), "Already purchased", Toast.LENGTH_SHORT).show()
        }else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED){
            Toast.makeText(requireContext(), "purchase Canceled", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}