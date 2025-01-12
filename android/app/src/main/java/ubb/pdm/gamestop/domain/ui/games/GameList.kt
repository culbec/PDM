package ubb.pdm.gamestop.domain.ui.games

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ubb.pdm.gamestop.core.util.formatTo
import ubb.pdm.gamestop.core.util.toDate
import ubb.pdm.gamestop.domain.data.game.Game
import ubb.pdm.gamestop.domain.data.photo.Photo
import ubb.pdm.gamestop.domain.data.photo.loadImageFromFile

const val TAG = "GameList"

typealias OnItemFn = (id: String?) -> Unit


@Composable
fun GameList(
    gameList: List<Game>,
    photoList: List<Photo>,
    onItemClick: OnItemFn,
    modifier: Modifier
) {
    val tag by rememberSaveable { mutableStateOf("GameList") }

    Log.d(tag, "recompose")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            items(gameList) { game ->
                val photosOfGame = photoList.filter { it.gameId == game.id }
                GameDetail(game, photosOfGame, onItemClick)
            }
        }
    }
}

@Composable
fun GameDetail(game: Game, photos: List<Photo>, onItemClick: OnItemFn) {
    val context = LocalContext.current
    val firstPhotoBitmap: Bitmap? = photos.firstOrNull()?.loadImageFromFile(context)

//    val tag: String by rememberSaveable { mutableStateOf("GameDetail") }
//    Log.d(tag, "recompose")

    // For animating the game detail
    // Shake animation and border color change

    val borderColorInfTransition = rememberInfiniteTransition(label = "borderColorTransition")
    val borderColor by borderColorInfTransition.animateColor(
        initialValue = Color.Red,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                Color.Red at 0
                Color.Blue at 1000
                Color.Green at 1500
                Color.Yellow at 2000
                Color.Magenta at 2500
                Color.Red at 5000
            },
            repeatMode = RepeatMode.Restart
        ), label = "borderColor"
    )

    val offsetXInfTransition = rememberInfiniteTransition(label = "offsetXTransition")
    val offsetX by offsetXInfTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0f at 0
                10f at 1000
                0f at 2000
                -10f at 3000
                0f at 5000
            },
            repeatMode = RepeatMode.Restart
        ), label = "offsetX"
    )

    val offsetYInfTransition = rememberInfiniteTransition(label = "offsetYTransition")
    val offsetY by offsetYInfTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0f at 0
                10f at 1000
                0f at 2000
                -10f at 3000
                0f at 5000
            },
            repeatMode = RepeatMode.Restart
        ), label = "offsetY"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(4.dp, borderColor)
            .shadow(4.dp, shape = CardDefaults.elevatedShape)
            .offset(x = offsetX.dp, y = offsetY.dp),
        shape = CardDefaults.elevatedShape,
        border = CardDefaults.outlinedCardBorder(),
        onClick = { onItemClick(game.id) }
    ) {
        if (firstPhotoBitmap != null) {
            Image(
                bitmap = firstPhotoBitmap.asImageBitmap(),
                contentDescription = "Game image of ${game.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
                    .border(1.dp, Color.Black),
                contentScale = ContentScale.Crop,
            )
        }
        // Game title

        Text(
            text = game.title,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .align(alignment = Alignment.CenterHorizontally),
            style = TextStyle(
                fontSize = 18.sp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                    ),
                    start = Offset(-0.5f, -0.5f),
                    end = Offset(0.5f, 0.5f)
                ),
                shadow = Shadow(
                    offset = Offset(0.5f, 0.5f),
                    blurRadius = 1f
                ),
                textDecoration = TextDecoration.Underline,
            )
        )

        // Game release date
        Text(
            text = "Release date: ${game.releaseDate.toDate().formatTo("dd-MM-yyyy")}",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
            style = TextStyle(
                color = Color.DarkGray,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
        )

        // Game properties
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
                .border(1.dp, Color.Gray)
                .shadow(4.dp)
                .background(Color.Black),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
                    .shadow(4.dp)
                    .background(Color.Black),
            ) {
                Text(
                    text = "Rating: ${game.rating}",
                    modifier = Modifier.padding(8.dp),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                )
                Text(
                    text = "Rental price: ${game.rentalPrice} COINS",
                    modifier = Modifier
                        .padding(8.dp),
                    style = TextStyle(
                        color = Color.Green,
                        fontSize = 12.sp,
                    )
                )
                Text(
                    text = "Category: ${game.category}",
                    modifier = Modifier.padding(8.dp),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun GameDetailPreview() {
    GameDetail(
        Game(
            id = "1",
            title = "Title",
        ),
        listOf(
            Photo(
                "1",
                "1",
                "1",
                "filepath",
                "data"
            )
        )
    ) { }
}