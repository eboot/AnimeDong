package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.EpisodeInfo
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AnimeDongBadgeOngoing
import com.example.ui.theme.AnimeDongBg
import com.example.ui.theme.AnimeDongCard
import com.example.ui.theme.AnimeDongCardBorder
import com.example.ui.theme.AnimeDongGold
import com.example.ui.theme.AnimeDongOrange
import com.example.ui.theme.AnimeDongSurface
import com.example.ui.theme.AnimeDongTextMuted
import com.example.ui.theme.AnimeDongTextPrimary
import com.example.ui.theme.AnimeDongTextSecondary
import com.example.viewmodel.AnimeDongViewModel

@Composable
fun DetailScreen(
    viewModel: AnimeDongViewModel,
    modifier: Modifier = Modifier
) {
    val anime = viewModel.selectedAnime.collectAsState().value ?: return
    val detail = viewModel.selectedAnimeDetail.collectAsState().value
    val isDetailLoading by viewModel.isDetailLoading.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val context = LocalContext.current

    var isSynopsisExpanded by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeDongBg)
            .testTag("detail_screen_content"),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Hero Banner & Poster
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Background Poster Scrim
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(anime.poster)
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x990B0D14),
                                    Color(0xEE0B0D14),
                                    AnimeDongBg
                                )
                            )
                        )
                )

                // Top Bar with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .background(Color(0x66000000), CircleShape)
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.toggleBookmark() },
                            modifier = Modifier
                                .background(Color(0x66000000), CircleShape)
                                .testTag("bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) AnimeDongGold else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, anime.title)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Nonton ${anime.title} subtitle Indonesia di AnimeDong!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan ke"))
                            },
                            modifier = Modifier
                                .background(Color(0x66000000), CircleShape)
                                .testTag("share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Bagikan",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Poster & Title Info row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.BottomStart),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(0.72f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AnimeDongCardBorder)
                    ) {
                        AsyncImage(
                            model = anime.poster,
                            contentDescription = anime.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = anime.title,
                            color = AnimeDongTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = AnimeDongBadgeOngoing,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = detail?.status ?: "Ongoing",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (detail != null && detail.rating > 0f) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AnimeDongGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "%.1f".format(detail.rating),
                                    color = AnimeDongGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (detail?.genres?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = detail.genres.joinToString(" • "),
                                color = AnimeDongTextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Synopsis
        if (!detail?.synopsis.isNullOrBlank()) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sinopsis",
                        color = AnimeDongTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = detail?.synopsis.orEmpty(),
                        color = AnimeDongTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .animateContentSize()
                            .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                    )
                    Text(
                        text = if (isSynopsisExpanded) "Tutup" else "Baca selengkapnya",
                        color = AnimeDongGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        // Episodes Section
        item {
            SectionHeader(
                title = "Daftar Episode",
                actionText = if (detail?.episodes?.isNotEmpty() == true) "${detail.episodes.size} Episode" else null
            )
        }

        if (isDetailLoading && detail == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AnimeDongGold)
                }
            }
        } else if (detail?.episodes.isNullOrEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada episode yang tersedia untuk anime ini.",
                        color = AnimeDongTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            val episodes = detail?.episodes ?: emptyList()
            items(episodes, key = { it.episodeId }) { episode ->
                EpisodeItemCard(
                    episode = episode,
                    onClick = { viewModel.playEpisode(episode) }
                )
            }
        }
    }
}

@Composable
fun EpisodeItemCard(
    episode: EpisodeInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
            .testTag("episode_item_${episode.episodeId}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AnimeDongCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, AnimeDongCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AnimeDongSurface, CircleShape)
                        .border(1.dp, AnimeDongCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AnimeDongGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = episode.title,
                        color = AnimeDongTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (episode.date.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = episode.date,
                            color = AnimeDongTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Surface(
                color = AnimeDongGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Tonton",
                    color = AnimeDongGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
