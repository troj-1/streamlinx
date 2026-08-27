package com.streamflixreborn.streamflix.ui.components
import com.streamflixreborn.streamflix.models.Movie
import com.streamflixreborn.streamflix.models.TvShow
import com.streamflixreborn.streamflix.models.Episode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflixreborn.streamflix.compat.Item
import java.util.Calendar

@Composable
fun CategoryRow(
    title: String,
    items: List<Item>,
    onItemClick: (Item) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "See All",
                color = Color(0xFFE50914),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                var titleString: String = ""
                var posterUrl: String? = null
                var quality: String? = null
                var rating: Double? = null
                var year: String? = null
                
                when (item) {
                    is Movie -> {
                        titleString = item.title
                        posterUrl = item.poster
                        quality = item.quality
                        rating = item.rating
                        year = item.released?.get(Calendar.YEAR)?.toString()
                    }
                    is TvShow -> {
                        titleString = item.title
                        posterUrl = item.poster
                        quality = item.quality
                        rating = item.rating
                        year = item.released?.get(Calendar.YEAR)?.toString()
                    }
                    is Episode -> {
                        titleString = item.title ?: ""
                        posterUrl = item.poster
                    }
                    else -> { titleString = "Unknown" }
                }
                
                ContentCard(
                    title = titleString,
                    posterUrl = posterUrl,
                    quality = quality,
                    rating = rating,
                    year = year,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}
