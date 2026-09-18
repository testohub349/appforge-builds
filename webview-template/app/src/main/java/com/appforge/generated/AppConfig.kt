package com.appforge.generated

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
data class AppConfig(
      val appName: String,
      val websiteUrl: String,
      val theme: ThemeConfig,
      val navigation: NavigationConfig,
      val features: FeatureFlags
  )

@Serializable
data class ThemeConfig(
      val primaryColor: String,
      val secondaryColor: String? = null,
      val darkMode: Boolean = false,
      val splashType: String = "classic",
      val logoAssetPath: String = "splash_logo.png"
  )

@Serializable
data class NavigationConfig(
      val drawerItems: List<NavItem> = emptyList(),
      val bottomNavItems: List<NavItem> = emptyList()
      )

@Serializable
data class NavItem(
      val id: String,
      val label: String,
      val iconAssetPath: String? = null,
      val targetUrl: String
  )

@Serializable
data class FeatureFlags(
      val fileUpload: Boolean = true,
      val fileDownload: Boolean = true,
      val cameraCapture: Boolean = true,
      val pushNotifications: Boolean = false,
      val pullToRefresh: Boolean = true,
      val shareButton: Boolean = false,
      val whatsappButton: Boolean = false,
      val whatsappNumber: String? = null
  )

object AppConfigLoader {
      private var cached: AppConfig? = null

      fun load(context: Context): AppConfig {
                cached?.let { return it }
                        val json = context.assets.open("app_config.json").bufferedReader().use { it.readText() }
                                val config = Json { ignoreUnknownKeys = true }.decodeFromString<AppConfig>(json)
                                        cached = config
                return config
      }
}
