package com.rangerscards.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun Context.openLink(link: String) {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            link.toUri(),
        )
    )
}

fun Context.openEmail(email: String) {
    val uri = "mailto:$email".toUri()
    val intent = Intent(Intent.ACTION_SENDTO, uri)
    startActivity(intent)
}