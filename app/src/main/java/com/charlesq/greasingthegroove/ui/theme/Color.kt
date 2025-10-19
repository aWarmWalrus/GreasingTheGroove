package com.charlesq.greasingthegroove.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Japandi Palette
val JapandiBeige = Color(0xFFD9C6B0) // Slightly darker beige for better contrast
val JapandiMutedGreen = Color(0xFF93A18F) // Slightly darker green
val JapandiLightGray = Color(0xFFC0C0C0) // Standard light gray
val JapandiCharcoal = Color(0xFF3E3E3E) // Slightly lighter charcoal
val JapandiOffWhite = Color(0xFFF9F9F9)
val JapandiDarkText = Color(0xFF363636)
val JapandiLightText = Color(0xFFF5F5F5)
val JapandiRedAccent = Color(0xFFB95C50) // Muted, earthy red

// Body Part Colors
val MutedBlue = Color(0xFF8aa3b0)
val Terracotta = Color(0xFFb08a8a)
val SageGreen = Color(0xFF8ab08a)
val SlateGray = Color(0xFF8a8ab0)
val SandyBrown = Color(0xFFb0a38a)
val RichTaupe = Color(0xFF7D6D61)


fun getContrastingTextColor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.5) {
        JapandiDarkText
    } else {
        JapandiLightText
    }
}
