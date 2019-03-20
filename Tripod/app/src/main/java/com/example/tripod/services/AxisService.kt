package com.example.tripod.services

import android.annotation.SuppressLint
import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.FrameLayout
import com.example.tripod.CustomCanvas
import com.example.tripod.R

class AxisService(private val activity: Activity) : SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var mRotationSensor: Sensor? = null

    private var orientation: FloatArray = FloatArray(3)
    private var pitch: Float = 0f
    private var roll: Float = 0f

    private val SENSOR_DELAY = 500 * 1000 // 500ms
    private val FROM_RADS_TO_DEGS = -57
    private val ALPHA = 0.1f

    var layout1 = activity.findViewById(R.id.frameLayout) as FrameLayout
    var canvass = CustomCanvas(activity)

    init {
        layout1.addView(canvass)
        try {
            mSensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
            mRotationSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            mSensorManager?.registerListener(this, mRotationSensor, SENSOR_DELAY)
        } catch (e: Exception) {
            // you got a problem
        }
    }


    fun getOrientationData(): FloatArray {
        return orientation
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor === mRotationSensor) {
            if (event.values.size > 4) {
                val truncatedRotationVector = FloatArray(4)
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4)
                update(truncatedRotationVector)
            } else {
                update(event.values)
            }
        }

        canvass.onSensorChanged(
            roll
        )
    }

    @SuppressLint("SetTextI18n")
    private fun update(vectors: FloatArray) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors)
        val worldAxisX = SensorManager.AXIS_X
        val worldAxisZ = SensorManager.AXIS_Z
        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix)
        SensorManager.getOrientation(adjustedRotationMatrix, orientation)
        pitch = orientation[1] * FROM_RADS_TO_DEGS
        roll = orientation[2] * FROM_RADS_TO_DEGS
    }

    fun onResume() {
        mSensorManager?.registerListener(
            this,
            mRotationSensor,
            SENSOR_DELAY
        )
    }

    fun onPause() {
        mSensorManager?.unregisterListener(this)
    }

    /**
     * @see http://en.wikipedia.org/wiki/Low-pass_filter.Algorithmic_implementation
     *
     * @see http://en.wikipedia.org/wiki/Low-pass_filter.Simple_infinite_impulse_response_filter
     */
    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input

        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output.clone()
    }
}