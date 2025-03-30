package com.skynet.streamnote.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // 테스트 광고 단위 ID 사용 (실제 게시 시 변경 필요)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"

                // 광고 로드
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}