package net.amazingapps.llama.android.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @JvmField
    @get:Rule
    val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun appPackageNameTest() {

        assertEquals("net.amazingapps.llama.android.core.test", context.packageName)
    }
}
