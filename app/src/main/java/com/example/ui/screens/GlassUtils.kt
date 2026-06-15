package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.LightTranslucent

fun Modifier.glassmorphic(
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.2.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(GlassCardBg)
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                LightTranslucent,
                Color(0x03FFFFFF)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
