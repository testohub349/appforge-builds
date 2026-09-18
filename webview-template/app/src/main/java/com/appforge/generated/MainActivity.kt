package com.appforge.generated

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.appforge.generated.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

      private lateinit var binding: ActivityMainBinding
      private lateinit var config: AppConfig
      private lateinit var drawerToggle: ActionBarDrawerToggle

      override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                        binding = ActivityMainBinding.inflate(layoutInflater)
                                setContentView(binding.root)

                                        config = AppConfigLoader.load(this)

                                                applyTheme()
                                                        setSupportActionBar(binding.toolbar)
                                                                supportActionBar?.title = config.appName

                setupDrawer()
                        setupBottomNav()

                                if (savedInstanceState == null) {
                                              val landingUrl = config.navigation.bottomNavItems.firstOrNull()?.targetUrl
                                                  ?: config.navigation.drawerItems.firstOrNull()?.targetUrl
                                                  ?: config.websiteUrl
                                              loadDestination(landingUrl)
                                }
      }

          private fun applyTheme() {
                    val primary = runCatching { Color.parseColor(config.theme.primaryColor) }
                                .getOrDefault(Color.parseColor("#1F5FB0"))
                                        binding.toolbar.setBackgroundColor(primary)
                                                binding.bottomNav.setBackgroundColor(primary)

                                                        val header = binding.navigationView.getHeaderView(0)
                                                                header.setBackgroundColor(primary)
                                                                        header.findViewById<TextView>(R.id.navHeaderAppName)?.text = config.appName

                    if (config.theme.darkMode) {
                                  delegate.localNightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                    }
          }

              private fun setupDrawer() {
                        val items = config.navigation.drawerItems
                        if (items.isEmpty()) {
                                      binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                                                  return
                        }

                                binding.navigationView.menu.clear()
                                        items.forEachIndexed { index, item ->
                                                      binding.navigationView.menu.add(Menu.NONE, index, index, item.label).apply {
                                                                        isCheckable = true
                                                      }
                                        }
                                                binding.navigationView.setNavigationItemSelectedListener { menuItem ->
                                                              val item = items[menuItem.itemId]
                                                              loadDestination(item.targetUrl)
                                                                          binding.drawerLayout.closeDrawer(GravityCompat.START)
                                                                                      menuItem.isChecked = true
                                                              true
                                                }

                                                        drawerToggle = ActionBarDrawerToggle(
                                                                      this, binding.drawerLayout, binding.toolbar,
                                                                      R.string.drawer_open, R.string.drawer_close
                                                                  )
                                                                binding.drawerLayout.addDrawerListener(drawerToggle)
                                                                        drawerToggle.syncState()
              }

                  private fun setupBottomNav() {
                            val items = config.navigation.bottomNavItems
                            if (items.isEmpty()) {
                                          binding.bottomNav.visibility = android.view.View.GONE
                                          return
                            }

                                    binding.bottomNav.visibility = android.view.View.VISIBLE
                            binding.bottomNav.menu.clear()
                                    items.forEachIndexed { index, item ->
                                                  binding.bottomNav.menu.add(Menu.NONE, index, index, item.label)
                                    }
                                            binding.bottomNav.setOnItemSelectedListener { menuItem ->
                                                          loadDestination(items[menuItem.itemId].targetUrl)
                                                                      true
                                            }
                  }

                      private fun loadDestination(url: String) {
                                val fragment = WebAppFragment.newInstance(url)
                                        supportFragmentManager.beginTransaction()
                                                    .replace(R.id.fragmentContainer, fragment)
                                                                .commit()
                      }

                          override fun onBackPressed() {
                                    val webFragment = supportFragmentManager
                                        .findFragmentById(R.id.fragmentContainer) as? WebAppFragment
                                    when {
                                                  binding.drawerLayout.isDrawerOpen(GravityCompat.START) ->
                                                      binding.drawerLayout.closeDrawer(GravityCompat.START)
                                                                  webFragment?.handleBackPress() == true -> Unit
                                                  else -> super.onBackPressed()
                                    }
                          }
}
