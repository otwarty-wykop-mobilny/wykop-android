package io.github.wykopmobilny.tests

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.wykopmobilny.R
import io.github.wykopmobilny.tests.pages.AboutDialogRegion
import io.github.wykopmobilny.tests.pages.AppearanceSettingsPage
import io.github.wykopmobilny.tests.pages.MainPage
import io.github.wykopmobilny.tests.pages.SettingsPage
import io.github.wykopmobilny.tests.responses.callsOnAppStart
import io.github.wykopmobilny.ui.modules.mainnavigation.MainNavigationActivity
import io.github.wykopmobilny.utils.assertLinkHandled
import io.github.wykopmobilny.utils.interceptingIntents
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest : BaseActivityTest() {

    @Test
    fun navigation() {
        mockWebServerRule.callsOnAppStart()
        launchActivity<MainNavigationActivity>()
        Espresso.onIdle()

        MainPage.openDrawer()
        MainPage.tapDrawerOption(R.id.about)

        interceptingIntents {
            AboutDialogRegion.tapAppInfo()

            assertLinkHandled("https://github.com/otwarty-wykop-mobilny/wykop-android")
        }

        MainPage.openDrawer()
        MainPage.tapDrawerOption(R.id.nav_settings)
        SettingsPage.assertVisible()

        SettingsPage.tapAppearance()
        AppearanceSettingsPage.assertVisible()

        Espresso.pressBack()
        SettingsPage.assertVisible()

        Espresso.pressBack()
        MainPage.openDrawer()
        MainPage.closeDrawer()
    }
}
