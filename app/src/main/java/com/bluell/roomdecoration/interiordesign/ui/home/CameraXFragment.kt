package com.bluell.roomdecoration.interiordesign.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.Response
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOBase64
import com.bluell.roomdecoration.interiordesign.databinding.DialogUseOrRetakeBinding
import com.bluell.roomdecoration.interiordesign.databinding.FragmentCameraXBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class CameraXFragment : Fragment() {
    private var _binding:FragmentCameraXBinding ?= null
    private val binding get() = _binding!!
    private var cameraProvider: ProcessCameraProvider? = null

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: Executor
    private var imageBitmap:Bitmap ?= null
    private var alertDialog: AlertDialog? = null
    @Inject
    lateinit var customProgressBar: CustomProgressBar

    private val generateInteriorViewModel: GenerateInteriorViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraXBinding.inflate(inflater,container,false)
        initObservers()
        init()
        requestPermission()

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.captureButton.setOnClickListener {
            takePhoto { capturedBitmap ->
                imageBitmap = capturedBitmap
                GlobalScope.launch(Dispatchers.IO) {
                    val outputStream = ByteArrayOutputStream()
                    capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                    val compressedByteArray = outputStream.toByteArray()
                    withContext(Dispatchers.Main) {
                        imageBitmap = BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.size)
                        showImageDialog(imageBitmap!!)
                    }
                }
                stopCamera()
            }
        }

        return binding.root
    }

    private fun init(){
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun requestPermission(){
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                var isPermissionDeniedPermanently = false
                for (i in permissions.indices) {
                    val permission = permissions[i]
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (!shouldShowRequestPermissionRationale(permission)) {
                            isPermissionDeniedPermanently = true
                            break
                        }
                    }
                }

                if (isPermissionDeniedPermanently) {
                    // Permission is denied permanently, navigate to app settings
                    showAppSettingsDialog()
                } else {
                    // Show a message to the user
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "This permission is required for using the camera",
                        Snackbar.LENGTH_LONG
                    )
                        .show()

                    findNavController().navigateUp()
                }
            }
        }
    }


    private fun showAppSettingsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("This app requires camera permission. You can grant the permission in the app settings.")
        builder.setPositiveButton("Go to Settings") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { _, _ ->
            // Handle the cancellation as needed
            findNavController().navigateUp()
        }
        builder.setCancelable(false)
        builder.show()
    }


    private fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    if (isAdded){
                        it.setSurfaceProvider(binding.previewView.surfaceProvider)
                    }
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview,imageCapture)

            } catch(exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun takePhoto(callback: (Bitmap) -> Unit) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Set up image capture listener, which is triggered after the photo has been taken
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                // Convert the captured bytes to a Bitmap
                var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    // Code to be executed if Android version is greater than or equal to 11 (Android R)
                    bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                        bitmap.rotate(0f)
                    }else{
                        bitmap.rotate(90f)
                    }

                }
                // Call the callback with the captured Bitmap
                callback(bitmap)

                // Close the ImageProxy to release resources
                image.close()
            }

            override fun onError(exc: ImageCaptureException) {
                Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
            }
        })
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun showImageDialog(bitmap: Bitmap) {
        val builder = AlertDialog.Builder(requireContext())
        val binding = DialogUseOrRetakeBinding.inflate(layoutInflater)
        builder.setView(binding.root)
        alertDialog = builder.create()

        alertDialog?.setCancelable(false)

        binding.takenImage.setImageBitmap(bitmap)

        binding.retake.setOnClickListener {
            setRadioButtonColor(binding.retake)
            binding.useImage.isChecked = false
            alertDialog?.dismiss()
            startCamera()
        }

        binding.useImage.setOnClickListener {
            setRadioButtonColor(binding.useImage)
            binding.retake.isChecked = false

            GlobalScope.launch(Dispatchers.IO) {
                alertDialog?.dismiss()
                withContext(Dispatchers.Main) { // Switch back to UI thread for UI updates
                    customProgressBar.show(requireContext(), requireContext().getString(R.string.uploading_image))
                }
                generateInteriorViewModel.uploadBase64(
                    DTOBase64(saveBitmapToFile(bitmap)!!)
                )
            }

        }

        alertDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        val filename = "drawing.png" // Adjust filename as needed
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null // Handle null case

        val file = File(storageDir, filename)
        try {
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            outputStream.flush()
            outputStream.close()

            val fileSize = file.length()
            val fileSizeKB = fileSize / 1024.0f
            val fileSizeMB = fileSizeKB / 1024.0f

            Log.d("saveBitmapToFile", "Image saved successfully, size: $fileSizeKB KB ($fileSizeMB MB)")
            return file
        } catch (e: Exception) {
            Log.e("saveBitmapToFile", "Error saving bitmap: ${e.message}")
            return null
        }
    }


    fun initObservers(){
        lifecycleScope.launch {
            generateInteriorViewModel.base64Response.collect() { result ->
                when (result) {
                    is Response.Loading -> {
                        Log.e("TAG", "promptType in loading upload")
                    }

                    is Response.Success -> {
                        if (result.data != null) {
                            val imgLink = result.data.image_url
                            Constants.initImage = imgLink
                            findNavController().navigateUp()
                            generateInteriorViewModel.clearBase64Resp()
                            generateInteriorViewModel.clearImageToImage()
                        }
                    }

                    is Response.Error -> {
                        customProgressBar.getDialog()?.dismiss()
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "${result.message}",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }

                    else -> {
                        customProgressBar.getDialog()?.dismiss()
                    }
                }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        customProgressBar.dismiss()
    }

    private fun setRadioButtonColor(view: RadioButton){
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_enabled)
            ), intArrayOf(
                resources.getColor(R.color.dialog_text_color),  // disabled
                resources.getColor(R.color.dialog_text_color) // enabled
            )
        )
        view.buttonTintList = colorStateList
        view.invalidate()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

}