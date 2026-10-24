package app.kashif.feature.callerid.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

/** Shows a small caller-ID card over other apps (needs "display over other apps"); no-op without it. */
object OverlayPresenter {
    private val main = Handler(Looper.getMainLooper())
    private var view: LinearLayout? = null

    fun show(ctx: Context, model: OverlayModel) {
        if (!Settings.canDrawOverlays(ctx)) return
        main.post {
            runCatching {
                dismiss(ctx)
                val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val pad = (16 * ctx.resources.displayMetrics.density).toInt()
                val card = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(model.colorArgb)
                    setPadding(pad, pad, pad, pad)
                    addView(TextView(ctx).apply {
                        text = model.title; textSize = 20f; setTextColor(-1); setTypeface(null, Typeface.BOLD)
                    })
                    addView(TextView(ctx).apply { text = model.subtitle; textSize = 14f; setTextColor(-1) })
                    setOnClickListener { dismiss(ctx) }
                }
                val lp = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP }
                wm.addView(card, lp)
                view = card
                main.postDelayed({ dismiss(ctx) }, 15_000)
            }
        }
    }

    fun dismiss(ctx: Context) {
        val v = view ?: return
        view = null
        runCatching { (ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(v) }
    }
}
