package com.bluell.roomdecoration.interiordesign.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.Settings
import android.widget.ImageView
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.Languagemodel
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.RoomTypeModel

object Constants {

    const val BASE_URL_OUR = "http://edecator.com/Romify/"
    var FIREBASE_TOKEN = ""
    var maskedImage: String? = null
    var initImageInterior: String? = null
    var initImageExterior: String? = null
    var initImageEdit: String? = null
    var initImage: String? = null
    var ROOM_TYPE = 0
    var ROOM_STYLE = 0
    var HOUSE_ANGLE = 0

    var INTERIOR_END_POINT = "interior"
    var EXTERIOR_END_POINT = "exterior"
    var EDIT_END_POINT = "edit"
    var INSPIRE_END_POINT = "inspire"
    var REWARD = false
//    var REWARD_AD_ID = "ca-app-pub-3940256099942544/5224354917" //test ID
    var REWARD_AD_ID = "ca-app-pub-5887559234735462/4770719215"
    var isMasked: Boolean = false
    const val FILE_TYPE = "image"
    const val TAG = "ROMIFY_ANDROID_APP"
    const val LINK_TO_GP = "https://play.google.com/store/apps/developer?id=Swed+AI"

    const val NEGATIVE_PROMPT =
        "(((nudity))),(((nsfw))),(((nude))),(((upskirt))),(((bra))),(((nude))),(((latex))),(((boobs))),(((panty))),(((ass))),painting, extra fingers, mutated hands, poorly drawn hands, poorly drawn face, deformed, ugly, blurry, bad anatomy, bad proportions, extra limbs, cloned face, skinny, glitchy, double torso, extra arms, extra hands, mangled fingers, missing lips, ugly face, distorted face, extra legs, anime, dual, multiple, " +
                "cluttered, messy, chaotic, disorganized, dilapidated, outdated, mismatched furniture, poor lighting, cramped spaces, uneven floors, crooked walls, unrealistic proportions, excessive clutter, overly crowded, depressing ambiance, uninviting, lack of harmony, unsettling atmosphere, unappealing color scheme, outdated decor, creepy, eerie, abandoned, neglected, rundown, unsanitary conditions, excessive dirt, broken fixtures, " +
                "dysfunctional layout, unlivable conditions, lack of functionality, impractical design, oppressive atmosphere, unsettling vibes, low-resolution, artifacting, unrealistic scale, missing walls, missing furniture, missing doors/windows, non-functional doorways, overlapping furniture, unnatural shadows, skewed perspective"

    var bitmap: Bitmap? = null

    fun combineBitmapsWithDrawing(originalBitmap: Bitmap, drawingBitmap: Bitmap): Bitmap {
        val combinedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(combinedBitmap)
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        }

        // Convert the drawingBitmap to a BitmapDrawable to draw on the canvas
        val drawingDrawable = BitmapDrawable(drawingBitmap)

        // Set the bounds of the drawingDrawable to match the canvas size
        drawingDrawable.setBounds(0, 0, canvas.width, canvas.height)

        // Draw the drawingDrawable on the canvas using the specified paint
        drawingDrawable.draw(canvas)

