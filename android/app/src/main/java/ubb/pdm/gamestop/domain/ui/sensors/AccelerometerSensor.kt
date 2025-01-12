package ubb.pdm.gamestop.domain.ui.sensors

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AccelerometerSensor() {
    val accelerometerViewModel = viewModel<AccelerometerSensorViewModel>(
        factory = AccelerometerSensorViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, Color.Black)
            .background(color = Color.Magenta)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Accelerometer values: ${accelerometerViewModel.uiState}",
            style = TextStyle(
                color = Color.White,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                textAlign = TextAlign.Center
            )
        )
    }
}