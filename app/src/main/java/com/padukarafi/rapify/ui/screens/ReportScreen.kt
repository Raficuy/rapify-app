package com.padukarafi.rapify.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padukarafi.rapify.BuildConfig
import com.padukarafi.rapify.R
import com.padukarafi.rapify.data.stats.StatsManager
import com.padukarafi.rapify.ui.theme.RapifyAppTheme
import com.padukarafi.rapify.ui.theme.Typography

private const val CONTACT_WA_NUMBER = "6283854944887"
private const val CONTACT_TELEGRAM = "RafiCuy"
private const val CONTACT_WA_CHANNEL = "https://whatsapp.com/channel/0029Vb5h45DKWEKjMyUUbg1O"
private const val DANA_NUMBER = "083854944887"

@Composable
fun ReportScreen() {
    val colors = RapifyAppTheme.colors
    val context = LocalContext.current
    val totalUsers by StatsManager.totalUsers.collectAsState()

    fun open(url: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) { }
    }

    val waText = stringResource(R.string.report_wa_prefill, BuildConfig.VERSION_NAME)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.report_title),
                style = Typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.report_desc),
                style = Typography.bodyMedium,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))

            ReportButton(
                label = stringResource(R.string.report_btn_wa),
                onClick = { open("https://wa.me/$CONTACT_WA_NUMBER?text=${Uri.encode(waText)}") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ReportButton(
                label = stringResource(R.string.report_btn_tg),
                onClick = { open("https://t.me/$CONTACT_TELEGRAM") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ReportButton(
                label = "Saluran WhatsApp", 
                onClick = { open(CONTACT_WA_CHANNEL) }
            )

            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = stringResource(R.string.donate_title),
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.donate_desc),
                style = Typography.bodySmall,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(R.drawable.qris_donation),
                contentDescription = stringResource(R.string.donate_qris_desc),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            ReportButton(
                label = stringResource(R.string.donate_copy_dana, DANA_NUMBER),
                onClick = {
                    try {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("DANA", DANA_NUMBER))
                        Toast.makeText(context, R.string.donate_copied, Toast.LENGTH_SHORT).show()
                    } catch (_: Exception) { }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
            totalUsers?.let { total ->
                Text(
                    text = stringResource(
                        R.string.settings_total_users,
                        java.text.NumberFormat.getInstance().format(total)
                    ),
                    style = Typography.labelSmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = "RapiFy v${BuildConfig.VERSION_NAME}",
                style = Typography.labelSmall,
                fontSize = 11.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.settings_copyright),
                style = Typography.labelSmall,
                fontSize = 10.sp,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun ReportButton(label: String, onClick: () -> Unit) {
    val colors = RapifyAppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = Typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
    }
}
