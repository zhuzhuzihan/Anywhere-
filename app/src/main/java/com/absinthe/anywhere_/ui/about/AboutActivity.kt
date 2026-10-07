package com.absinthe.anywhere_.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.absinthe.anywhere_.AppBarActivity
import com.absinthe.anywhere_.BuildConfig
import com.absinthe.anywhere_.R
import com.absinthe.anywhere_.constants.GlobalValues
import com.absinthe.anywhere_.databinding.ActivityAboutBinding
import com.absinthe.anywhere_.databinding.ItemAboutCardBinding
import com.absinthe.anywhere_.databinding.LayoutAboutHeaderBinding
import com.absinthe.anywhere_.utils.handler.URLSchemeHandler
import com.absinthe.anywhere_.utils.manager.DialogManager.showDebugDialog
import com.absinthe.anywhere_.utils.manager.URLManager
import com.absinthe.libraries.me.Absinthe
import timber.log.Timber

/** M3E About screen: header + standalone tonal cards (FolkPatch-style). No drakeet dependency. */
class AboutActivity : AppBarActivity<ActivityAboutBinding>() {

  private var mClickCount = 0
  private var mStartTime = 0L
  private var mEndTime = 0L
  private lateinit var headerBinding: LayoutAboutHeaderBinding

  override fun setViewBinding() = ActivityAboutBinding.inflate(layoutInflater)

  override fun getToolBar() = binding.toolbar.toolBar

