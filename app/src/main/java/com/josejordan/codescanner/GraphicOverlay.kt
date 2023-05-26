package com.josejordan.codescanner

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.vision.CameraSource
import java.util.HashSet

class GraphicOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val lock = Any()
    private val graphics: MutableSet<Graphic> = HashSet()

    abstract class Graphic(private val overlay: GraphicOverlay) {
        abstract fun draw(canvas: Canvas)

        fun scaleX(horizontal: Float): Float {
            return horizontal * overlay.widthScaleFactor
        }

        fun scaleY(vertical: Float): Float {
            return vertical * overlay.heightScaleFactor
        }

        fun translateX(x: Float): Float {
            return if (overlay.facing == CameraSource.CAMERA_FACING_FRONT) {
                overlay.width - scaleX(x)
            } else {
                scaleX(x)
            }
        }

        fun translateY(y: Float): Float {
            return scaleY(y)
        }

    /*    fun postInvalidate() {
            overlay.postInvalidate()
        }*/
    }

    var widthScaleFactor = 1.0f
    var heightScaleFactor = 1.0f
    var facing = CameraSource.CAMERA_FACING_BACK

    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }

    fun setCameraInfo(previewWidth: Int, previewHeight: Int) {
        synchronized(lock) {
            widthScaleFactor = width.toFloat() / previewWidth.toFloat()
            heightScaleFactor = height.toFloat() / previewHeight.toFloat()
        }
        postInvalidate()
    }

}
