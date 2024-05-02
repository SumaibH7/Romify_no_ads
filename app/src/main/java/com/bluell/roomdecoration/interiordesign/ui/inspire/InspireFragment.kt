package com.bluell.roomdecoration.interiordesign.ui.inspire

//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.FullScreenContentCallback
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.OnUserEarnedRewardListener
//import com.google.android.gms.ads.rewarded.RewardedAd
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.ImageGenerationDialog
import com.bluell.roomdecoration.interiordesign.common.RecyclerviewItemDecore
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOGenSingleton
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOObjectGenerate
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.DummyModelInspirations
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.FragmentInspireBinding
import com.bluell.roomdecoration.interiordesign.ui.history.HistoryViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class InspireFragment : Fragment() {

    private var _binding: FragmentInspireBinding? = null
    private val binding get() = _binding!!
    val TAG = "INSPIRATION"
//    private var rewardedAd: RewardedAd? = null

    @Inject
    lateinit var customProgressBar: CustomProgressBar

    @Inject
    lateinit var imageGenerationDialog: ImageGenerationDialog

    var dtoObject: DTOObjectGenerate = DTOGenSingleton.getInstance()

    private val viewModel: HistoryViewModel by viewModels()

    private var arrayListMyCreations: ArrayList<genericResponseModel?> = arrayListOf()

    @Inject
    lateinit var appDatabase: AppDatabase

    private val generateInteriorViewModel: InspireGenerationViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspireBinding.inflate(inflater, container, false)

        binding.root.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val heightDiff = binding.root.height - (bottom - top)
            if (heightDiff > 100) {
                binding.generatebtn.visibility = View.GONE
            } else {
                if (binding.promptEdt.text.isNotEmpty()) {
                    binding.generatebtn.visibility = View.VISIBLE
                }
            }
        }
        return binding.root
    }


    private fun setEvents() {

        binding.toolbar.titleName.text = requireContext().getString(R.string.inspire)
        val maxCharCount = 100
        binding.promptEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    binding.generatebtn.visibility = View.VISIBLE
                } else {
                    binding.generatebtn.visibility = View.GONE
                }
                val currentCharCount = s?.length ?: 0
                binding.currentCharacters.text = "$currentCharCount/$maxCharCount"
            }
        })


