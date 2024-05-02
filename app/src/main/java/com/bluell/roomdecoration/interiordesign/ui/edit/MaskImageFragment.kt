package com.bluell.roomdecoration.interiordesign.ui.edit

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.Constants.TAG
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.databinding.FragmentMaskImageBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shouheng.compress.Compress
import me.shouheng.compress.concrete
import me.shouheng.compress.strategy.config.ScaleMode
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class MaskImageFragment : Fragment() {

    private var _binding: FragmentMaskImageBinding? = null
    private val binding get() = _binding!!


    private var type: String? = null
    var bitmap: Bitmap? = null

    private val inPaintViewModel: InPaintViewModel by viewModels()

    @Inject
    lateinit var customProgressBar: CustomProgressBar


    private var initImage = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMaskImageBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getPrevData()
        setEvents()
        initObservers()
    }

    fun setEvents() {

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.brushToll.setOnClickListener {
            binding.drawingView.initializePen()
            binding.brushToll.icon = ContextCompat.getDrawable(requireContext(),R.drawable.mask_brush_filled)
            binding.eraseTool.icon = ContextCompat.getDrawable(requireContext(),R.drawable.mask_erase)
            binding.drawingView.penColor = ContextCompat.getColor(requireContext(), R.color.white)
            binding.drawingView.setPenSize(40f)
        }
        binding.eraseTool.setOnClickListener {
            binding.brushToll.icon = ContextCompat.getDrawable(requireContext(),R.drawable.mask_brush)
            binding.eraseTool.icon = ContextCompat.getDrawable(requireContext(),R.drawable.mask_eraser_filled)
            binding.drawingView.initializeEraser()
            binding.drawingView.setEraserSize(40f)
        }

        binding.applyMask.setOnClickListener {
            if (binding.progressBar.visibility == View.GONE) {
                GlobalScope.launch(Dispatchers.Main) {
                    if (isAdded) {
                        Constants.bitmap = Constants.combineBitmapsWithDrawing(
                            bitmap!!,
                            binding.drawingView.getBmp()!!
                        )
                        inPaintViewModel.uploadBase64(DTOBase64(saveBitmapToFile(binding.drawingView.getBitmap())!!))
                    }
                }
            }
        }

        binding.undoMask.setOnClickListener {
            binding.drawingView.undo()
        }
    }


    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        val filename = "drawing.png" // Adjust filename as needed
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: return null // Handle null case

        val file = File(storageDir, filename)
        try {
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return file
        } catch (e: Exception) {
            Log.e("saveBitmapToFile", "Error saving bitmap: ${e.message}")
            return null
        }
    }


    private fun getPrevData() {
        arguments?.let {
            type = it.getString("type")
            if (type == "create") {
                initImage = Constants.initImageEdit!!
                GlobalScope.launch(Dispatchers.Main) {
                    Glide.with(activity?.applicationContext!!).asBitmap().load(initImage)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                if (isAdded){
                                    binding.progressBar.visibility = View.GONE
                                    binding.buttons.visibility = View.VISIBLE
                                    binding.applyMask.visibility = View.VISIBLE
                                    binding.undoMask.visibility = View.VISIBLE
                                    bitmap = resource
                                    binding.actualImg.setImageBitmap(bitmap)
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }
            } else {
                Log.d(TAG, "getPrevData: " + it.getString("init_image"))
                initImage = it.getString("init_image")!!
                Constants.initImageEdit = initImage
                Glide.with(activity?.applicationContext!!)
                    .asBitmap()
                    .load(it.getString("init_image"))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            bitmap = resource
                            GlobalScope.launch {
                                bitmap = Compress.with(requireContext(), bitmap!!)
                                    .setQuality(50)
                                    .concrete {
                                        withScaleMode(ScaleMode.SCALE_HEIGHT)
                                        withIgnoreIfSmaller(true)
                                    }
                                    .asBitmap()
                                    .get(Dispatchers.IO)
                                withContext(Dispatchers.Main) {
                                    if (isAdded){
                                        if (bitmap != null) {
                                            binding.progressBar.visibility = View.GONE
                                            binding.buttons.visibility = View.VISIBLE
                                            binding.applyMask.visibility = View.VISIBLE
                                            binding.undoMask.visibility = View.VISIBLE
                                            binding.actualImg.setImageBitmap(bitmap)
                                        } else {
                                            Log.d(TAG, "getPrevData1: " + it.getString("init_image"))
                                        }
                                    }

                                }

                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }
            binding.brushToll.icon = ContextCompat.getDrawable(requireContext(),R.drawable.mask_brush_filled)
            binding.eraseTool.icon = ContextCompat.getDrawable(requireContext(),R.drawable.mask_erase)
            binding.drawingView.initializePen()
            binding.drawingView.penColor =
                ContextCompat.getColor(requireContext(), R.color.white)
            binding.drawingView.setPenSize(40f)
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            inPaintViewModel.base64Response.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        customProgressBar.show(
                            requireContext(),
                            requireContext().getString(R.string.uploading_image)
                        )
                    }

                    is Response.Success -> {
                        if (result.data != null) {
                            customProgressBar.getDialog()?.dismiss()
                            Constants.maskedImage = result.data.image_url
                            Constants.isMasked = true
                            inPaintViewModel.clearBase64Resp()

                            if (type == "create") {
                                if (findNavController().currentDestination?.id == R.id.maskImageFragment) {
                                    findNavController().popBackStack(
                                        R.id.fragmentEditDesign,
                                        inclusive = false
                                    )
                                }
                            } else {
                                findNavController().navigate(R.id.action_maskImageFragment_to_fragmentEditDesign)
                            }
                        }

                    }

                    is Response.Error -> {
                        customProgressBar.getDialog()?.dismiss()
                        Log.d(TAG, "initObservers: ${result.message}")
                    }

                    else -> {
                        customProgressBar.getDialog()?.dismiss()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}