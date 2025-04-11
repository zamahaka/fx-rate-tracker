package com.example.fxratetracker.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.datastore.preferences.preferencesDataStore
import com.example.fxratetracker.BuildConfig
import com.example.fxratetracker.data.remote.ApiKeyInterceptor
import com.example.fxratetracker.data.remote.service.ExchangeRateService
import com.example.fxratetracker.data.remote.source.FxRateRemoteDataSource
import com.example.fxratetracker.data.repository.DataStoreSelectedAssetsRepository
import com.example.fxratetracker.data.repository.RealAssetsRepository
import com.example.fxratetracker.data.repository.RealFxRateRepository
import com.example.fxratetracker.domain.usecase.AutorefreshSelectedFxRates
import com.example.fxratetracker.domain.usecase.ObserveSearchableAssets
import com.example.fxratetracker.domain.usecase.ObserveSelectedFxRates
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListPresenter
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListScreen
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListScreenUi
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsPresenter
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreenUi
import com.example.fxratetracker.presentation.ui.theme.FXRateTrackerTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecorationFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import kotlin.time.Duration.Companion.seconds

private const val API_HOST = "api.exchangerate.host"
private const val API_URL = "https://$API_HOST/"
private const val API_KEY_NAME = "access_key"

// Yeah, yeah, this should not be stored in APK altogether.
// As just by decompiling apk and running string on it will show plain api key.
// We can obfuscate it, like storing string bytes and then recreating it,
// but it is a futile attempt, as anyone can deobfuscate it. Its just an extra step
//
// Ideally api that requires api key usage should be proxied.
private const val API_KEY = BuildConfig.API_KEY

private val FX_RATE_REFRESH_PERIOD = BuildConfig.FX_RATE_REFRESH_PERIOD_SECONDS.seconds

private val Context.dataStore by preferencesDataStore(name = "store")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val json = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }

        val retrofit = buildRetrofit(json)

        val service = retrofit.create<ExchangeRateService>()
        val fxRateRemoteDataSource = FxRateRemoteDataSource(service)

        val fxRepository = RealFxRateRepository(dataStore, fxRateRemoteDataSource, json)
        val assetsRepository = RealAssetsRepository(dataStore, service, json)
        val selectedAssetsRepository = DataStoreSelectedAssetsRepository(dataStore)

        val observeSearchableAssets = ObserveSearchableAssets(assetsRepository)
        val observeSelectedFxRates = ObserveSelectedFxRates(
            Dispatchers.IO,
            selectedAssetsRepository, assetsRepository, fxRepository
        )
        val autorefreshSelectedFxRates = AutorefreshSelectedFxRates(
            Dispatchers.IO, FX_RATE_REFRESH_PERIOD,
            fxRepository, selectedAssetsRepository,
        )

        val circuit = Circuit.Builder()
            .addPresenterFactory(
                FxRateListPresenter.Factory(
                    observeSelectedFxRates,
                    autorefreshSelectedFxRates,
                )
            )
            .addUi<FxRateListScreen, FxRateListScreen.State> { state, modifier ->
                FxRateListScreenUi(state, modifier)
            }
            .addPresenterFactory(
                SelectAssetsPresenter.Factory(
                    assetsRepository, selectedAssetsRepository, observeSearchableAssets,
                ),
            )
            .addUi<SelectAssetsScreen, SelectAssetsScreen.State> { state, modifier ->
                SelectAssetsScreenUi(state, modifier)
            }
            .build()

        setContent {
            FXRateTrackerTheme {
                val backStack = rememberSaveableBackStack(root = FxRateListScreen)
                val navigator = rememberCircuitNavigator(backStack)

                CircuitCompositionLocals(circuit) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        NavigableCircuitContent(
                            navigator, backStack,
                            decoratorFactory = remember(navigator) {
                                GestureNavigationDecorationFactory(onBackInvoked = navigator::pop)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = Level.BODY
            })
            .addInterceptor(ApiKeyInterceptor(API_HOST, API_KEY_NAME, API_KEY))
            .build()
    }

    private fun buildRetrofit(json: Json): Retrofit {
        return Retrofit.Builder()
            .client(buildClient())
            .baseUrl(API_URL)
            .addConverterFactory(
                json.asConverterFactory("application/json; charset=UTF8".toMediaType()),
            )
            .build()
    }
}