//        binding.toolbar.goPro.setOnClickListener {
//            Snackbar.make(
//                requireActivity().findViewById(android.R.id.content),
//                "Coming Soon!",
//                Snackbar.LENGTH_LONG
//            )
//                .show()
////            findNavController().navigate(R.id.premiumFragment)
//        }

        binding.generatebtn.setOnClickListener {
            if (binding.promptEdt.text.isNotEmpty()) {
//                customProgressBar.show(
//                    requireContext(),
//                    requireContext().getString(R.string.loading_ad)
//                )
//                fullScreenContentCallBack()
//                loadRewarded()
                dtoObject.prompt =
                    " _ ${binding.promptEdt.text.toString()}"
                dtoObject.token = Constants.FIREBASE_TOKEN
                dtoObject.enhance_prompt = "no"
                dtoObject.upscale = "no"
                dtoObject.endpoint = "v4/dreambooth"
                generateInteriorViewModel.generateTextToImg(
                    dtoObject = dtoObject
                )
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Please enter a prompt!",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }

        binding.clearEdt.setOnClickListener {
            binding.promptEdt.setText("")
        }
        binding.toolbar.settings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }


    }

    fun initObservers() {
        lifecycleScope.launch {
            generateInteriorViewModel.textToImgResp.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        imageGenerationDialog.show(requireContext())
                    }

                    is Response.Success -> {
                        val imageToImageResponse = result.data
                        if (imageToImageResponse != null && imageToImageResponse.output!!.isNotEmpty()) {
                            imageToImageResponse.meta.init_image = dtoObject.init_image
                            imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
                            imageToImageResponse.endpoint = Constants.INSPIRE_END_POINT
                            if (imageToImageResponse.id == null) {
                                imageToImageResponse.id = System.currentTimeMillis().toInt()
                            }
                            appDatabase.genericResponseDao()?.saveData(
                                imageToImageResponse
                            )
                            generateInteriorViewModel.clearTextToImage()
                            Constants.clear()

                            Bundle().apply {
                                putString("art", Gson().toJson(imageToImageResponse))
                                putString("id", imageToImageResponse.id.toString())

                                findNavController().navigate(
                                    R.id.fullScreenInspireFragment,
                                    this
                                )
                            }
                            imageGenerationDialog.dismissDialog()

                        }
                    }

                    is Response.Processing -> {
                        val imageToImageResponse = result.data
                        if (imageToImageResponse != null) {
                            imageToImageResponse.meta.init_image = dtoObject.init_image
                            imageToImageResponse.meta.type = Constants.INTERIOR_END_POINT
                            imageToImageResponse.endpoint = Constants.INSPIRE_END_POINT

                            if (imageToImageResponse.id == null) {
                                imageToImageResponse.id = System.currentTimeMillis().toInt()
                            }
                            appDatabase.genericResponseDao()?.saveData(
                                imageToImageResponse
                            )
                            Bundle().apply {
                                putString("art", Gson().toJson(imageToImageResponse))
                                putString("id", imageToImageResponse.id.toString())

                                findNavController().navigate(
                                    R.id.fullScreenInspireFragment,
                                    this
                                )
                            }
                        }
                        imageGenerationDialog.dismissDialog()
                    }

                    is Response.Error -> {
                        generateInteriorViewModel.clearTextToImage()
                        Constants.clear()
                        imageGenerationDialog.dismissDialog()
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "An error occurred! Please try again later!",
                            Snackbar.LENGTH_LONG
                        )
                            .show()

                        Log.e("TAG", "initObservers: ${result.message}")
                    }
                }
            }
        }


        lifecycleScope.launch {
            viewModel.allCreations.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Response.Loading -> {}

                    is Response.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            arrayListMyCreations = arrayListOf()
                            result.data.forEachIndexed { _, genericResponseModel ->
                                if (genericResponseModel.output!!.isNotEmpty()) {
                                    arrayListMyCreations.add(genericResponseModel)
                                }
                            }
                        }
                    }

                    is Response.Error -> {
                    }

                    else -> {
                    }
                }
            }
        }


        appDatabase.genericResponseDao()?.getAllCreationsLive(1, "")?.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                it?.let { data ->
                    arrayListMyCreations = arrayListOf()
                    data.forEachIndexed { _, genericResponseModel ->
                        if (genericResponseModel.output!!.isNotEmpty()) {
                            arrayListMyCreations.add(genericResponseModel)
                        }
                    }
                }
            }
        }
    }

//    private fun fullScreenContentCallBack() {
//        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
//            override fun onAdClicked() {
//                // Called when a click is recorded for an ad.
//                Log.d("ADLOAD", "Ad was clicked.")
//            }
//
//            override fun onAdDismissedFullScreenContent() {
//                // Called when ad is dismissed.
//                // Set the ad reference to null so you don't show the ad a second time.
//                Log.d("ADLOAD", "Ad dismissed fullscreen content.")
//                rewardedAd = null
//            }
//
//            override fun onAdImpression() {
//                // Called when an impression is recorded for an ad.
//                Log.d("ADLOAD", "Ad recorded an impression.")
//            }
//
//            override fun onAdShowedFullScreenContent() {
//                // Called when ad is shown.
//                Log.d("ADLOAD", "Ad showed fullscreen content.")
//            }
//        }
//    }