  override fun getAppBarLayout() = binding.toolbar.appBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    addHeader()
    addTextCard(getString(R.string.about_text), null)
    addRow(
      photo = R.mipmap.pic_rabbit,
      title = "zhuzhuzihan",
      summary = getString(R.string.developer_info),
      url = MAINTAINER_URL
    )
    addRow(
      photo = R.mipmap.pic_android_links,
      title = getString(R.string.android_links_title),
      summary = "https://androidlinks.org/",
      url = "https://androidlinks.org/"
    )
    addRow(
      icon = R.drawable.ic_green_android,
      tintIcon = false,
      title = getString(R.string.green_android_title),
      summary = "https://green-android.org/",
      url = "https://green-android.org/"
    )
    addHtmlCard(
      "Telegram: <a href=\"t.me/anywhereee\">t.me/anywhereee</a><br>E-mail: <a href=\"mailto:${Absinthe.EMAIL}\">${Absinthe.EMAIL}</a>"
    )
    LICENSES.forEach { (name, by, url) ->
      addRow(
        title = name,
        summary = by,
        url = url
      )
    }
  }

  private fun addHeader() {
    headerBinding = LayoutAboutHeaderBinding.inflate(layoutInflater, binding.container, false)
    headerBinding.ivIcon.apply {
      if (BuildConfig.BETA) {
        setImageResource(R.mipmap.ic_launcher_beta)
      } else {
        setImageResource(R.drawable.pic_splash)
      }
      setOnClickListener(createDebugListener())
    }
    headerBinding.tvSlogan.text = getString(R.string.slogan)
    headerBinding.tvVersion.text = String.format("Version: %s", BuildConfig.VERSION_NAME)
    binding.container.addView(headerBinding.root)
  }

  private fun addRow(
    icon: Int? = null,
    photo: Int? = null,
    tintIcon: Boolean = true,
    title: String,
    summary: String? = null,
    url: String? = null
  ) {
    val row = ItemAboutCardBinding.inflate(layoutInflater, binding.container, false)
    if (icon != null) {
      row.ivIcon.isVisible = true
      row.ivIcon.setImageResource(icon)
      if (!tintIcon) {
        row.ivIcon.imageTintList = null
      }
    } else {
      row.ivIcon.isVisible = false
    }
    if (photo != null) {
      row.ivPhoto.isVisible = true
      row.ivPhoto.setImageResource(photo)
    } else {
      row.ivPhoto.isVisible = false
    }
    row.tvTitle.text = title
    if (summary != null) {
      row.tvSummary.isVisible = true
      row.tvSummary.text = summary
    } else {
      row.tvSummary.isVisible = false
    }
    if (url != null) {
      row.ivChevron.isVisible = true
      row.root.setOnClickListener { openUrl(url) }
    }
    binding.container.addView(row.root)
  }

  private fun addTextCard(body: String, url: String?) {
    val row = ItemAboutCardBinding.inflate(layoutInflater, binding.container, false)
    row.ivIcon.isVisible = false
    row.ivPhoto.isVisible = false
    row.tvTitle.setTextAppearance(R.style.TextAppearance_Aw_BodyLarge)
    row.tvTitle.text = body
    row.tvSummary.isVisible = false
    if (url != null) {
      row.ivChevron.isVisible = true
      row.root.setOnClickListener { openUrl(url) }
    }
    binding.container.addView(row.root)
  }

  private fun addHtmlCard(html: String) {
    val row = ItemAboutCardBinding.inflate(layoutInflater, binding.container, false)
    row.ivIcon.isVisible = false
    row.ivPhoto.isVisible = false
    row.tvTitle.isVisible = false
    row.tvSummary.isVisible = true
    row.tvSummary.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    row.tvSummary.movementMethod = LinkMovementMethod.getInstance()
    binding.container.addView(row.root)
  }

  private fun openUrl(url: String) {
    val uri = if (url.startsWith("http")) url else "https://$url"
    try {
      CustomTabsIntent.Builder().build().apply {
        launchUrl(this@AboutActivity, uri.toUri())
      }
    } catch (e: ActivityNotFoundException) {
      Timber.e(e)
      try {
        startActivity(Intent(Intent.ACTION_VIEW).apply { data = uri.toUri() })
      } catch (e: ActivityNotFoundException) {
        Timber.e(e)
      }
    }
  }

  private fun createDebugListener(): View.OnClickListener {
    mClickCount = 0
    mEndTime = 0
    mStartTime = mEndTime

    return View.OnClickListener {
      mEndTime = System.currentTimeMillis()

      if (mEndTime - mStartTime > 500) {
        mClickCount = 0
      } else {
        mClickCount++
      }

      mStartTime = mEndTime

      if (mClickCount == 9) {
        if (GlobalValues.sIsDebugMode) {
          try {
            val inputStream = assets.open("renge.webp")
            headerBinding.ivIcon.setImageBitmap(
              BitmapFactory.decodeStream(inputStream)
            )
            headerBinding.tvSlogan.text = "えい、私もよ。"
            window.statusBarColor = getColor(R.color.renge)

            val fd = assets.openFd("renge_no_koe.aac")
            MediaPlayer().apply {
              setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
              prepare()
              start()
            }
          } catch (e: Exception) {
            Timber.e(e)
          }
        } else {
          GlobalValues.sIsDebugMode = true
          showDebugDialog(this)
        }
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.about_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
    if (menuItem.itemId == R.id.toolbar_rate) {
      try {
        URLSchemeHandler.parse(this, URLManager.ANYWHERE_MARKET_URL)
      } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
      }
    } else if (menuItem.itemId == android.R.id.home) {
      finish()
    }
    return super.onOptionsItemSelected(menuItem)
  }

  companion object {
    const val MAINTAINER_URL = "https://github.com/zhuzhuzihan"

    private val LICENSES = listOf(
      Triple("Kotlin", "JetBrains · Apache 2.0", "https://github.com/JetBrains/kotlin"),
      Triple("Shizuku-API", "Rikka · MIT", "https://github.com/RikkaApps/Shizuku-API"),
      Triple("Sui", "RikkaApps · GPLv3", "https://github.com/RikkaApps/Sui"),
      Triple("libsu", "topjohnwu · Apache 2.0", "https://github.com/topjohnwu/libsu"),
      Triple("MultiType", "drakeet · Apache 2.0", "https://github.com/drakeet/MultiType"),
      Triple(
        "FullDraggableDrawer",
        "drakeet · Apache 2.0",
        "https://github.com/PureWriter/FullDraggableDrawer"
      ),
      Triple(
        "glide",
        "bumptech · BSD, part MIT and Apache 2.0",
        "https://github.com/bumptech/glide"
      ),
      Triple(
        "AndResGuard",
        "shwenzhang · Apache 2.0",
        "https://github.com/shwenzhang/AndResGuard"
      ),
      Triple(
        "Delegated-Scopes-Manager",
        "heruoxin · WTFPL",
        "https://github.com/heruoxin/Delegated-Scopes-Manager"
      ),
      Triple(
        "IceBox-SDK",
        "heruoxin · Apache 2.0",
        "https://github.com/heruoxin/IceBox-SDK"
      ),
      Triple(
        "Robfuscate",
        "heruoxin · Apache 2.0",
        "https://github.com/heruoxin/Robfuscate"
      ),
      Triple("Once", "jonfinerty · Apache 2.0", "https://github.com/jonfinerty/Once"),
      Triple(
        "BaseRecyclerViewAdapterHelper",
        "CymChad · MIT",
        "https://github.com/CymChad/BaseRecyclerViewAdapterHelper"
      ),
      Triple("colorpicker", "QuadFlask · Apache 2.0", "https://github.com/QuadFlask/colorpicker"),
      Triple("gson", "Google · Apache 2.0", "https://github.com/google/gson"),
      Triple("zxing", "zxing · Apache 2.0", "https://github.com/zxing/zxing"),
      Triple("AndroidX", "Google · Apache 2.0", "https://source.google.com"),
      Triple("Android Jetpack", "Google · Apache 2.0", "https://source.google.com"),
      Triple("Palette", "Google · Apache 2.0", "https://source.google.com"),
      Triple("OkHttp", "Square · Apache 2.0", "https://github.com/square/okhttp"),
      Triple("Retrofit", "Square · Apache 2.0", "https://github.com/square/retrofit"),
      Triple(
        "LeakCanary",
        "Square · Apache 2.0",
        "https://github.com/square/leakcanary"
      ),
      Triple(
        "timber",
        "JakeWharton · Apache 2.0",
        "https://github.com/JakeWharton/timber"
      ),
      Triple(
        "RxAndroid",
        "JakeWharton · Apache 2.0",
        "https://github.com/ReactiveX/RxAndroid"
      ),
      Triple("RxJava", "ReactiveX · Apache 2.0", "https://github.com/ReactiveX/RxJava"),
      Triple(
        "android-target-tooltip",
        "sephiroth74 · MIT",
        "https://github.com/sephiroth74/android-target-tooltip"
      ),
      Triple(
        "AndroidUtilCode",
        "Blankj · Apache 2.0",
        "https://github.com/Blankj/AndroidUtilCode"
      ),
      Triple("MMKV", "Tencent · BSD 3-Clause", "https://github.com/Tencent/MMKV"),
      Triple(
        "AndroidHiddenApiBypass",
        "LSPosed · Apache 2.0",
        "https://github.com/LSPosed/AndroidHiddenApiBypass"
      )
    )
  }
}
