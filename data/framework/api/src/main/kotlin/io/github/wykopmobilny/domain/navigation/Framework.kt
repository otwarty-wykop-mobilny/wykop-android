package io.github.wykopmobilny.domain.navigation

interface Framework {

    fun appRestarter(): AppRestarter

    fun nightModeDetector(): SystemSettingsDetector

    fun youtubeAppDetector(): YoutubeAppDetector

    fun htmlUtils(): WykopTextUtils

    fun clipboardService(): ClipboardService
}
