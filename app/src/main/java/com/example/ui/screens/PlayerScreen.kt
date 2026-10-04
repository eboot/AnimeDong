package com.example.ui.screens

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DongHiveBadgeOngoing
import com.example.ui.theme.DongHiveBadgeVip
import com.example.ui.theme.DongHiveBg
import com.example.ui.theme.DongHiveCard
import com.example.ui.theme.DongHiveCardBorder
import com.example.ui.theme.DongHiveGold
import com.example.ui.theme.DongHiveOrange
import com.example.ui.theme.DongHivePlayerBg
import com.example.ui.theme.DongHiveSurface
import com.example.ui.theme.DongHiveTextMuted
import com.example.ui.theme.DongHiveTextPrimary
import com.example.ui.theme.DongHiveTextSecondary
import com.example.viewmodel.DongHiveViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: DongHiveViewModel,
    modifier: Modifier = Modifier
) {
    val donghua = viewModel.selectedDonghua.collectAsState().value ?: return
    val episode = viewModel.selectedEpisode.collectAsState().value ?: return
    val episodes = viewModel.episodesList.collectAsState().value
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.playbackPositionMs.collectAsState()
    val durationMs by viewModel.totalDurationMs.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val selectedSubtitle by viewModel.selectedSubtitle.collectAsState()
    val autoNext by viewModel.autoNext.collectAsState()
    val isFav by viewModel.isFavorite.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val showSettings by viewModel.showPlayerSettings.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var newCommentText by remember { mutableStateOf("") }

    // Intercept hardware and gesture back
    BackHandler {
        viewModel.handleBack()
    }

    // Auto-advance simulated playback timer when playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            val next = positionMs + (1000 * playbackSpeed).toLong()
            if (next >= durationMs) {
                viewModel.seekTo(durationMs)
                if (autoNext) {
                    viewModel.nextEpisode()
                }
                break
            } else {
                viewModel.seekTo(next)
            }
        }
    }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    val formatTime = { millis: Long ->
        val totalSec = millis / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        "%02d:%02d".format(m, s)
    }

    val streamUrl = selectedServer?.videoUrl ?: episode.streamServers.firstOrNull()?.videoUrl ?: ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DongHiveBg)
            .testTag("player_screen_content")
    ) {
        // VIDEO PLAYER CONTAINER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9.5f)
                .background(DongHivePlayerBg)
                .clickable { showControls = !showControls }
                .testTag("video_player_box")
        ) {
            // Android Native VideoView / Player
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setVideoURI(Uri.parse(streamUrl))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            if (isPlaying) start()
                        }
                    }
                },
                update = { videoView ->
                    if (isPlaying) {
                        if (!videoView.isPlaying) videoView.start()
                    } else {
                        if (videoView.isPlaying) videoView.pause()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // CONTROLS OVERLAY
            if (showControls) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88000000))
                ) {
                    // Top Player Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.handleBack() },
                                modifier = Modifier.testTag("player_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = donghua.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Episode ${episode.episodeNumber} • $selectedQuality",
                                    color = DongHiveGold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.togglePlayerSettings() },
                            modifier = Modifier.testTag("player_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }

                    // Center Playback Actions (Rewind, Play/Pause, Forward)
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.seekRelative(-10) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0x55000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Mundur 10 Detik",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(60.dp)
                                .background(DongHiveGold, CircleShape)
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Jeda" else "Putar",
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.seekRelative(10) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0x55000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Maju 10 Detik",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Bottom Scrubber Bar & Timers
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Slider(
                            value = positionMs.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toLong()) },
                            valueRange = 0f..durationMs.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = DongHiveGold,
                                activeTrackColor = DongHiveGold,
                                inactiveTrackColor = Color(0x66FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${formatTime(positionMs)} / ${formatTime(durationMs)}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0x88000000),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.clickable { viewModel.togglePlayerSettings() }
                                ) {
                                    Text(
                                        text = selectedQuality,
                                        color = DongHiveGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // SCROLLABLE EPISODE DETAILS AND COMMENTS
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Actions Bar
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Episode ${episode.episodeNumber} : ${donghua.title}",
                                color = DongHiveTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${donghua.chineseTitle} • Sub Indo",
                                color = DongHiveGold,
                                fontSize = 12.sp
                            )
                        }

                        // Server Pill
                        Surface(
                            color = DongHiveCard,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                            modifier = Modifier.clickable { viewModel.togglePlayerSettings() }
                        ) {
                            Text(
                                text = selectedServer?.name?.take(12) ?: "Server VIP",
                                color = DongHiveTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons (Prev, Next, Download, Bookmark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DongHiveCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                                modifier = Modifier.clickable { viewModel.previousEpisode() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Prev",
                                        tint = DongHiveTextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Eps Lalu", color = DongHiveTextPrimary, fontSize = 11.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DongHiveCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                                modifier = Modifier.clickable { viewModel.nextEpisode() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Eps Berikut", color = DongHiveGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next",
                                        tint = DongHiveGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DongHiveCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                                modifier = Modifier.clickable { viewModel.toggleFavorite(donghua) }
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) DongHiveGold else DongHiveTextPrimary,
                                    modifier = Modifier.padding(8.dp).size(18.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DongHiveCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                                modifier = Modifier.clickable { viewModel.downloadEpisode(donghua, episode) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = DongHiveTextPrimary,
                                    modifier = Modifier.padding(8.dp).size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Episode Selector Carousel
            item {
                Column {
                    Text(
                        text = "Pilih Episode",
                        color = DongHiveTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(episodes) { ep ->
                            val isSelected = ep.episodeNumber == episode.episodeNumber
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) DongHiveGold else DongHiveCard,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                                modifier = Modifier.clickable {
                                    viewModel.openPlayer(donghua, ep)
                                }
                            ) {
                                Text(
                                    text = "Ep ${ep.episodeNumber}",
                                    color = if (isSelected) Color.Black else DongHiveTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Diskusi & Komentar Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Komentar Penonton (${comments.size})",
                            color = DongHiveTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tertib & No Spoiler",
                            color = DongHiveTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Post comment input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Tulis pendapatmu tentang episode ini...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("player_comment_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DongHiveCard,
                                unfocusedContainerColor = DongHiveCard,
                                focusedBorderColor = DongHiveGold,
                                unfocusedBorderColor = DongHiveCardBorder,
                                focusedTextColor = DongHiveTextPrimary,
                                unfocusedTextColor = DongHiveTextPrimary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    viewModel.postComment(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(DongHiveGold, RoundedCornerShape(10.dp))
                                .testTag("player_post_comment_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Comments list
            items(comments) { comment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(DongHiveOrange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comment.avatarInitial,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = comment.userName,
                                    color = DongHiveTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = comment.timeAgo,
                                color = DongHiveTextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = comment.commentText,
                            color = DongHiveTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = null,
                                tint = DongHiveTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${comment.likes} suka",
                                color = DongHiveTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // SETTINGS BOTTOM SHEET
    if (showSettings) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.togglePlayerSettings() },
            sheetState = sheetState,
            containerColor = DongHiveSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Pengaturan Pemutar Video",
                    color = DongHiveTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Server Selector
                Text(
                    text = "Pilih Server",
                    color = DongHiveTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                episode.streamServers.forEach { server ->
                    val isSelected = selectedServer?.id == server.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectServer(server) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DongHiveGold.copy(alpha = 0.15f) else DongHiveCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) DongHiveGold else DongHiveCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = server.name,
                                color = if (isSelected) DongHiveGold else DongHiveTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = server.quality,
                                color = DongHiveTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quality Selector
                Text(
                    text = "Resolusi Video",
                    color = DongHiveTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1080p Ultra", "720p HD", "480p SD").forEach { quality ->
                        val isSelected = selectedQuality == quality
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) DongHiveGold else DongHiveCard,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectQuality(quality) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = quality,
                                    color = if (isSelected) Color.Black else DongHiveTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Speed Selector
                Text(
                    text = "Kecepatan Putar",
                    color = DongHiveTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val isSelected = playbackSpeed == speed
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) DongHiveGold else DongHiveCard,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectPlaybackSpeed(speed) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${speed}x",
                                    color = if (isSelected) Color.Black else DongHiveTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto Next Episode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Otomatis Lanjut Episode",
                            color = DongHiveTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Putar episode selanjutnya saat episode ini tamat",
                            color = DongHiveTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = autoNext,
                        onCheckedChange = { viewModel.toggleAutoNext() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = DongHiveGold
                        )
                    )
                }
            }
        }
    }
}
