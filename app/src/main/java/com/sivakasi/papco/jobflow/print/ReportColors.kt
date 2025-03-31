package com.sivakasi.papco.jobflow.print

import android.graphics.Color
import androidx.core.graphics.toColorInt

interface ReportColors {
    val primary:Int
    val primaryContainer:Int
    val onPrimaryContainer:Int
    val onPrimary:Int
    val subtleText:Int
}

data class NewPlateColors(
    override val primary: Int = "#0574BC".toColorInt(),
    override val primaryContainer: Int="#DCDDDE".toColorInt(),
    override val onPrimary: Int = Color.WHITE,
    override val onPrimaryContainer: Int= Color.BLACK,
    override val subtleText: Int = "#6D6E71".toColorInt()
):ReportColors

data class RepeatColors(
    override val primary: Int = "#3F9948".toColorInt(),
    override val primaryContainer: Int="#DCDDDE".toColorInt(),
    override val onPrimaryContainer: Int= Color.BLACK,
    override val onPrimary: Int = Color.WHITE,
    override val subtleText: Int = "#6D6E71".toColorInt()
):ReportColors

data class OutsidePlateColors(
    override val primary: Int = "#BB7027".toColorInt(),
    override val primaryContainer: Int="#DCDDDE".toColorInt(),
    override val onPrimaryContainer: Int= Color.BLACK,
    override val onPrimary: Int = Color.WHITE,
    override val subtleText: Int = "#6D6E71".toColorInt()
):ReportColors
