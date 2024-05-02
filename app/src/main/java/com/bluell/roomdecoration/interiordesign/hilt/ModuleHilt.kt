package com.bluell.roomdecoration.interiordesign.hilt

import android.content.Context
import androidx.work.WorkManager
import com.bluell.roomdecoration.interiordesign.MyApplication
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.CustomProgressBar
import com.bluell.roomdecoration.interiordesign.common.ImageGenerationDialog
import com.bluell.roomdecoration.interiordesign.common.InspireGenertionDialog
import com.bluell.roomdecoration.interiordesign.common.SocketFactoryWithTcpNoDelay
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.data.remote.EndPointsInterface
import com.bluell.roomdecoration.interiordesign.data.repoistry.DeleteAllHistoryRepoImpli
import com.bluell.roomdecoration.interiordesign.data.repoistry.DeleteFavrepoImpli
import com.bluell.roomdecoration.interiordesign.data.repoistry.FetchDataRepositoryImpl
import com.bluell.roomdecoration.interiordesign.data.repoistry.FetchFavoritesDataRepoImpl
import com.bluell.roomdecoration.interiordesign.data.repoistry.RoomInteriorDesignRepositoryImpl
import com.bluell.roomdecoration.interiordesign.data.repoistry.UpdateGemsRespositryImpl
import com.bluell.roomdecoration.interiordesign.domain.repo.DeleteAllHistory
import com.bluell.roomdecoration.interiordesign.domain.repo.DeletefavoriteHistory
import com.bluell.roomdecoration.interiordesign.domain.repo.FetchDataRepository
import com.bluell.roomdecoration.interiordesign.domain.repo.FetchFavDataRepositry
import com.bluell.roomdecoration.interiordesign.domain.repo.InteriorDesignRepoistry
import com.bluell.roomdecoration.interiordesign.domain.repo.UpdateGemsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.http.params.HttpConnectionParams
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuleHilt {

    @Singleton
    @Provides
    fun providesWebApiInterface(): EndPointsInterface {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val gson = GsonBuilder()
            .setLenient()
            .create()
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
            .socketFactory(SocketFactoryWithTcpNoDelay())
            .addInterceptor(logging)
            .addInterceptor(Interceptor { chain ->
                val original: Request = chain.request()
                val originalHttpUrl: HttpUrl = original.url
                val url = originalHttpUrl.newBuilder()
                    .build()
                val requestBuilder: Request.Builder = original.newBuilder()
                    .url(url)
                val request: Request = requestBuilder.build()
                chain.proceed(request)
            })
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
        return Retrofit.Builder().baseUrl(Constants.BASE_URL_OUR)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build().create(EndPointsInterface::class.java)
    }

    @Provides
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideTextToImgRepository(endPointsInterface: EndPointsInterface): InteriorDesignRepoistry {
        return RoomInteriorDesignRepositoryImpl(endPointsInterface)
    }


    @Provides
    fun provideFetchDataRepository(
        endPointsInterface: EndPointsInterface,
        appDatabase: AppDatabase
    ): FetchDataRepository {
        return FetchDataRepositoryImpl(endPointsInterface, appDatabase)
    }


    @Provides
    fun probideFetchFavDataRepo(
        endPointsInterface: EndPointsInterface,
        appDatabase: AppDatabase
    ): FetchFavDataRepositry {
        return FetchFavoritesDataRepoImpl(endPointsInterface, appDatabase)
    }

    @Provides
    fun probideDeleteAllHistoryRepo(
        endPointsInterface: EndPointsInterface,
        appDatabase: AppDatabase
    ): DeleteAllHistory {
        return DeleteAllHistoryRepoImpli(endPointsInterface, appDatabase)
    }


    @Provides
    fun provideDeleteFavHistoryRepo(
        endPointsInterface: EndPointsInterface,
        appDatabase: AppDatabase
    ): DeletefavoriteHistory {
        return DeleteFavrepoImpli(endPointsInterface, appDatabase)
    }

    @Provides
    fun providesAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase(context)
    }

    @Provides
    fun provideUpdateGemsRepository(firestore: FirebaseFirestore): UpdateGemsRepository {
        return UpdateGemsRespositryImpl(firestore = firestore)
    }

    @Provides
    fun providesCustomProgressBar(): CustomProgressBar {
        return CustomProgressBar()
    }


    @Provides
    fun providesImageGenerationDialog(): ImageGenerationDialog {
        return ImageGenerationDialog()
    }


    @Provides
    fun providesInspireGenerationDialog(): InspireGenertionDialog {
        return InspireGenertionDialog()
    }


    @Provides
    @Singleton
    fun providesDatabaseReference(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun providesApplication(@ApplicationContext app: Context): MyApplication {
        return app as MyApplication
    }
}