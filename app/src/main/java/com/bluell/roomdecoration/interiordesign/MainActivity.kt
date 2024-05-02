package com.bluell.roomdecoration.interiordesign

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.Constants.TAG
import com.bluell.roomdecoration.interiordesign.common.LocaleManager
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.newUser
import com.bluell.roomdecoration.interiordesign.databinding.ActivityMainBinding
import com.bluell.roomdecoration.interiordesign.ui.localization.LanguageViewModel
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var _navController: NavController? = null
    private lateinit var languageViewModel: LanguageViewModel

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    private val navController get() = _navController!!

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var appDatabase: AppDatabase

    private var userDeviceId: String? = null
    var user: newUser? = null

    private var currentItem: Int = R.id.home_nav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (userDeviceId.isNullOrEmpty()) {
            userDeviceId = Constants.getDeviceId(this@MainActivity)
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_main) as NavHostFragment
        _navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomMenu.setItemSelected(currentItem, true)
        Log.e("TAG", "onCreate1: activity created")
        languageViewModel = ViewModelProvider(this)[LanguageViewModel::class.java]
        init()
        setEvents()
        obserserDatastore()
    }

    private fun init() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (navController.currentDestination?.id) {
                    R.id.exteriorGenerationFragment, R.id.inspireFragment, R.id.fragmentEditDesign -> {
                        navController.navigate(R.id.interiorGenerationFragment)
                        binding.bottomMenu.setItemSelected(R.id.home_nav, true)
                    }

                    R.id.interiorGenerationFragment -> {
                        showExitDialog()
                    }

                    else -> {
                        navController.navigateUp()
                    }
                }
            }
        })

        checkDeviceID()
    }

    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this@MainActivity)
        val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.exit_app_dialog, null)
        builder.setView(view)

        val dialog: AlertDialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.setCancelable(false)

        view.findViewById<Button>(R.id.exitApp)?.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        view.findViewById<Button>(R.id.cancelDialog)?.setOnClickListener {
            // Handle cancel action here
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkDeviceID() {
        firestore.collection("user").document(userDeviceId!!).get().addOnSuccessListener { snap ->
            if (snap.exists()) {
                user = snap.toObject(newUser::class.java)
                val deviceId = snap.getString("deviceID")
                val purchaseToken = snap.getString("purchaseToken")
                val totalGems = snap.getDouble("totalGems")!!.toInt()
                val usedGems = snap.getDouble("usedGems")!!.toInt()
                val updateDate = snap.getString("updateDate")
                val gemsFromSubs = snap.getDouble("gemsFromSubs")!!.toInt()
                val gemsOthers = snap.getDouble("otherGems")!!.toInt()
                val purchaseType = snap.getString("purchaseType")

                if (user?.deviceID == userDeviceId) {
                    mainActivityViewModel.setUser(
                        newUser(
                            deviceID = userDeviceId!!,
                            purchaseToken = purchaseToken!!,
                            totalGems = gemsFromSubs + gemsOthers,
                            usedGems = usedGems,
                            updateDate = updateDate,
                            type = purchaseType!!,
                            gemsFromSub = gemsFromSubs,
                            otherGems = gemsOthers,
                            expiry = ""
                        )
                    )
                }
            } else {
                addUser()
            }
        }

    }

    private fun addUser(isFound: Boolean = false) {
        if (!isFound) {
            val timeStamp = System.currentTimeMillis().toString()

            val userDeviceid = userDeviceId
            val purchaseToken = ""
            val usedGems = 0
            val isTodayReward = false
            val purchaseType = "free"
            val gemsFromSubs = 0
            val otherGems = 10
            val totalGems = gemsFromSubs + otherGems
            val subExpiryDate = ""
            val userMap = mapOf<String, Any>(
                "deviceID" to userDeviceId!!,
                "purchaseToken" to "",
                "totalGems" to gemsFromSubs + otherGems,
                "usedGems" to 0,
                "isTodayReward" to false,
                "updateDate" to timeStamp,
                "purchaseType" to "free",
                "gemsFromSubs" to 0,
                "otherGems" to otherGems,
                "subExpiryDate" to "",
            )
            mainActivityViewModel.setUser(
                newUser(
                    userDeviceId!!,
                    purchaseToken = "",
                    totalGems = totalGems,
                    usedGems = 0,
                    updateDate = timeStamp,
                    isDailyClaimed = false,
                    type = "free",
                    gemsFromSub = gemsFromSubs,
                    otherGems = otherGems,
                    expiry = ""
                )
            )
            firestore.collection("user").document(userDeviceId!!).set(userMap)
                .addOnSuccessListener {
                    Log.e(TAG, "init: user has been created")
                }
        }
    }

    private fun setBottomNavigationTitles(code: String) {
        val context = LocaleManager.setLocale(this, code)
        val resources = context.resources
        val newLocale = Locale(code)
        val resources1 = getResources()
        val configuration = resources1.configuration
        configuration.setLocale(newLocale)
        configuration.setLayoutDirection(Locale(code))
        resources1.updateConfiguration(configuration, resources.displayMetrics)
        if (navController.currentDestination?.id != R.id.splashFragment && navController.currentDestination?.id != R.id.welcomeFragment) {
            binding.bottomMenu.setMenuResource(R.menu.bottom_nav_menu)
            binding.bottomMenu.setItemSelected(currentItem, true)
        }
    }

    fun setEvents() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.interiorGenerationFragment, R.id.exteriorGenerationFragment, R.id.inspireFragment, R.id.fragmentEditDesign -> {
                    binding.bottomMenu.visibility = View.VISIBLE
                }

                else -> {
                    binding.bottomMenu.visibility = View.GONE
                }
            }

            if (destination.id == R.id.fragmentEditDesign) {
                binding.bottomMenu.setItemSelected(R.id.inpaint_nav, true)
            }

        }

        lifecycleScope.launch {
            delay(500)
            if (intent.hasExtra("art") && intent.hasExtra("id")) {
                val response = intent.getStringExtra("art")
                val id = intent.getIntExtra("id", -1)
                if (response != null) {
                    val responseModel =
                        appDatabase.genericResponseDao()?.getCreationsByIdNotLive(id)
                    Bundle().apply {
                        putString("art", Gson().toJson(responseModel))
                        putString("id", responseModel?.id.toString())

                        if (responseModel?.output?.size!! > 1) {
                            navController.navigate(R.id.fullScreenInspireFragment, this)
                        } else {
                            navController.navigate(R.id.fullScreenFragment, this)
                        }
                    }

                }
            }
        }

        binding.bottomMenu.setOnItemSelectedListener { item ->
            when (item) {
                R.id.home_nav -> {
                    currentItem = R.id.home_nav
                    navController.navigate(R.id.interiorGenerationFragment)
                }

                R.id.exterior_nav -> {
                    currentItem = R.id.exterior_nav
                    navController.navigate(R.id.exteriorGenerationFragment)
                }

                R.id.inspire_nav -> {
                    currentItem = R.id.inspire_nav
                    navController.navigate(R.id.inspireFragment)
                }

                R.id.inpaint_nav -> {
                    currentItem = R.id.inpaint_nav
                    navController.navigate(R.id.fragmentEditDesign)
                }

                else -> binding.bottomNavigationView.visibility = View.GONE
            }
            true
        }
    }

    private fun obserserDatastore() {

        languageViewModel.selectedLanguage.observe(this, Observer { language ->
            setBottomNavigationTitles(language)
        })

        mainActivityViewModel.isDark.observe(this) {
            if (it) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        val preferenceDataStoreHelper = UserPreferencesDataStoreHelper(context = this)
        lifecycleScope.launch {
            preferenceDataStoreHelper.getPreference(
                PreferenceDataStoreKeysConstants.FIREBASE_TOKEN,
                ""
            )
                .collect() {
                    if (it.isNotEmpty()) {
                        Log.e("TAG", "observeDataStoreManager: Observed token is $it")

                        Constants.FIREBASE_TOKEN = it
                    }
                }
        }

        lifecycleScope.launch {
            preferenceDataStoreHelper.getPreference(
                PreferenceDataStoreKeysConstants.selectLanguageCode,
                "en"
            )
                .collect() {
                    val context = LocaleManager.setLocale(this@MainActivity, it)
                    val resources = context.resources
                    val newLocale = Locale(it)
                    val resources1 = getResources()
                    val configuration = resources1.configuration
                    configuration.setLocale(newLocale)
                    configuration.setLayoutDirection(Locale(it))
                    window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
                    resources1.updateConfiguration(configuration, resources.displayMetrics)
                    if (navController.currentDestination?.id != R.id.splashFragment && navController.currentDestination?.id != R.id.welcomeFragment) {
                        binding.bottomMenu.setMenuResource(R.menu.bottom_nav_menu)
                        binding.bottomMenu.setItemSelected(currentItem, true)
                    }
                }
        }
    }

//    override fun recreate() {
//        finish()
//        startActivity(intent)
//    }

    override fun onDestroy() {
        super.onDestroy()
        if (!this.isDestroyed) {
            Glide.with(applicationContext).pauseRequests()
        }
    }
}