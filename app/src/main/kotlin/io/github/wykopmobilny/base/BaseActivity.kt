package io.github.wykopmobilny.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.android.support.DaggerAppCompatActivity
import io.github.wykopmobilny.R
import io.github.wykopmobilny.ui.dialogs.showExceptionDialog
import io.github.wykopmobilny.utils.preferences.SettingsPreferences
import io.github.wykopmobilny.utils.preferences.SettingsPreferencesApi

/**
 * This class should be extended in all activities in this app. Place global-activity settings here.
 */
abstract class BaseActivity : DaggerAppCompatActivity() {

    open val enableSwipeBackLayout: Boolean = false
    open val isActivityTransfluent: Boolean = false
    var isRunning = false
    lateinit var rxPermissions: RxPermissions
    private val themeSettingsPreferences: SettingsPreferencesApi by lazy { SettingsPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        initTheme()
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(this)
        if (enableSwipeBackLayout) {
            Slidr.attach(
                this,
                SlidrConfig.Builder().edge(true).build()
            )
        }
    }

    override fun onResume() {
        isRunning = true
        super.onResume()
    }

    override fun onPause() {
        isRunning = false
        super.onPause()
    }

    // This function initializes activity theme based on settings
    private fun initTheme() {
        if (themeSettingsPreferences.useDarkTheme) {
            if (themeSettingsPreferences.useAmoledTheme) {
                setTheme(R.style.WykopAppTheme_Amoled)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark_Amoled)
            } else {
                setTheme(R.style.WykopAppTheme_Dark)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark_Dark)
            }
        } else {
            setTheme(R.style.WykopAppTheme)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        }
        if (isActivityTransfluent || enableSwipeBackLayout) {
            theme.applyStyle(R.style.TransparentActivityTheme, true)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        when (themeSettingsPreferences.fontSize) {
            "tiny" -> theme.applyStyle(R.style.TextSizeTiny, true)
            "small" -> theme.applyStyle(R.style.TextSizeSmall, true)
            "normal" -> theme.applyStyle(R.style.TextSizeNormal, true)
            "large" -> theme.applyStyle(R.style.TextSizeLarge, true)
            "huge" -> theme.applyStyle(R.style.TextSizeHuge, true)
        }
    }

    fun showErrorDialog(e: Throwable) {
        if (isRunning) showExceptionDialog(e)
    }
}
