package com.github.artnest.rollaball

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.concurrent.thread
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class JoystickView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        /**
         * Default refresh rate as a time in milliseconds to send move values through callback
         */
        private const val DEFAULT_LOOP_INTERVAL = 50L // in milliseconds

        /**
         * Default color for button
         */
        private const val DEFAULT_COLOR_BUTTON = Color.BLACK

        /**
         * Default color for border
         */
        private const val DEFAULT_COLOR_BORDER = Color.TRANSPARENT

        /**
         * Default alpha for border
         */
        private const val DEFAULT_ALPHA_BORDER = 255

        /**
         * Default background color
         */
        private const val DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT

        /**
         * Default View's size
         */
        private const val DEFAULT_SIZE = 200

        /**
         * Default border's width
         */
        private const val DEFAULT_WIDTH_BORDER = 3

        /**
         * Default behavior to fixed center (not auto-defined)
         */
        private const val DEFAULT_FIXED_CENTER = true

        /**
         * Default behavior to auto re-center button (automatically recenter the button)
         */
        private const val DEFAULT_AUTO_RECENTER_BUTTON = true

        /**
         * Default behavior to button stickToBorder (button stay on the border)
         */
        private const val DEFAULT_BUTTON_STICK_TO_BORDER = false

        /**
         * Default value.
         * Both direction correspond to horizontal and vertical movement
         */
        var BUTTON_DIRECTION_BOTH = 0
    }

    private val mPaintCircleButton: Paint
    private val mPaintCircleBorder: Paint
    private val mPaintBackground: Paint

    /**
     * Ratio use to define the size of the button
     */
    private var mButtonSizeRatio: Float = 0f

    /**
     * Ratio use to define the size of the background
     */
    private var mBackgroundSizeRatio: Float = 0f

    private var mPosX = 0
    private var mPosY = 0
    private var mCenterX = 0
    private var mCenterY = 0

    private var mFixedCenterX = 0
    private var mFixedCenterY = 0

    /**
     * Used to adapt behavior whether it is auto-defined center (false) or fixed center (true)
     */
    private var mFixedCenter: Boolean = false

    /**
     * Used to adapt behavior whether the button is automatically re-centered (true)
     * when released or not (false)
     */
    var isAutoReCenterButton: Boolean = false

    /**
     * Used to adapt behavior whether the button is stick to border (true) or
     * could be anywhere (when false - similar to regular behavior)
     */
    var isButtonStickToBorder: Boolean = false

    /**
     * Used to enable/disable the Joystick. When disabled the joystick button
     * can't move and onMove is not called.
     */
    private var mEnabled: Boolean = false

    private var mButtonRadius: Int = 0
    private var mBorderRadius: Int = 0

    /**
     * Alpha of the border (to use when changing color dynamically)
     */
    private var mBorderAlpha: Int = 0

    /**
     * Based on mBorderRadius but a bit smaller (minus half the stroke size of the border)
     */
    private var mBackgroundRadius: Float = 0f

    /**
     * Listener used to dispatch OnMove event
     */
    private var mCallback: OnMoveListener? = null

    private var loopInterval = DEFAULT_LOOP_INTERVAL
    private val threadBlock: () -> Unit = {
        while (!Thread.interrupted()) {
            post {
                if (mCallback != null) {
                    mCallback!!.onMove(angle, strength)
                }
            }

            try {
                Thread.sleep(loopInterval)
            } catch (e: InterruptedException) {
                break
            }
        }
    }
    private var thread: Thread? = thread(start = false, block = threadBlock)

    /**
     * The allowed direction of the button is define by the value of this parameter:
     * - a negative value for horizontal axe
     * - a positive value for vertical axe
     * - zero for both axes
     */
    var buttonDirection = 0

    /**
     * Process the angle following the 360° counter-clock protractor rules.
     *
     * @return the angle of the button
     */
    private val angle: Int
        get() {
            val angle = Math.toDegrees(atan2((mCenterY - mPosY).toDouble(), (mPosX - mCenterX).toDouble())).toInt()
            return if (angle < 0) angle + 360 else angle // make it as a regular counter-clock protractor
        }

    /**
     * Process the strength as a percentage of the distance between the center and the border.
     *
     * @return the strength of the button
     */
    private val strength: Int
        get() = (100 * sqrt(((mPosX - mCenterX) *
                (mPosX - mCenterX) + (mPosY - mCenterY) *
                (mPosY - mCenterY)).toDouble()) / mBorderRadius)
                .toInt()

    /**
     * Return the size of the button (as a ratio of the total width/height)
     * Default is 0.25 (25%).
     *
     * @return button size (value between 0.0 and 1.0)
     */
    var buttonSizeRatio: Float
        get() = mButtonSizeRatio
        set(newRatio) {
            if ((newRatio > 0.0f) and (newRatio <= 1.0f)) {
                mButtonSizeRatio = newRatio
            }
        }

    /**
     * Return the relative X coordinate of button center related
     * to top-left virtual corner of the border.
     *
     * @return coordinate of X (normalized between 0 and 100)
     */
    val normalizedX: Int
        get() = if (width == 0) {
            50
        } else {
            ((mPosX - mButtonRadius) * 100.0f / (width - mButtonRadius * 2)).roundToInt()
        }

    /**
     * Return the relative Y coordinate of the button center related
     * to top-left virtual corner of the border.
     *
     * @return coordinate of Y (normalized between 0 and 100)
     */
    val normalizedY: Int
        get() = if (height == 0) {
            50
        } else {
            ((mPosY - mButtonRadius) * 100.0f / (height - mButtonRadius * 2)).roundToInt()
        }

    /**
     * The transparency of the border between 0 and 255.
     *
     * @return it is an integer between 0 and 255 previously set
     */
    var borderAlpha: Int
        get() = mBorderAlpha
        set(alpha) {
            mBorderAlpha = alpha
            mPaintCircleBorder.alpha = alpha
            invalidate()
        }

    /**
     * Interface definition for a callback to be invoked when a
     * JoystickView's button is moved
     */
    interface OnMoveListener {

        /**
         * Called when a JoystickView's button has been moved
         *
         * @param angle    current angle
         * @param strength current strength
         */
        fun onMove(angle: Int, strength: Int)
    }


    init {
        val buttonColor: Int = DEFAULT_COLOR_BUTTON
        val borderColor: Int = DEFAULT_COLOR_BORDER
        val backgroundColor: Int = DEFAULT_BACKGROUND_COLOR
        val borderWidth: Int = DEFAULT_WIDTH_BORDER
        mBorderAlpha = DEFAULT_ALPHA_BORDER
        mFixedCenter = DEFAULT_FIXED_CENTER
        isAutoReCenterButton = DEFAULT_AUTO_RECENTER_BUTTON
        isButtonStickToBorder = DEFAULT_BUTTON_STICK_TO_BORDER
        mEnabled = true
        mButtonSizeRatio = 0.25f
        mBackgroundSizeRatio = 0.75f
        buttonDirection = BUTTON_DIRECTION_BOTH

        // Initialize the drawing

        mPaintCircleButton = Paint()
        mPaintCircleButton.isAntiAlias = true
        mPaintCircleButton.color = buttonColor
        mPaintCircleButton.style = Paint.Style.FILL

        mPaintCircleBorder = Paint()
        mPaintCircleBorder.isAntiAlias = true
        mPaintCircleBorder.color = borderColor
        mPaintCircleBorder.style = Paint.Style.STROKE
        mPaintCircleBorder.strokeWidth = borderWidth.toFloat()
        mPaintCircleBorder.alpha = mBorderAlpha

        mPaintBackground = Paint()
        mPaintBackground.isAntiAlias = true
        mPaintBackground.color = backgroundColor
        mPaintBackground.style = Paint.Style.FILL
    }

    private fun initPosition() {
        // get the center of view to position circle
        mPosX = width / 2
        mCenterX = mPosX
        mFixedCenterX = mCenterX
        mPosY = width / 2
        mCenterY = mPosY
        mFixedCenterY = mCenterY
    }

    /**
     * Draw the background, the border and the button
     *
     * @param canvas the canvas on which the shapes will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        // Draw the background
        canvas.drawCircle(mFixedCenterX.toFloat(), mFixedCenterY.toFloat(), mBackgroundRadius, mPaintBackground)

        // Draw the circle border
        canvas.drawCircle(mFixedCenterX.toFloat(), mFixedCenterY.toFloat(), mBorderRadius.toFloat(), mPaintCircleBorder)

        // Draw the button as simple circle
        canvas.drawCircle(
                (mPosX + mFixedCenterX - mCenterX).toFloat(),
                (mPosY + mFixedCenterY - mCenterY).toFloat(),
                mButtonRadius.toFloat(),
                mPaintCircleButton
        )
    }


    /**
     * This is called during layout when the size of this view has changed.
     * Here we get the center of the view and the radius to draw all the shapes.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldW Old width of this view.
     * @param oldH Old height of this view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)

        initPosition()

        // radius based on smallest size : height OR width
        val d = w.coerceAtMost(h)
        mButtonRadius = (d / 2 * mButtonSizeRatio).toInt()
        mBorderRadius = (d / 2 * mBackgroundSizeRatio).toInt()
        mBackgroundRadius = mBorderRadius - mPaintCircleBorder.strokeWidth / 2
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // setting the measured values to resize the view to a certain width and height
        val d = min(measure(widthMeasureSpec), measure(heightMeasureSpec))
        setMeasuredDimension(d, d)
    }

    private fun measure(measureSpec: Int): Int {
        return if (View.MeasureSpec.getMode(measureSpec) == View.MeasureSpec.UNSPECIFIED) {
            // if no bounds are specified return a default size (200)
            DEFAULT_SIZE
        } else if (View.MeasureSpec.getMode(measureSpec) == View.MeasureSpec.AT_MOST) {
            // As you want to fill the available space
            // always return the full available bounds.
            min(DEFAULT_SIZE, MeasureSpec.getSize(measureSpec))
        } else {
            MeasureSpec.getSize(measureSpec)
        }
    }

    /**
     * Handle touch screen motion event. Move the button according to the
     * finger coordinate and detect longPress by multiple pointers only.
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // if disabled we don't move the joystick
        if (!mEnabled) {
            return true
        }

        // to move the button according to the finger coordinate
        // (or limited to one axe according to direction option
        mPosY = if (buttonDirection < 0) mCenterY else event.y.toInt() // direction negative is horizontal axe
        mPosX = if (buttonDirection > 0) mCenterX else event.x.toInt() // direction positive is vertical axe

        if (event.action == MotionEvent.ACTION_UP) {
            // stop listener because the finger left the touch screen
            thread!!.interrupt()

            // re-center the button or not (depending on settings)
            if (isAutoReCenterButton) {
                resetButtonPosition()

                // update now the last strength and angle which should be zero after resetButton
                if (mCallback != null) {
                    mCallback!!.onMove(angle, strength)
                }
            }

            // if mAutoReCenterButton is false we will send the last strength and angle a bit
            // later only after processing new position X and Y otherwise it could be above the border limit
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread!!.isAlive) {
                thread!!.interrupt()
            }

            thread = thread(block = threadBlock)

            if (mCallback != null) {
                mCallback!!.onMove(angle, strength)
            }
        }

        val abs = sqrt(((mPosX - mCenterX) *
                (mPosX - mCenterX) + (mPosY - mCenterY) *
                (mPosY - mCenterY))
                .toDouble())

        // (abs > mBorderRadius) means button is too far therefore we limit to border
        // (buttonStickBorder && abs != 0) means wherever is the button we stick it to the border except when abs == 0
        if (abs > mBorderRadius || isButtonStickToBorder && abs != 0.0) {
            mPosX = ((mPosX - mCenterX) * mBorderRadius / abs + mCenterX).toInt()
            mPosY = ((mPosY - mCenterY) * mBorderRadius / abs + mCenterY).toInt()
        }

        if (!isAutoReCenterButton) {
            // Now update the last strength and angle if not reset to center
            if (mCallback != null) {
                mCallback!!.onMove(angle, strength)
            }
        }

        // to force a new draw
        invalidate()

        return true
    }

    /**
     * Reset the button position to the center.
     */
    fun resetButtonPosition() {
        mPosX = mCenterX
        mPosY = mCenterY
    }

    /**
     * Return the state of the joystick. False when the button don't move.
     *
     * @return the state of the joystick
     */
    override fun isEnabled(): Boolean {
        return mEnabled
    }

    /**
     * Return the size of the background (as a ratio of the total width/height)
     * Default is 0.75 (75%).
     *
     * @return background size (value between 0.0 and 1.0)
     */
    fun getmBackgroundSizeRatio(): Float {
        return mBackgroundSizeRatio
    }

    /**
     * Set the button color for this JoystickView.
     *
     * @param color the color of the button
     */
    fun setButtonColor(color: Int) {
        mPaintCircleButton.color = color
        invalidate()
    }

    /**
     * Set the border color for this JoystickView.
     *
     * @param color the color of the border
     */
    fun setBorderColor(color: Int) {
        mPaintCircleBorder.color = color
        if (color != Color.TRANSPARENT) {
            mPaintCircleBorder.alpha = mBorderAlpha
        }
        invalidate()
    }

    /**
     * Set the background color for this JoystickView.
     *
     * @param color the color of the background
     */
    override fun setBackgroundColor(color: Int) {
        mPaintBackground.color = color
        invalidate()
    }

    /**
     * Set the border width for this JoystickView.
     *
     * @param width the width of the border
     */
    fun setBorderWidth(width: Int) {
        mPaintCircleBorder.strokeWidth = width.toFloat()
        mBackgroundRadius = mBorderRadius - width / 2.0f
        invalidate()
    }

    /**
     * Register a callback to be invoked when this JoystickView's button is moved
     *
     * @param onMoveListener            The callback that will run
     * @param loopInterval Refresh rate to be invoked in milliseconds
     */
    @JvmOverloads
    fun setOnMoveListener(onMoveListener: OnMoveListener, loopInterval: Long = DEFAULT_LOOP_INTERVAL) {
        mCallback = onMoveListener
        this.loopInterval = loopInterval
    }

    /**
     * Set the joystick center's behavior (fixed or auto-defined)
     *
     * @param fixedCenter True for fixed center, False for auto-defined center based on touch down
     */
    fun setFixedCenter(fixedCenter: Boolean) {
        // if we set to "fixed" we make sure to re-init position related to the width of the joystick
        if (fixedCenter) {
            initPosition()
        }
        mFixedCenter = fixedCenter
        invalidate()
    }

    /**
     * Enable or disable the joystick
     *
     * @param enabled False mean the button won't move and onMove won't be called
     */
    override fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    /**
     * Set the joystick button size (as a fraction of the real width/height)
     * By default it is 75% (0.75).
     * Not working if the background is an image.
     *
     * @param newRatio between 0.0 and 1.0
     */
    fun setBackgroundSizeRatio(newRatio: Float) {
        if ((newRatio > 0.0f) and (newRatio <= 1.0f)) {
            mBackgroundSizeRatio = newRatio
        }
    }
}
