package net.amazingapps.llama_android.sample.app

import com.arm.aichat.AiChat
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import net.amazingapps.llama_android.sample.app.repository.ModelDownloader
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory {
        HttpClient(CIO) {
            engine {
                requestTimeout = 0
            }
        }
    }

    factory { ModelDownloader(get(), get()) }
    
    single { AiChat.getInferenceEngine(get()) }

    viewModel { MainViewModel(get(), get()) }
}
