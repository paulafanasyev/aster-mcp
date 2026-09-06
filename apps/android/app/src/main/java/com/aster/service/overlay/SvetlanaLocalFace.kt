package com.aster.service.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.widget.FrameLayout

/**
 * Aster's face renderer no longer requires a second installed app to provide the
 * first geometry frame. This is the local seed pose for Светлана. It deliberately
 * feeds the existing CompanionFaceView renderer, so blink/breath/reaction/speech
 * animation remains in the same native animation lane as the former companion rig.
 */
fun createLocalSvetlanaFaceModel(): CompanionFaceModel {
    val head = Color.rgb(255, 226, 194)
    val ink = Color.rgb(28, 28, 34)
    val red = Color.rgb(239, 82, 92)
    val blush = Color.rgb(248, 120, 130)
    val cookieBase = Color.rgb(201, 138, 78)
    val cookieChip = Color.rgb(84, 52, 28)

    val headPath = Path().apply {
        addRoundRect(24f, 18f, 176f, 132f, 48f, 48f, Path.Direction.CW)
    }

    val leftBrow = Path().apply {
        moveTo(52f, 61f); quadTo(72f, 52f, 88f, 60f)
        quadTo(70f, 56f, 52f, 66f); close()
    }
    val rightBrow = Path().apply {
        moveTo(112f, 60f); quadTo(130f, 52f, 148f, 61f)
        quadTo(130f, 56f, 112f, 66f); close()
    }

    val leftEye = Path().apply {
        addOval(52f, 67f, 78f, 88f, Path.Direction.CW)
    }
    val rightEye = Path().apply {
        addOval(122f, 67f, 148f, 88f, Path.Direction.CW)
    }

    val mouthOuter = Path().apply {
        moveTo(78f, 101f)
        quadTo(100f, 91f, 122f, 101f)
        quadTo(100f, 123f, 78f, 101f)
        close()
    }
    val mouthInterior = Path().apply {
        moveTo(84f, 101f)
        quadTo(100f, 96f, 116f, 101f)
        quadTo(100f, 116f, 84f, 101f)
        close()
    }
    val teeth = Path().apply {
        moveTo(87f, 101f); quadTo(100f, 98f, 113f, 101f)
        quadTo(100f, 106f, 87f, 101f); close()
    }

    val headphoneBand = Path().apply {
        moveTo(31f, 76f)
        cubicTo(29f, 36f, 171f, 36f, 169f, 76f)
    }

    val palette = mapOf(
        "ink" to ink,
        "head" to head,
        "red" to red,
        "blush" to blush,
    )

    val headphones = FaceHeadphones(
        alpha = 0.95f,
        band = headphoneBand,
        bandStrokeWidth = 6f,
        cups = listOf(
            FaceCup(23f, 72f, 15f, 34f, 7f),
            FaceCup(162f, 72f, 15f, 34f, 7f),
        ),
    )

    val blushes = listOf(
        FaceBlush(51f, 94f, 12f, 5f),
        FaceBlush(149f, 94f, 12f, 5f),
    )

    val cookie = FaceCookie(
        cx = 164f,
        cy = 117f,
        r = 10f,
        rotDeg = -8f,
        alpha = 0.95f,
        chips = listOf(
            FaceCircle(160f, 113f, 1.5f),
            FaceCircle(168f, 120f, 1.5f),
            FaceCircle(168f, 113f, 1.2f),
        ),
        bites = emptyList(),
    )

    return CompanionFaceModel(
        viewW = 200f,
        viewH = 150f,
        opacity = 1f,
        scale = 1f,
        offsetX = 0f,
        offsetY = 0f,
        tiltDeg = 0f,
        headPath = headPath,
        head = head,
        ink = ink,
        red = red,
        blushRed = blush,
        cookieBase = cookieBase,
        cookieChip = cookieChip,
        brows = listOf(leftBrow, rightBrow),
        eyes = listOf(leftEye, rightEye),
        mouth = FaceMouth(
            outer = mouthOuter,
            interior = mouthInterior,
            teeth = teeth,
            tiltDeg = 0f,
            pivotX = 100f,
            pivotY = 104f,
        ),
        tongue = FaceTongue(94f, 107f, 12f, 6f, 3f),
        blush = blushes,
        blushAlpha = 0.28f,
        hands = emptyList(),
        headphones = headphones,
        cookie = cookie,
        props = emptyList(),
        particles = emptyList(),
        palette = palette,
    )
}

/** Normal Android View host for the local face; no overlay permission is required. */
class SvetlanaLocalFaceView(context: Context) : FrameLayout(context) {
    private val face = CompanionFaceView(context)

    init {
        setWillNotDraw(false)
        setBackgroundColor(Color.TRANSPARENT)
        face.setFrame(createLocalSvetlanaFaceModel())
        addView(
            face,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            face.setSafeGeometry(SafeBounds(0, 0, w, h))
        }
    }
}
