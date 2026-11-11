package com.example.discordmobilemanager

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

private data class Profile(val id: Long, val title: String, val channel: String)

class MainActivity : Activity() {
    private val bg = Color.rgb(16, 17, 22)
    private val panel = Color.rgb(29, 31, 40)
    private val accent = Color.rgb(155, 123, 255)
    private val profiles = mutableListOf<Profile>()
    private lateinit var list: LinearLayout
    private lateinit var count: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        loadProfiles()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(18))
            setBackgroundColor(bg)
        }
        val title = TextView(this).apply {
            text = "Discord Manager"
            textSize = 26f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(title)
        val subtitle = TextView(this).apply {
            text = "Профили и быстрый доступ к Discord"
            textSize = 14f
            setTextColor(Color.rgb(165, 168, 184))
            setPadding(0, dp(5), 0, dp(18))
        }
        root.addView(subtitle)

        val notice = TextView(this).apply {
            text = "Важно: это менеджер профилей, а не несколько копий Discord. Android и официальный клиент Discord не дают приложению независимо подключить много аккаунтов к голосовым каналам одной кнопкой."
            textSize = 13f
            setTextColor(Color.rgb(226, 211, 255))
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(Color.rgb(48, 38, 68))
        }
        root.addView(notice)

        count = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(20), 0, dp(10))
        }
        root.addView(count)

        val add = Button(this).apply {
            text = "+ Добавить профиль"
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent))
            setOnClickListener { showAddDialog() }
        }
        root.addView(add, LinearLayout.LayoutParams(-1, dp(48)))

        val open = Button(this).apply {
            text = "Открыть Discord"
            setOnClickListener { openDiscord() }
        }
        root.addView(open, LinearLayout.LayoutParams(-1, dp(46)))

        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val footer = TextView(this).apply {
            text = "Не сохраняйте здесь пароли или токены Discord. Вход выполняйте только в официальном приложении."
            textSize = 12f
            setTextColor(Color.rgb(130, 133, 148))
            setPadding(0, dp(12), 0, 0)
        }
        root.addView(footer)
        setContentView(root)
        render()
    }

    private fun showAddDialog() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val name = EditText(this).apply { hint = "Название профиля (например, Аккаунт 1)"; setSingleLine() }
        val channel = EditText(this).apply { hint = "Название голосового канала (необязательно)"; setSingleLine() }
        box.addView(name)
        box.addView(channel)
        AlertDialog.Builder(this)
            .setTitle("Новый профиль")
            .setView(box)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Добавить") { _, _ ->
                val n = name.text.toString().trim()
                if (n.isNotEmpty() && profiles.size < 100) {
                    profiles.add(Profile(System.currentTimeMillis(), n, channel.text.toString().trim()))
                    saveProfiles(); render()
                } else if (profiles.size >= 100) toast("Достигнут лимит 100 профилей")
                else toast("Укажи название профиля")
            }.show()
    }

    private fun render() {
        count.text = "Профили · ${profiles.size}"
        list.removeAllViews()
        if (profiles.isEmpty()) {
            val empty = TextView(this).apply {
                text = "Пока нет профилей. Нажми «Добавить профиль»."
                setTextColor(Color.LTGRAY); textSize = 14f
                setPadding(0, dp(24), 0, 0)
            }
            list.addView(empty)
        }
        profiles.forEach { p ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(15), dp(13), dp(15), dp(10))
                setBackgroundColor(panel)
            }
            val label = TextView(this).apply {
                text = p.title; textSize = 17f; typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
            }
            card.addView(label)
            val sub = TextView(this).apply {
                text = if (p.channel.isBlank()) "Канал не указан" else "Канал: ${p.channel}"
                textSize = 13f; setTextColor(Color.rgb(165, 168, 184))
                setPadding(0, dp(4), 0, dp(8))
            }
            card.addView(sub)
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val launch = Button(this).apply {
                text = "Discord"
                setOnClickListener { openDiscord() }
            }
            val remove = Button(this).apply {
                text = "Удалить"
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity).setMessage("Удалить профиль «${p.title}»?")
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Удалить") { _, _ -> profiles.removeAll { it.id == p.id }; saveProfiles(); render() }.show()
                }
            }
            row.addView(launch, LinearLayout.LayoutParams(0, dp(44), 1f))
            row.addView(remove, LinearLayout.LayoutParams(0, dp(44), 1f))
            card.addView(row)
            val lp = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
            list.addView(card, lp)
        }
    }

    private fun openDiscord() {
        val launch = packageManager.getLaunchIntentForPackage("com.discord")
        if (launch != null) startActivity(launch)
        else {
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.discord"))) }
            catch (_: Exception) { toast("Не удалось открыть Google Play") }
        }
    }

    private fun loadProfiles() {
        try {
            val raw = getSharedPreferences("profiles", MODE_PRIVATE).getString("items", "[]") ?: "[]"
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                profiles.add(Profile(o.optLong("id"), o.optString("title"), o.optString("channel")))
            }
        } catch (_: Exception) { profiles.clear() }
    }

    private fun saveProfiles() {
        val arr = JSONArray()
        profiles.forEach { p -> arr.put(JSONObject().put("id", p.id).put("title", p.title).put("channel", p.channel)) }
        getSharedPreferences("profiles", MODE_PRIVATE).edit().putString("items", arr.toString()).apply()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
