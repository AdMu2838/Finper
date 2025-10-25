package com.devgarden.finper.ui.features.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devgarden.finper.R
import com.devgarden.finper.ui.theme.BackgroundWhite
import com.devgarden.finper.ui.theme.DarkTextColor
import com.devgarden.finper.ui.theme.FinperTheme
import com.devgarden.finper.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val imageRes: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Bienvenido A\nFinper",
        imageRes = R.drawable.img_onboarding_coins
    ),
    OnboardingPage(
        title = "¿Listo Para Tomar\nEl Control De Tus\nFinanzas?",
        imageRes = R.drawable.img_onboarding_phone
    )
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(PrimaryGreen)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { pageIndex ->
                val page = onboardingPages[pageIndex]
                OnboardingPageView(page = page)
            }
            Button(
                onClick = {
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkTextColor)
            ) {
                Text(text = "Siguiente", fontSize = 18.sp, color = Color.White)
            }

            PagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }
    }
}

@Composable
fun OnboardingPageView(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = page.title,
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxHeight(0.4f)
                .padding(top = 130.dp, start = 20.dp, end = 20.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            color = BackgroundWhite,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 3f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(color = PrimaryGreen.copy(alpha = 0.08f), radius = radius, center = center)
                }

                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = page.title,
                    modifier = Modifier
                        .padding(70.dp)
                        .size(250.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) DarkTextColor else Color.LightGray
            val width = animateDpAsState(
                targetValue = if (pagerState.currentPage == iteration) 25.dp else 10.dp, label = ""
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .height(10.dp)
                    .width(width.value)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun OnboardingScreenPreview() {
    FinperTheme {
        OnboardingScreen(onFinish = {})
    }
}