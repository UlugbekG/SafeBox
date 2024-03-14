package cd.ghost.safebox.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextWithBorder(
    modifier: Modifier = Modifier,
    text: String,
    borderColor: Color = Color.Gray
) {
    Text(
        modifier = modifier
            .padding(6.dp)
            .border(
                BorderStroke(
                    2.2.dp,
                    borderColor
                ),
                RoundedCornerShape(10.dp)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    )
}