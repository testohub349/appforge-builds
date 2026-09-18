package com.appforge.generated

import android.app.Application

class AppForgeApplication : Application() {
      override fun onCreate() {
                super.onCreate()
                        AppConfigLoader.load(this)
      }
}
