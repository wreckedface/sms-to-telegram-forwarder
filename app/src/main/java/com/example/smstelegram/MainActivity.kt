package com.example.smstelegram

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Color
import android.widget.*

class MainActivity : Activity() {

    private lateinit var token: EditText
    private lateinit var chat: EditText
    private lateinit var wl: EditText
    private lateinit var kw: EditText
    private lateinit var on: Switch
    private lateinit var dedupe: CheckBox
    private lateinit var redact: CheckBox
    private lateinit var status: TextView
    private lateinit var log: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        build()
        requestSms()
    }

    private fun build() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
            setBackgroundColor(Color.rgb(7, 17, 31))
        }

        fun lab(s: String) = TextView(this).apply {
            text = s
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 12, 0, 5)
        }

        fun ed(h: String) = EditText(this).apply {
            hint = h
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }

        root.addView(TextView(this).apply {
            text = "SMS → Telegram"
            textSize = 30f
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text = "Final • secure local forwarding"
            textSize = 15f
            setTextColor(Color.rgb(110, 190, 230))
            setPadding(0, 4, 0, 14)
        })

        root.addView(lab("Telegram bot token"))
        token = ed("BotFather token")
        root.addView(token)

        root.addView(lab("Telegram chat ID"))
        chat = ed("Auto-discover available")
        chat.inputType = 2
        root.addView(chat)

        root.addView(Button(this).apply {
            text = "Auto-discover chat ID"

            setOnClickListener {
                saveNow()

                Telegram.discoverChat(this@MainActivity) { id, msg ->
                    runOnUiThread {
                        if (id != null) {
                            chat.setText(id)
                            saveNow()
                            status.text = "Chat ID discovered: $id"
                        } else {
                            status.text = msg
                        }
                    }
                }
            }
        })

        on = Switch(this).apply {
            text = "Automatic forwarding"
            setTextColor(Color.WHITE)
        }
        root.addView(on)

        root.addView(lab("Sender whitelist (optional)"))
        wl = ed("Comma-separated senders")
        root.addView(wl)

        root.addView(lab("Keyword filter (optional)"))
        kw = ed("Comma-separated words")
        root.addView(kw)

        dedupe = CheckBox(this).apply {
            text = "Prevent duplicate forwards"
            setTextColor(Color.WHITE)
            isChecked = true
        }
        root.addView(dedupe)

        redact = CheckBox(this).apply {
            text = "Redact 4–8 digit codes"
            setTextColor(Color.WHITE)
        }
        root.addView(redact)

        root.addView(Button(this).apply {
            text = "Save settings"

            setOnClickListener {
                saveNow()
                status.text = "Settings saved."
            }
        })

        root.addView(Button(this).apply {
            text = "Test Telegram connection"

            setOnClickListener {
                saveNow()

                Telegram.send(
                    this@MainActivity,
                    "✅ SMS → Telegram connection test",
                    object : Telegram.Callback {
                        override fun done(ok: Boolean, d: String) {
                            runOnUiThread {
                                status.text = d
                            }
                        }
                    }
                )
            }
        })

        root.addView(Button(this).apply {
            text = "Retry failed messages now"

            setOnClickListener {
                androidx.work.WorkManager
                    .getInstance(this@MainActivity)
                    .enqueue(
                        androidx.work.OneTimeWorkRequestBuilder<RetryWorker>()
                            .build()
                    )

                status.text = "Retry queued."
            }
        })

        root.addView(Button(this).apply {
            text = "Clear history & retry queue"

            setOnClickListener {
                History.clear(this@MainActivity)
                RetryStore.clear(this@MainActivity)
                Stats.clear(this@MainActivity)

                refresh()

                status.text = "Local data cleared."
            }
        })

        status = TextView(this).apply {
            setTextColor(Color.LTGRAY)
            setPadding(0, 14, 0, 8)
        }
        root.addView(status)

        log = TextView(this).apply {
            setTextColor(Color.LTGRAY)
            textSize = 13f
        }
        root.addView(log)

        setContentView(
            ScrollView(this).apply {
                addView(root)
            }
        )

        token.setText(Store.token(this))
        chat.setText(Store.chatId(this))
        wl.setText(Store.whitelist(this))
        kw.setText(Store.keywords(this))
        on.isChecked = Store.enabled(this)
        dedupe.isChecked = Store.dedupe(this)
        redact.isChecked = Store.redact(this)

        refresh()
    }

    private fun saveNow() {
        Store.save(
            this,
            token.text.toString().trim(),
            chat.text.toString().trim(),
            on.isChecked,
            wl.text.toString(),
            kw.text.toString(),
            dedupe.isChecked,
            redact.isChecked
        )
    }

    private fun refresh() {
        val h = History.all(this)

        log.text =
            "Statistics\n" +
            "Received: ${Stats.received(this)}  •  " +
            "Forwarded: ${Stats.forwarded(this)}  •  " +
            "Failed: ${Stats.failed(this)}  •  " +
            "Filtered: ${Stats.filtered(this)}\n\n" +
            if (h.isEmpty()) {
                "No forwarded messages yet."
            } else {
                "Recent activity:\n" +
                    h.take(12).joinToString("\n\n") {
                        (if (it.ok) "✓ " else "✕ ") +
                            it.time +
                            " • " +
                            it.sender +
                            "\n" +
                            it.body.take(100)
                    }
            }
    }

    private fun requestSms() {
        if (
            android.os.Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.RECEIVE_SMS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                ),
                100
            )
        }
    }
}
