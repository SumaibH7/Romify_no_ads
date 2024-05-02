package com.bluell.roomdecoration.interiordesign.ui.favorites.exterior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.RecyclerviewItemDecore
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.FragmentExteriorFavoritesFragmengtBinding
import com.bluell.roomdecoration.interiordesign.domain.repo.MyInteriorClicks
import com.bluell.roomdecoration.interiordesign.ui.favorites.FavHistoryAdapter
import com.bluell.roomdecoration.interiordesign.ui.favorites.FavHistoryViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExteriorFavoritesFragment : Fragment() {

    private var _binding: FragmentExteriorFavoritesFragmengtBinding? = null
    private val binding get() = _binding!!

    private var arrayListMyCreations: MutableList<genericResponseModel>? = mutableListOf()
    private var myCreationAdapter: FavHistoryAdapter? = null

    @Inject
    lateinit var appDatabase: AppDatabase
    private val viewModel: FavHistoryViewModel by viewModels()

    private var _navController: NavController? = null
    private val navController get() = _navController!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentExteriorFavoritesFragmengtBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHistory.addItemDecoration(
            RecyclerviewItemDecore(
                2,
                20,
                false
            )
        )
        viewModel.getAllCreations("exterior")
        initObservers()
    }


    private fun initObservers() {
        appDatabase.genericResponseDao()?.getAllFavoritesLive("exterior")
            ?.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    binding.emptySupport.visibility = View.GONE
                    binding.rvHistory.visibility = View.VISIBLE
                    it?.let { data ->
                        arrayListMyCreations = mutableListOf()
                        data.forEachIndexed { _, genericResponseModel ->
                            if (genericResponseModel.output!!.isNotEmpty()) {
                                arrayListMyCreations?.add(genericResponseModel)
                            }
                        }

                        initMyCreations()
                        myCreationAdapter?.notifyDataSetChanged()
                    }
                } else {
                    binding.emptySupport.visibility = View.VISIBLE
                    binding.rvHistory.visibility = View.GONE
                }
            }

        lifecycleScope.launch {
            viewModel.favCreations.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Response.Loading -> {
                        binding.emptySupport.visibility = View.GONE
                        binding.rvHistory.visibility = View.VISIBLE
                    }

                    is Response.Success -> {
                        binding.emptySupport.visibility = View.GONE
                        binding.rvHistory.visibility = View.VISIBLE
                        if (!result.data.isNullOrEmpty()) {
                            arrayListMyCreations = mutableListOf()
                            result.data.forEachIndexed { _, genericResponseModel ->
                                if (genericResponseModel.output!!.isNotEmpty()) {
                                    arrayListMyCreations?.add(genericResponseModel)
                                }
                            }

                            initMyCreations()
                            myCreationAdapter?.notifyDataSetChanged()
                        }
                    }

                    is Response.Error -> {
                        binding.emptySupport.visibility = View.VISIBLE
                        binding.rvHistory.visibility = View.GONE
                    }

                    else -> {
                        binding.emptySupport.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun initMyCreations() {

        myCreationAdapter = arrayListMyCreations?.let {
            FavHistoryAdapter(it, object :
                MyInteriorClicks {
                override fun onMenuItemClick(
                    item: genericResponseModel?,
                    position: Int,
                    anchor: View
                ) {
                    val oldData =
                        appDatabase.genericResponseDao()?.getCreationsByIdNotLive(item?.id!!)
                    if (oldData?.isFavorite == true) {
                        oldData.isFavorite = false
                        appDatabase.genericResponseDao()?.UpdateData(oldData)

                    }
                }

                override fun onItemClick(item: genericResponseModel?, position: Int) {
                    Bundle().apply {
                        this.putString("art", Gson().toJson(item))
                        this.putString("id", item!!.id.toString())
                        findNavController().navigate(R.id.fullScreenFragment, this)
                    }
                }
            })
        }
        binding.rvHistory.layoutManager =
            GridLayoutManager(requireContext(), 2)
        binding.rvHistory.adapter = myCreationAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}