package com.appforge.generated

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.appforge.generated.databinding.FragmentWebAppBinding
import java.io.File

class WebAppFragment : Fragment() {

      private var _binding: FragmentWebAppBinding? = null
      private val binding get() = _binding!!

      private lateinit var config: AppConfig
      private var pendingUploadCallback: ValueCallback<Array<Uri>>? = null
      private var pendingCameraUri: Uri? = null

      private val fileChooserLauncher: ActivityResultLauncher<Intent> =
          registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        val callback = pendingUploadCallback
                        pendingUploadCallback = null
                        if (callback == null) return@registerForActivityResult

                        if (result.resultCode != android.app.Activity.RESULT_OK) {
                                          callback.onReceiveValue(null)
                                                          return@registerForActivityResult
                        }
                                    val data = result.data
                        val results: Array<Uri> = when {
                                          data?.clipData != null -> {
                                                                (0 until data.clipData!!.itemCount)
                                                                                        .map { data.clipData!!.getItemAt(it).uri }
                                                                                                                .toTypedArray()
                                          }
                                                          data?.data != null -> arrayOf(data.data!!)
                                                                          pendingCameraUri != null -> arrayOf(pendingCameraUri!!)
                                                                                          else -> emptyArray()
                        }
                                    callback.onReceiveValue(results)
          }

              private val cameraPermissionLauncher: ActivityResultLauncher<String> =
          registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                        if (!granted) {
                                          Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
                        }
          }

              companion object {
                        private const val ARG_URL = "url"
                        fun newInstance(url: String) = WebAppFragment().apply {
                                      arguments = Bundle().apply { putString(ARG_URL, url) }
                        }
              }

                  override fun onCreateView(
                            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                          ): View {
                            _binding = FragmentWebAppBinding.inflate(inflater, container, false)
                                    return binding.root
                  }

                      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                                super.onViewCreated(view, savedInstanceState)
                                        config = AppConfigLoader.load(requireContext())

                                                setupWebView()
                                                        setupSwipeRefresh()

                                                                val url = arguments?.getString(ARG_URL) ?: config.websiteUrl
                                binding.webView.loadUrl(resolveUrl(url))
                      }

                          private fun resolveUrl(url: String): String =
          if (url.startsWith("http://") || url.startsWith("https://")) url
          else config.websiteUrl.trimEnd('/') + "/" + url.trimStart('/')

              private fun setupWebView() {
                        val webView: WebView = binding.webView
                        webView.settings.apply {
                                      javaScriptEnabled = true
                                      domStorageEnabled = true
                                      databaseEnabled = true
                                      loadWithOverviewMode = true
                                      useWideViewPort = true
                                      mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                      cacheMode = WebSettings.LOAD_DEFAULT
                                      setSupportZoom(true)
                                                  builtInZoomControls = true
                                      displayZoomControls = false
                        }
                                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

                                        webView.webViewClient = object : WebViewClient() {
                                                      override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                                                        val requestUrl = request.url.toString()
                                                                                        val ownHost = Uri.parse(config.websiteUrl).host
                                                                        return if (Uri.parse(requestUrl).host == ownHost) {
                                                                                              false
                                                                        } else {
                                                                                              runCatching {
                                                                                                                        startActivity(Intent(Intent.ACTION_VIEW, request.url))
                                                                                              }
                                                                                                                  true
                                                                        }
                                                      }

                                                                  override fun onPageFinished(view: WebView, url: String) {
                                                                                    super.onPageFinished(view, url)
                                                                                                    binding.swipeRefresh.isRefreshing = false
                                                                  }
                                        }

                                                webView.webChromeClient = object : WebChromeClient() {
                                                              override fun onShowFileChooser(
                                                                                webView: WebView,
                                                                                filePathCallback: ValueCallback<Array<Uri>>,
                                                                                fileChooserParams: FileChooserParams
                                                                            ): Boolean {
                                                                                if (!config.features.fileUpload) return false
                                                                                pendingUploadCallback = filePathCallback

                                                                                val intents = mutableListOf<Intent>()

                                                                                                if (config.features.cameraCapture && hasCameraPermission()) {
                                                                                                                      createCameraCaptureIntent()?.let { intents.add(it) }
                                                                                                } else if (config.features.cameraCapture) {
                                                                                                                      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                                                                }

                                                                                                                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                                                                                                                      addCategory(Intent.CATEGORY_OPENABLE)
                                                                                                                                                          type = "*/*"
                                                                                                                                      putExtra(Intent.EXTRA_ALLOW_MULTIPLE, fileChooserParams.mode == FileChooserParams.MODE_OPEN_MULTIPLE)
                                                                                                                                                          val acceptTypes = fileChooserParams.acceptTypes
                                                                                                                                      if (acceptTypes.isNotEmpty() && acceptTypes[0].isNotEmpty()) {
                                                                                                                                                                putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes)
                                                                                                                                      }
                                                                                                                }
                                                                                                                
                                                                                                                                val chooser = Intent(Intent.ACTION_CHOOSER).apply {
                                                                                                                                                      putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                                                                                                                                                                          putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_file))
                                                                                                                                                                                              if (intents.isNotEmpty()) {
                                                                                                                                                                                                                        putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
                                                                                                                                                                                                                                            }
                                                                                                                                                                                                              }
                                                                                                                                
                                                                                                                                                fileChooserLauncher.launch(chooser)
                                                                                                                                                                return true
                                                              }
                                                }

                                                        webView.setDownloadListener { url, _, contentDisposition, mimeType, _ ->
                                                                      if (!config.features.fileDownload) return@setDownloadListener
                                                                      startDownload(url, contentDisposition, mimeType)
                                                        }
              }

                  private fun startDownload(url: String, contentDisposition: String?, mimeType: String?) {
                            val context = requireContext()
                                    val fileName = guessFileName(url, contentDisposition, mimeType)

                                            val request = DownloadManager.Request(Uri.parse(url)).apply {
                                                          setMimeType(mimeType)
                                                                      val cookie = CookieManager.getInstance().getCookie(url)
                                                                                  addRequestHeader("cookie", cookie)
                                                                                              addRequestHeader("User-Agent", binding.webView.settings.userAgentString)
                                                                                                          setDescription(getString(R.string.downloading_file))
                                                                                                                      setTitle(fileName)
                                                                                                                                  setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                                                                                                              setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                                                                                                                                          setAllowedOverMetered(true)
                                                                                                                                                                      setAllowedOverRoaming(true)
                                            }

                                                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            downloadManager.enqueue(request)
                                    Toast.makeText(context, getString(R.string.download_started, fileName), Toast.LENGTH_SHORT).show()
                  }

                      private fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
                                val fromHeader = contentDisposition
                                    ?.substringAfter("filename=", "")
                                                ?.trim('"')
                                                            ?.takeIf { it.isNotBlank() }
                                                                    if (fromHeader != null) return fromHeader

                                val lastSegment = Uri.parse(url).lastPathSegment
                                if (!lastSegment.isNullOrBlank() && lastSegment.contains(".")) return lastSegment

                                val ext = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: "bin"
                                return "download_${System.currentTimeMillis()}.$ext"
                      }

                          private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
                                    requireContext(), Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

      private fun createCameraCaptureIntent(): Intent? {
                val photoFile = runCatching {
                              File.createTempFile(
                                                "capture_${System.currentTimeMillis()}", ".jpg",
                                                requireContext().externalCacheDir?.apply { mkdirs() }
                                                            )
                }.getOrNull() ?: return null

                val photoUri = FileProvider.getUriForFile(
                              requireContext(), "${requireContext().packageName}.fileprovider", photoFile
                          )
                        pendingCameraUri = photoUri

                return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                              putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                                          addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
      }

          private fun setupSwipeRefresh() {
                    binding.swipeRefresh.isEnabled = config.features.pullToRefresh
                    binding.swipeRefresh.setOnRefreshListener { binding.webView.reload() }
          }

              fun handleBackPress(): Boolean {
                        val webView = _binding?.webView ?: return false
                        return if (webView.canGoBack()) {
                                      webView.goBack()
                                                  true
                        } else {
                                      false
                        }
              }

                  override fun onDestroyView() {
                            super.onDestroyView()
                                    _binding = null
                  }
}