//    private fun loadRewarded() {
//        val adRequest = AdRequest.Builder().build()
//        RewardedAd.load(
//            requireActivity(),
//            Constants.REWARD_AD_ID,
//            adRequest,
//            object : RewardedAdLoadCallback() {
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    Log.d("ADLOAD", adError.toString())
//                    rewardedAd = null
//                    customProgressBar.dismiss()
//                    Snackbar.make(
//                        requireActivity().findViewById(android.R.id.content),
//                        "An error occurred! Please try again later! Check your internet Connection",
//                        Snackbar.LENGTH_LONG
//                    )
//                        .show()
////                    if (binding.promptEdt.text.isNotEmpty()) {
////                        dtoObject.prompt =
////                            " _ ${binding.promptEdt.text.toString()}"
////                        dtoObject.token = Constants.FIREBASE_TOKEN
////                        dtoObject.enhance_prompt = "no"
////                        dtoObject.upscale = "no"
////                        dtoObject.endpoint = "v4/dreambooth"
////                        generateInteriorViewModel.generateTextToImg(
////                            dtoObject = dtoObject
////                        )
////                    }else {
////                        Snackbar.make(
////                            requireActivity().findViewById(android.R.id.content),
////                            "Please enter a prompt!",
////                            Snackbar.LENGTH_LONG
////                        )
////                            .show()
////                    }
//                }
//
//                override fun onAdLoaded(ad: RewardedAd) {
//                    Log.d("ADLOAD", "Ad was loaded.")
//                    rewardedAd = ad
//                    customProgressBar.dismiss()
//                    isRewardEarned()
//                }
//            })
//    }

//    private fun isRewardEarned() {
//        // Implement your reward logic here (refer to ad network's documentation)
//        rewardedAd?.let { ad ->
//            ad.show(requireActivity(), OnUserEarnedRewardListener { _ ->
//                // Handle the reward.
//                Constants.REWARD = true
//                if (binding.promptEdt.text.isNotEmpty()) {
//                    dtoObject.prompt =
//                        " _ ${binding.promptEdt.text.toString()}"
//                    dtoObject.token = Constants.FIREBASE_TOKEN
//                    dtoObject.enhance_prompt = "no"
//                    dtoObject.upscale = "no"
//                    dtoObject.endpoint = "v4/dreambooth"
//                    generateInteriorViewModel.generateTextToImg(
//                        dtoObject = dtoObject
//                    )
//                }else {
//                    Snackbar.make(
//                        requireActivity().findViewById(android.R.id.content),
//                        "Please enter a prompt!",
//                        Snackbar.LENGTH_LONG
//                    )
//                        .show()
//                }
//                Log.d("ADLOAD", "User earned the reward.")
//            })
//        } ?: run {
//            Log.d("ADLOAD", "The rewarded ad wasn't ready yet.")
//        }
//    }

    private fun getInspirations(): ArrayList<DummyModelInspirations> {
        val list = ArrayList<DummyModelInspirations>()
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_1),
                R.drawable.in_style_1
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_2),
                R.drawable.in_style_2
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_3),
                R.drawable.in_style_3
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_4),
                R.drawable.in_style_4
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_5),
                R.drawable.in_style_5
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_6),
                R.drawable.in_style_6
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_7),
                R.drawable.in_style_7
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_8),
                R.drawable.in_style_8
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_9),
                R.drawable.in_style_9
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_10),
                R.drawable.in_style_10
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_11),
                R.drawable.in_style_11
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_12),
                R.drawable.in_style_12
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_13),
                R.drawable.in_style_13
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_14),
                R.drawable.in_style_14
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_15),
                R.drawable.in_style_15
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_16),
                R.drawable.in_style_16
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_17),
                R.drawable.in_sytle_17
            )
        )
        list.add(
            DummyModelInspirations(
                requireActivity().getString(R.string.inspiration_18),
                R.drawable.in_style_18
            )
        )
        return list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initInspirations()
        initObservers()
        setEvents()
    }


    private fun initInspirations() {
        binding.recyclerInspirations.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = InspirationsAdapter(getInspirations(),
            object : InspirationsAdapter.SelectedInspirations {
                override fun inspirations(item: DummyModelInspirations, position: Int) {
                    binding.promptEdt.setText("")
                    binding.promptEdt.setText(item.prompt)
                    binding.scroll.scrollTo(0, 0)
                }
            })

        binding.recyclerInspirations.adapter = adapter
        binding.recyclerInspirations.addItemDecoration(RecyclerviewItemDecore(2, 10, false))
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}