package com.josejordan.codescanner

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.android.gms.vision.barcode.Barcode


class BarcodeGraphic(overlay: GraphicOverlay, private val barcode: Barcode) :
    GraphicOverlay.Graphic(overlay) {

    private val rectPaint: Paint = Paint().apply {
        color = RECT_COLOR
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    override fun draw(canvas: Canvas) {
        val rect = RectF(barcode.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, rectPaint)
    }




    companion object {
        private const val RECT_COLOR = Color.RED
        private const val STROKE_WIDTH = 10.0f
    }
}
