package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.DongHiveBadgeHot
import com.example.ui.theme.DongHiveBadgeOngoing
import com.example.ui.theme.DongHiveCard
import com.example.ui.theme.DongHiveCardBorder
import com.example.ui.theme.DongHiveGold
import com.example.ui.theme.DongHiveOrange
import com.example.ui.theme.DongHiveSurface
import com.example.ui.theme.DongHiveTextMuted
import com.example.ui.theme.DongHiveTextPrimary
import com.example.ui.theme.DongHiveTextSecondary

@Composable
fun DonghuaCard(
    donghua: Donghua,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("donghua_card_${donghua.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DongHiveCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(donghua.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = donghua.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating Badge
                    Surface(
                        color = Color(0xCC0B0D14),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = DongHiveGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "%.1f".format(donghua.rating),
                                color = DongHiveTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Episode Badge
                    Surface(
                        color = DongHiveOrange,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Ep ${donghua.latestEpisode}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Gradient scrim at bottom of image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xEE1A1E2E))
                            )
                        )
                )
            }

            // Card Info
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = donghua.title,
                    color = DongHiveTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = donghua.genres.take(2).joinToString(" • "),
                    color = DongHiveTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DonghuaHorizontalCard(
    donghua: Donghua,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("donghua_horizontal_${donghua.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DongHiveCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 100.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(donghua.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = donghua.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = DongHiveBadgeHot.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "HD",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                    text = donghua.chineseTitle,
                    color = DongHiveGold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = DongHiveTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (progress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = DongHiveGold,
                        trackColor = DongHiveCardBorder
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 18.dp)
                    .background(DongHiveGold, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = DongHiveTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = DongHiveGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            )
        }
    }
}

@Composable
fun GenreChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) DongHiveGold else DongHiveCard,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DongHiveCardBorder)
    ) {
        Text(
            text = text,
            color = if (isSelected) DongHiveSurface else DongHiveTextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(DongHiveCard, CircleShape)
                .border(1.dp, DongHiveCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DongHiveGold,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = DongHiveTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = DongHiveTextMuted,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
