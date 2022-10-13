package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import timber.log.Timber
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    private var backgroundColor_default = 0
    private var backgroundColor_loading = 0
    private var text_download = ""
    private var text_loading = ""
    private var textColor = 0
    private var circleColor = 0

    private val circleRect = RectF()
    private var circleSize = 0f

    private var currentCircleValue = 0f

    private var buttonText = ""

    private lateinit var buttonTextBounds: Rect

    private val paintBtn = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.DEFAULT
    }
    private val valueAnimator = ValueAnimator().apply {
    }
    private val animatorSet = AnimatorSet().apply {
        duration = 3000
    }

    private val CircleAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            currentCircleValue = it.animatedValue as Float
            invalidate()
        }
    }
    private var currentButtonAnimation = 0f
    private lateinit var buttonAnimator: ValueAnimator


    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        Log.i("newButtonState", "$new")
        when (new) {
            ButtonState.Loading -> {
                buttonText = text_loading.toString()

                if (!::buttonTextBounds.isInitialized) {
                    buttonTextBounds = Rect()
                    paintText.getTextBounds(buttonText, 0, buttonText.length, buttonTextBounds)
                    val horizontalCenter =
                        (buttonTextBounds.right + buttonTextBounds.width() + 16.0f)
                    val verticalCenter = (heightSize / 2.0f)

                    circleRect.set(
                        horizontalCenter - circleSize,
                        verticalCenter - circleSize,
                        horizontalCenter + circleSize,
                        verticalCenter + circleSize
                    )
                }
                animatorSet.start()
            }
            ButtonState.Completed -> {
                animatorSet.cancel()
                currentButtonAnimation = 0f
                currentCircleValue = 0f
                buttonText = text_download
                invalidate()
            }
            else -> {
                buttonText = text_download
                new.takeIf { it == ButtonState.Completed }.run { animatorSet.cancel() }
            }
        }
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundColor_default = getColor(R.styleable.LoadingButton_DefaultBackgroundColor, 0)
            backgroundColor_loading = getColor(R.styleable.LoadingButton_BackgroundColor, 0)
            text_download = getText(R.styleable.LoadingButton_DefaultText) as String
            textColor = getColor(R.styleable.LoadingButton_TextColor, 0)
            text_loading = getText(R.styleable.LoadingButton_Text) as String
        }.also {
            buttonText = text_download
            circleColor = ContextCompat.getColor(context, R.color.colorAccent)
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { buttonCanvas ->
            Timber.i("onDraw()")
            buttonCanvas.apply {
                drawBackgroundColor()
                drawButtonText()
                drawProgressCircleIfLoading()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circleSize = (min(w, h) / 2.0f) * 0.4f
        buttonAnimator()
    }

    private fun AnimatorSet.playProgressCircleAndButtonBackgroundTogether() =
        apply { playTogether(CircleAnimator, buttonAnimator) }

    private fun buttonAnimator() {
        ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                currentButtonAnimation = it.animatedValue as Float
                invalidate()
            }
        }.also {
            buttonAnimator = it
            animatorSet.playProgressCircleAndButtonBackgroundTogether()
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    //----------------------------------------------------
    private fun Canvas.drawButtonText() {
        paintText.color = textColor
        drawText(buttonText, (widthSize / 2.0f), (heightSize / 1.7f), paintText)
    }

    private fun Canvas.drawBackgroundColor() {
        when (buttonState) {
            ButtonState.Loading -> {
                loadingColor()
                defaultColor()
            }
            else -> drawColor(backgroundColor_default)
        }
    }

    private fun Canvas.loadingColor() = paintBtn.apply {
        color = backgroundColor_loading
    }.run {
        drawRect(0f, 0f, currentButtonAnimation, heightSize.toFloat(), paintBtn)
    }

    private fun Canvas.defaultColor() = paintBtn.apply {
        color = backgroundColor_default
    }.run {
        drawRect(
            currentButtonAnimation,
            0f,
            widthSize.toFloat(),
            heightSize.toFloat(),
            paintBtn
        )
    }

    private fun Canvas.drawProgressCircleIfLoading() =
        buttonState.takeIf { it == ButtonState.Loading }?.let { drawProgressCircle(this) }

    private fun drawProgressCircle(buttonCanvas: Canvas) {
        paintBtn.color = circleColor
        buttonCanvas.drawArc(circleRect, 0f, currentCircleValue, true, paintBtn)
    }

    fun changeState(state: ButtonState) {
        if (state != buttonState) {
            buttonState = state
            invalidate()
        }
    }
}