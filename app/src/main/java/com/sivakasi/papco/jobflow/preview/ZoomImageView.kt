@file:Suppress("SameParameterValue")

package com.sivakasi.papco.jobflow.preview

import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ZoomableImageView(
    imageUrl: String,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()

    var imageSize = remember { IntSize.Zero }
    var viewPortSize = remember { IntSize.Zero }

    var scale by remember {
        mutableFloatStateOf(1f)
    }
    var pan by remember {
        mutableStateOf(Offset.Zero)
    }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit){
                detectTransformGestures { centroid, panChange, scaleChange, _ ->
                    val newScale = (scale * scaleChange).coerceIn(1f, 5f)
                    val bounds = movementBounds(imageSize,viewPortSize,newScale)
                    val boundsX = (imageSize.width*scale-viewPortSize.width)/2
                    val boundsY = (imageSize.height*scale-viewPortSize.height)/2
                    val cy = imageSize.height*scale/2
                    val cx = imageSize.width*scale/2
                    val py = cy - (boundsY-pan.y+centroid.y)
                    val px= cx - (boundsX-pan.x+centroid.x)
                    val movedPy = py/scale*newScale
                    val movedPx = px/scale*newScale
                    val dy = movedPy - py
                    val dx = movedPx - px

                    scale=newScale
                    pan = Offset(
                        x= (pan.x + panChange.x+ dx).coerceIn(-bounds.width,bounds.width),
                        y= (pan.y + panChange.y + dy).coerceIn(-bounds.height,bounds.height)
                    )

                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale == 1f) {
                            scope.launch {
                                animate(
                                    initialValue = 1f,
                                    targetValue = 3f
                                ) { value, _ ->
                                    scale = value
                                }
                            }

                            scope.launch {
                                animate(
                                    initialValue = pan,
                                    targetValue = calculateZoomOffset(
                                        touchOffset = tapOffset,
                                        imageSize = imageSize,
                                        scale = 3f,
                                        viewPortSize
                                    ),
                                    typeConverter = Offset.VectorConverter
                                ) { value, _ ->
                                    pan = value
                                }
                            }

                        } else {
                            scope.launch {
                                animate(
                                    initialValue = pan,
                                    targetValue = Offset(0f, 0f),
                                    typeConverter = Offset.VectorConverter
                                ) { value, _ ->
                                    pan = value
                                }
                            }

                            scope.launch {
                                animate(
                                    initialValue = scale,
                                    targetValue = 1f
                                ) { value, _ ->
                                    scale = value
                                }
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {

        AsyncImage(
            model = imageUrl,
            modifier = Modifier
                .onSizeChanged {
                    imageSize = it
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = pan.x
                    translationY = pan.y
                },
            contentDescription = null
        )

        LaunchedEffect(Unit) {
            viewPortSize = IntSize(constraints.maxWidth, constraints.maxHeight)
        }
    }
}

private fun calculateZoomOffset(
    touchOffset: Offset,
    imageSize: IntSize,
    scale: Float,
    viewportSize: IntSize
): Offset {

    //The Given touch offset is relative to the viewport. Convert it relative to the Image
    val imageTouchOffsetX =
        touchOffset.x / (viewportSize.width.toFloat() / imageSize.width.toFloat())
    val imageTouchOffsetY =
        touchOffset.y / (viewportSize.height.toFloat() / imageSize.height.toFloat())

    //Now, convert the mapped touch offset relative to the scale
    val targetX = scale * imageTouchOffsetX
    val targetY = scale * imageTouchOffsetY

    //Calculate the amount of offset that actually needs to move to reach the center of the screen
    val moveX = (imageSize.width * scale / 2f) - targetX
    val moveY = (imageSize.height * scale / 2f) - targetY

    val bounds = movementBounds(imageSize, viewportSize, scale)

    return Offset(
        x = moveX.coerceIn(-bounds.width, bounds.width),
        y = moveY.coerceIn(-bounds.height, bounds.height)
    )
}

private fun movementBounds(
    imageSize: IntSize,
    viewportSize: IntSize,
    scale: Float
): Size {
    val maxX =
        ((imageSize.width * scale - viewportSize.width) / 2).coerceIn(0f, Float.MAX_VALUE)
    val maxY = ((imageSize.height * scale - viewportSize.height) / 2).coerceIn(
        0f,
        Float.MAX_VALUE
    )

    return Size(maxX, maxY)
}
