package com.example.ui.screens

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.DonghuaCard
import com.example.ui.components.DonghuaHorizontalCard
import com.example.ui.components.EmptyState
import com.example.ui.components.GenreChip
import com.example.ui.components.SectionHeader
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
fun HomeScreen(
    viewModel: DongHiveViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    val featuredDonghua = viewModel.repository.getFeaturedDonghua()
    val latestEpisodes = viewModel.repository.getLatestEpisodes()
    val popularDonghua = viewModel.repository.getPopularDonghua()
    val allDonghua = viewModel.repository.getAllDonghua()

    val searchResults = if (searchQuery.isNotBlank() || selectedGenre != "Semua" || selectedStatus != "Semua") {
        viewModel.repository.searchDonghua(searchQuery, selectedGenre, selectedStatus)
    } else {
        null
    }

    val genres = listOf("Semua", "Xianxia", "Aksi", "Kultivasi", "Fantasi", "Sci-Fi", "Komedi", "Romansa")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DongHiveBg)
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // App Header Brand
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(DongHiveGold, DongHiveOrange)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AD",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AnimeDong",
                            color = DongHiveTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Streaming Donghua & Anime Sub Indo",
                            color = DongHiveTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // VIP Badge
                Surface(
                    color = DongHiveGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveGold.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = DongHiveGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VIP MEMBER",
                            color = DongHiveGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Search Input Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Cari judul donghua (BTTH, Perfect World...)",
                            color = DongHiveTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = DongHiveGold
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = DongHiveTextSecondary
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(12.dp),
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
            }
        }

        // Genre Filter Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    GenreChip(
                        text = genre,
                        isSelected = selectedGenre == genre,
                        onClick = { viewModel.updateSelectedGenre(genre) }
                    )
                }
            }
        }

        // IF USER IS SEARCHING OR FILTERING, SHOW SEARCH RESULTS
        if (searchResults != null) {
            item {
                SectionHeader(
                    title = "Hasil Pencarian (${searchResults.size})",
                    actionText = "Reset Filter",
                    onActionClick = {
                        viewModel.updateSearchQuery("")
                        viewModel.updateSelectedGenre("Semua")
                        viewModel.updateSelectedStatus("Semua")
                    }
                )
            }

            if (searchResults.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "Donghua Tidak Ditemukan",
                        subtitle = "Coba gunakan kata kunci lain seperti 'Kultivasi' atau 'Xiao Yan'"
                    )
                }
            } else {
                items(searchResults) { donghua ->
                    DonghuaHorizontalCard(
                        donghua = donghua,
                        subtitle = "Episode ${donghua.latestEpisode} • ${donghua.status} • ★ ${donghua.rating}",
                        onClick = { viewModel.openDetail(donghua) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            // Hero Banner Carousel
            item {
                HeroBannerCarousel(
                    featuredList = featuredDonghua,
                    onDonghuaClick = { viewModel.openDetail(it) },
                    onWatchClick = { donghua ->
                        val eps = viewModel.repository.getEpisodesForDonghua(donghua)
                        if (eps.isNotEmpty()) {
                            viewModel.openPlayer(donghua, eps.first())
                        } else {
                            viewModel.openDetail(donghua)
                        }
                    }
                )
            }

            // Episode Terbaru Hari Ini Section
            item {
                SectionHeader(
                    title = "Episode Baru Hari Ini",
                    actionText = "Semua",
                    onActionClick = { }
                )
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(latestEpisodes.take(6)) { donghua ->
                        DonghuaCard(
                            donghua = donghua,
                            onClick = { viewModel.openDetail(donghua) },
                            modifier = Modifier.width(135.dp)
                        )
                    }
                }
            }

            // Populer Teratas Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Donghua Terpopuler",
                    actionText = "Top Chart",
                    onActionClick = { }
                )
            }

            itemsIndexed(popularDonghua.take(4)) { index, donghua ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.openDetail(donghua) },
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = DongHiveCard
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ranking Number Badge
                        Text(
                            text = "#${index + 1}",
                            color = if (index == 0) DongHiveGold else if (index == 1) DongHiveOrange else DongHiveTextMuted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.width(36.dp)
                        )

                        // Cover
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(donghua.coverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = donghua.title,
                            modifier = Modifier
                                .size(width = 60.dp, height = 75.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = donghua.title,
                                color = DongHiveTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${donghua.genres.take(2).joinToString(", ")} • ${donghua.views} tayangan",
                                color = DongHiveTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = DongHiveGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "%.1f".format(donghua.rating),
                                    color = DongHiveGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ep ${donghua.latestEpisode}",
                                    color = DongHiveOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val eps = viewModel.repository.getEpisodesForDonghua(donghua)
                                if (eps.isNotEmpty()) {
                                    viewModel.openPlayer(donghua, eps.first())
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DongHiveGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Semua Donghua Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Koleksi Lengkap Donghua",
                    actionText = null
                )
            }

            // 2 column rows
            val chunkedDonghua = allDonghua.chunked(2)
            items(chunkedDonghua) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (donghua in pair) {
                        DonghuaCard(
                            donghua = donghua,
                            onClick = { viewModel.openDetail(donghua) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBannerCarousel(
    featuredList: List<Donghua>,
    onDonghuaClick: (Donghua) -> Unit,
    onWatchClick: (Donghua) -> Unit
) {
    if (featuredList.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { featuredList.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val donghua = featuredList[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9.5f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDonghuaClick(donghua) }
                    .testTag("featured_banner_$page"),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(donghua.bannerUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = donghua.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x99000000),
                                        Color(0xF00B0D14)
                                    )
                                )
                            )
                    )

                    // Content overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = DongHiveGold,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SPOTLIGHT",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xCC000000),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Ep ${donghua.latestEpisode} Sub Indo",
                                    color = DongHiveTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = donghua.title,
                            color = DongHiveTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = donghua.genres.take(3).joinToString(" • "),
                            color = DongHiveTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onWatchClick(donghua) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DongHiveGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Nonton",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                color = Color(0x66000000),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
                                modifier = Modifier
                                    .height(34.dp)
                                    .clickable { onDonghuaClick(donghua) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "Detail",
                                        color = DongHiveTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pager indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(featuredList.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(4.dp)
                        .width(if (isSelected) 18.dp else 6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) DongHiveGold else DongHiveCardBorder)
                )
            }
        }
    }
}