        return combinedBitmap
    }

    fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IllegalArgumentException("Image URI is invalid")
    }

    fun clear() {
        initImage = null
        initImageEdit = null
        initImageInterior = null
        initImageExterior = null
    }
    fun clearInterior() {
        initImageInterior = null
    }
    fun clearExterior() {
        initImageExterior = null
    }
    fun clearEdit() {
        initImageEdit = null
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getScaleType(imgHeight: Int, imgWidth: Int, imvHeight: Int, imvWidth: Int): ImageView.ScaleType {
        return if (imgWidth < imvWidth && imgHeight < imvHeight) ImageView.ScaleType.CENTER_INSIDE else ImageView.ScaleType.CENTER_CROP
    }

    fun getRoomTypesList(context: Context, selectedPosition: Int): ArrayList<RoomTypeModel> {
        val roomTypes = ArrayList<RoomTypeModel>()
        val roomTypeNames = arrayOf(
            context.getString(R.string.living_room),
            context.getString(R.string.dining_room),
            context.getString(R.string.kitchen),
            context.getString(R.string.lounge),
            context.getString(R.string.hallway),
            context.getString(R.string.study_room),
            context.getString(R.string.office),
            context.getString(R.string.attic),
            context.getString(R.string.gaming),
            context.getString(R.string.toilet),
            context.getString(R.string.bathroom),
            context.getString(R.string.restaurant)
        )

        val roomTypeImages = arrayOf(
            R.drawable.type_living_room, R.drawable.type_dinning_room, R.drawable.type_kitchen_room,
            R.drawable.type_lounge_room, R.drawable.type_hallway_room, R.drawable.type_study_room,
            R.drawable.type_office_room, R.drawable.type_attic_room, R.drawable.type_gaming_room,
            R.drawable.type_toilet_room, R.drawable.type_bath_room, R.drawable.type_resturent_room
        )

        for (i in roomTypeNames.indices) {
            val isSelected = (selectedPosition == i)
            roomTypes.add(RoomTypeModel(roomTypeNames[i], roomTypeImages[i], isSelected))
        }

        return roomTypes
    }

    fun getRoomStyles(context: Context,selectedPosition: Int): ArrayList<RoomTypeModel> {
        val roomTypes = ArrayList<RoomTypeModel>()
        val roomTypeNames = arrayOf(
            context.getString(R.string.minimalist),
            context.getString(R.string.tropical),
            context.getString(R.string.japanese),
            context.getString(R.string.halloween),
            context.getString(R.string.farmhouse),
            context.getString(R.string.rustic),
            context.getString(R.string.cyberpunk),
            context.getString(R.string.industrial),
            context.getString(R.string.bohemian),
            context.getString(R.string.vintage),
            context.getString(R.string.modern),
            context.getString(R.string.zen)
        )

        val roomTypeImages = arrayOf(
            R.drawable.style_minimalist, R.drawable.style_tropical, R.drawable.style_japanese,
            R.drawable.style_halloween, R.drawable.style_farmhouse, R.drawable.style_rustic,
            R.drawable.style_cyberpunk, R.drawable.style_industrial, R.drawable.style_bohemian,
            R.drawable.style_vintage, R.drawable.style_modern, R.drawable.style_zen
        )

        for (i in roomTypeNames.indices) {
            val isSelected = (selectedPosition == i)
            roomTypes.add(RoomTypeModel(roomTypeNames[i], roomTypeImages[i], isSelected))
        }

        return roomTypes
    }

    fun getLanguageList(pos:String): ArrayList<Languagemodel> {
      val languagesList = ArrayList<Languagemodel>()

        languagesList.add(Languagemodel("English (US)", "en", R.drawable.flag_us, false))
        languagesList.add(Languagemodel("English (UK)", "en", R.drawable.flag_uk, false))
        languagesList.add(Languagemodel("French", "fr", R.drawable.flag_france, false))
        languagesList.add(Languagemodel("German", "de", R.drawable.flag_germany, false))
        languagesList.add(Languagemodel("Japanese", "ja", R.drawable.flag_japanese, false))
        languagesList.add(Languagemodel("Korean", "ko", R.drawable.flag_korean, false))
        languagesList.add(Languagemodel("Portuguese", "pt", R.drawable.flag_portgal, false))
        languagesList.add(Languagemodel("Spanish", "es", R.drawable.flag_spain, false))
        languagesList.add(Languagemodel("Arabic", "ar", R.drawable.flag_arabic, false))
        languagesList.add(Languagemodel("Chinese", "zh", R.drawable.flag_chinese, false))
        languagesList.add(Languagemodel("Italian", "it", R.drawable.flag_italian, false))
        languagesList.add(Languagemodel("Russian", "ru", R.drawable.flag_russian, false))
        languagesList.add(Languagemodel("Thai", "th", R.drawable.flag_thai, false))
        languagesList.add(Languagemodel("Turkish", "tr", R.drawable.flag_turkey, false))
        languagesList.add(Languagemodel("Vietnamese", "vi", R.drawable.flag_vietnamese, false))
        languagesList.add(Languagemodel("Hindi", "hi", R.drawable.flag_hindi, false))
        languagesList.add(Languagemodel("Persian", "fa", R.drawable.flag_farsi, false))
        languagesList.add(Languagemodel("Urdu", "ur", R.drawable.flag_pakistan, false))
        languagesList.add(Languagemodel("Dutch", "nl", R.drawable.flag_dutch, false))
        languagesList.add(Languagemodel("Indonesian", "in", R.drawable.flag_indonesian, false))

        for (item in languagesList) {
         if (item.lan_code == pos) {
            item.isSelected_lan =true
            break
         }
      }
      return languagesList
   }
}