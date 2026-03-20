package net.amazingapps.llama.android.sample.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LlamaAndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LlamaAndroidApp)
            modules(appModule)
        }
    }
}
