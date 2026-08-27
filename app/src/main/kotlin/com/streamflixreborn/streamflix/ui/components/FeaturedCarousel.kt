package com.streamflixreborn.streamflix.ui.components
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.models.Episode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.compat.Item
import kotlinx.coroutines.delay

@Composable
fun FeaturedCarousel(
    items: List<Item>,
    onItemClick: (Item) -> Unit = {},
    onWatchClick: (Item) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { items.size })
    
    LaunchedEffect(pagerState.currentPage) {
        delay(6000)
        val nextPage = (pagerState.currentPage + 1) % items.size
        pagerState.animateScrollToPage(nextPage)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = items[page]
            var banner: String? = null
            var title: String = ""
            var genres: List<String>? = null
            
            when (item) {
                is Movie -> {
                    banner = item.banner ?: item.poster
                    title = item.title
                    genres = item.genres.map { it.name }
                }
                is TvShow -> {
                    banner = item.banner ?: item.poster
                    title = item.title
                    genres = item.genres.map { it.name }
                }
                is Episode -> {
                    banner = item.poster
                    title = item.title ?: ""
                    genres = null
                }
                else -> { title = "" }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onItemClick(item) }
            ) {
                AsyncImage(
                    url = banner,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 100f
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (!genres.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = genres.joinToString(" • "),
                                color = Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Button(
                        onClick = { onWatchClick(item) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Watch Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(items.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.White else Color.Gray
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
