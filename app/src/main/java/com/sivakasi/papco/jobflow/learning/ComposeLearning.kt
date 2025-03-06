package com.sivakasi.papco.jobflow.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.ui.JobFlowTheme


@Composable
fun TorchLightSearch(){

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        var torchPosition by remember{
            mutableStateOf(Offset(0f,0f))
        }

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    torchPosition = Offset(it.width / 2f, it.height / 2f)
                }
                .pointerInput("Searching") {
                    detectDragGestures { _, dragAmount ->
                        torchPosition += dragAmount
                    }
                }
                .drawWithContent {
                    drawContent()

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            center = torchPosition,
                            radius = 100.dp.toPx()
                        )
                    )
                }
        )
    }

}

@Preview
@Composable
fun PreviewTorchLightScreen(){
    JobFlowTheme {
        Surface(
            color=Color.White
        ) {
            TorchLightSearch()
        }
    }
}

@Composable
fun MyCustomComposable(
    content:@Composable ()->Unit,
    modifier: Modifier=Modifier.layoutId("Sundar")
){
    Layout(content = { content()}){measurable,constraints->

        val placeables=measurable.map {
            it.measure(constraints)
        }

        layout(15,15){
            placeables.forEach {

            }
        }
    }
}

@Composable
fun LearnParentDataModifierComposable() {
    Box(
        modifier = Modifier.size(200.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.Blue)
                .align(Alignment.Center)
        )
    }
}
