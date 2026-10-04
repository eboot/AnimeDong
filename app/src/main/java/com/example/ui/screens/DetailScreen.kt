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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Donghua
import com.example.data.model.Episode
import com.example.ui.components.DonghuaCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DongHiveBadgeOngoing
import com.example.ui.theme.DongHiveBg
import com.example.ui.theme.DongHiveCard
import com.example.ui.theme.DongHiveCardBorder
import com.example.ui.theme.DongHiveGold
import com.example.ui.theme.DongHiveOrange
import com.example.ui.theme.DongHiveSurface
import com.example.ui.theme.DongHiveTextMuted
import com.example.ui.theme.DongHiveTextPrimary
import com.example.ui.theme.DongHiveTextSecondary
import com.example.viewmodel.DongHiveViewModel

@Composable
fun DetailScreen(
    viewModel: DongHiveViewModel,
    modifier: Modifier = Modifier
) {
    val donghua = viewModel.selectedDonghua.collectAsState().value ?: return
    val episodes = viewModel.episodesList.collectAsState().value
    val isFav by viewModel.isFavorite.collectAsState()
    val context = LocalContext.current

    var isSynopsisExpanded by remember { mutableStateOf(false) }
    var selectedRangeIndex by remember { mutableIntStateOf(0) }

    // Intercept hardware and gesture back
    BackHandler {
        viewModel.handleBack()
    }

    val relatedDonghua = viewModel.repository.getAllDonghua().filter { it.id != donghua.id }.take(5)

    // Calculate episode range chunks (e.g. 25 eps per tab)
    val chunkSize = 25
    val chunks = episodes.chunked(chunkSize)

    val currentChunk = if (chunks.isNotEmpty() && selectedRangeIndex in chunks.indices) {
        chunks[selectedRangeIndex]
    } else {
        episodes
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DongHiveBg)
            .testTag("detail_screen_content"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Backdrop with Top Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(donghua.bannerUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = donghua.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Scrim gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x99000000),
                                    Color.Transparent,
                                    DongHiveBg
                                )
                            )
                        )
                )

                // Top Bar with Back and Share
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.handleBack() },
                        modifier = Modifier
                            .background(Color(0x88000000), CircleShape)
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Tonton donghua seru ${donghua.title} (${donghua.chineseTitle}) sub Indo di aplikasi AnimeDong!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Donghua"))
                            },
                            modifier = Modifier.background(Color(0x88000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { viewModel.toggleFavorite(donghua) },
                            modifier = Modifier
                                .background(Color(0x88000000), CircleShape)
                                .testTag("detail_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) DongHiveGold else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Donghua Header Info (Poster, Titles, Metadata)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 0.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Cover Poster with shadow/border
                Card(
                    modifier = Modifier
                        .size(width = 110.dp, height = 155.dp)
                        .border(1.5.dp, DongHiveCardBorder, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(donghua.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = donghua.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = donghua.title,
                        color = DongHiveTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = donghua.chineseTitle,
                        color = DongHiveGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status and Rating Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = DongHiveBadgeOngoing.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveBadgeOngoing.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = donghua.status,
                                color = DongHiveBadgeOngoing,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = DongHiveGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "%.1f".format(donghua.rating),
                                color = DongHiveGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${donghua.latestEpisode} Episode",
                            color = DongHiveTextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Studio: ${donghua.studio}",
                        color = DongHiveTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Rilis: Setiap ${donghua.releaseDay} ${donghua.releaseTime}",
                        color = DongHiveTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Primary Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val firstEp = episodes.firstOrNull()
                        if (firstEp != null) {
                            viewModel.openPlayer(donghua, firstEp)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("detail_play_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DongHiveGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mulai Nonton",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.toggleFavorite(donghua) },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isFav) DongHiveGold else DongHiveTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isFav) DongHiveGold else DongHiveCardBorder
                    )
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFav) "Tersimpan" else "Favorit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Synopsis Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateContentSize(),
                colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Sinopsis Cerita",
                        color = DongHiveTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = donghua.synopsis,
                        color = DongHiveTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSynopsisExpanded) "Tampilkan Lebih Sedikit" else "Selengkapnya...",
                        color = DongHiveGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isSynopsisExpanded = !isSynopsisExpanded }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Genres tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        donghua.genres.forEach { genre ->
                            Surface(
                                color = DongHiveSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
                            ) {
                                Text(
                                    text = genre,
                                    color = DongHiveTextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Episode List Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Daftar Episode (${episodes.size})",
                actionText = "Urutkan",
                onActionClick = { }
            )
        }

        // Episode Chunk Tabs (if more than 25 eps)
        if (chunks.size > 1) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(chunks) { idx, chunk ->
                        val startEp = chunk.last().episodeNumber
                        val endEp = chunk.first().episodeNumber
                        val isSelected = selectedRangeIndex == idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) DongHiveGold else DongHiveCard,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder),
                            modifier = Modifier.clickable { selectedRangeIndex = idx }
                        ) {
                            Text(
                                text = "Ep $startEp - $endEp",
                                color = if (isSelected) Color.Black else DongHiveTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Episodes Grid Cards
        items(currentChunk) { episode ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { viewModel.openPlayer(donghua, episode) }
                    .testTag("episode_item_${episode.episodeNumber}"),
                colors = CardDefaults.cardColors(containerColor = DongHiveCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DongHiveSurface)
                                .border(1.dp, DongHiveCardBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = DongHiveGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Episode ${episode.episodeNumber}",
                                color = DongHiveTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${episode.durationMinutes} Menit • Sub Indo • ${episode.releaseDate}",
                                color = DongHiveTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Download Action
                    IconButton(
                        onClick = { viewModel.downloadEpisode(donghua, episode) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Episode",
                            tint = DongHiveTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Rekomendasi Terkait Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Donghua Serupa",
                actionText = null
            )
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(relatedDonghua) { related ->
                    DonghuaCard(
                        donghua = related,
                        onClick = { viewModel.openDetail(related) },
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }
    }
}
