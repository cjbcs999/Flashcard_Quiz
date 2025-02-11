package com.example.flashcardquiz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import org.xmlpull.v1.XmlPullParser
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

data class Flashcard(val question: String, val answer: String)

// parse flashcards from res/xml/flashcards.xml
fun parseFlashcardsXml(context: Context): List<Flashcard> {
    val flashcards = mutableListOf<Flashcard>()
    val parser = context.resources.getXml(R.xml.flashcards)
    var eventType = parser.eventType

    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "card") {
            var question = ""
            var answer = ""
            val cardDepth = parser.depth
            eventType = parser.next()
            while (!(eventType == XmlPullParser.END_TAG && parser.name == "card" && parser.depth == cardDepth)) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "question" -> question = parser.nextText()
                        "answer" -> answer = parser.nextText()
                    }
                }
                eventType = parser.next()
            }
            if (question.isNotEmpty() && answer.isNotEmpty()) {
                flashcards.add(Flashcard(question, answer))
            }
        }
        eventType = parser.next()
    }
    return flashcards
}


// Single flashcard component: flips when clicked
@Composable
fun FlashcardItem(flashcard: Flashcard, modifier: Modifier = Modifier) {
    // Controls whether the card is flipped
    var isFlipped by remember { mutableStateOf(false) }
    // Animates rotationY transition
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = modifier
            .width(250.dp)
            .height(150.dp)
            .clickable { isFlipped = !isFlipped }
            .graphicsLayer {
                rotationY = rotation
                // Adjust cameraDistance to enhance the 3D effect
                cameraDistance = 12 * density
            }
            .background(color = Color.LightGray)
            .padding(16.dp)
    ) {
        // Display the question if rotation is ≤ 90°, otherwise show the answer
        if (rotation <= 90f) {
            Text(
                text = flashcard.question,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // To prevent text from appearing reversed, apply an extra 180° flip
            Text(
                text = flashcard.answer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer { rotationY = 180f }
            )
        }
    }
}

// FlashcardQuizScreen: Displays flashcards using LazyRow and shuffles them every 15 seconds
@Composable
fun FlashcardQuizScreen() {
    val context = LocalContext.current
    var flashcards by remember { mutableStateOf(parseFlashcardsXml(context)) }

    // Use a coroutine to shuffle flashcards every 15 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000L)
            flashcards = flashcards.shuffled()
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(flashcards) { flashcard ->
            FlashcardItem(flashcard = flashcard)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FlashcardQuizScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlashcardQuizScreenPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            FlashcardQuizScreen()
        }
    }
}
