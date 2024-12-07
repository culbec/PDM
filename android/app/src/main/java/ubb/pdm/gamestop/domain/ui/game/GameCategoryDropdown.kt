package ubb.pdm.gamestop.domain.ui.game

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ubb.pdm.gamestop.domain.data.game.GameCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCategoryDropdown(
    selectedCategory: GameCategory,
    onCategorySelected: (GameCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .border(1.dp, Color.Black)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(16.dp)
                .align(alignment = Alignment.CenterHorizontally)
        ) {
            
            Text(
                text = selectedCategory.value,
                style = TextStyle(fontSize = 16.sp, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            )
            Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Dropdown",
                tint = Color.Black,
                modifier = Modifier
                    .padding(end = 16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            GameCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category.value,
                            style = TextStyle(fontSize = 16.sp)
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun GameCategoryDropdownPreview() {
    GameCategoryDropdown(selectedCategory = GameCategory.Action, onCategorySelected = {})
}