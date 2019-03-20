package com.example.tripod.services

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.example.tripod.R
import androidx.core.graphics.rotationMatrix


class AxisService(activity: Activity) : SensorEventListener {
    val ALPHA = 0.2f

    private val sensorManager: SensorManager =
        activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager //Менеджер сенсоров аппрата

    private val rotationMatrix: FloatArray = FloatArray(16)   //Матрица поворота
    private var accelerometerData: FloatArray = FloatArray(3)           //Данные с акселерометра
    private var magnetData: FloatArray = FloatArray(3)       //Данные геомагнитного датчика
    private val orientationData: FloatArray = FloatArray(3) //Матрица положения в пространстве


    var layout1 = activity.findViewById(R.id.frameLayout) as FrameLayout
    var canvass = Canvass(activity)

    init {

        layout1.addView(canvass)

    }

    fun getOrientationData():FloatArray {
        return orientationData
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class Canvass(context: Context) : View(context) {

        var yStart = 0f
        var yFinish = 0f

        var vxStart = 0f
        var vxFinish = 0f
        var vyStart = 0f
        var vyFinish = 0f

        var angle = 0

        fun onSensorChanged(xy: Float,xz: Float,zy: Float, rotationMatrix: FloatArray){


            //val azimuth = Math.atan2(-rotationMatrix[6].toDouble(), rotationMatrix[8].toDouble()).toFloat()

            // Log.d("AZI", azimuth.toString());

            angle = xz.toInt()

            if(zy>0) angle =  90 - Math.abs(angle)
            if(zy<0) angle =  -(90 - Math.abs(angle))

            if(angle < 90){
                this.yStart = (height/2).toFloat() + (height/2)*(angle.toFloat() / 90)
                this.yFinish = (height/2).toFloat() - (height/2)*(angle.toFloat() / 90)
                this.vxStart = (width/2).toFloat() + (width/2)*(angle.toFloat() / 90)
                this.vxFinish = (width/2).toFloat() - (width/2)*(angle.toFloat() / 90)
                this.vyStart = yStart + height/2
                this.vyFinish = yFinish - height/2



            } else {
                this.yStart = (height/2).toFloat() - (height/2)*(angle.toFloat() / 90)
                this.yFinish = (height/2).toFloat() + (height/2)*(angle.toFloat() / 90)

                this.vxStart = (width/2).toFloat() - (width/2)*(angle.toFloat() / 90)
                this.vxFinish = (width/2).toFloat() + (width/2)*(angle.toFloat() / 90)
                this.vyStart = yStart - height/2
                this.vyFinish = yFinish + height/2

            }


        }

        override fun onDraw(canvas: Canvas) {
            val width = getWidth()
            val height = getHeight()
            val paint = Paint()
            paint.setStrokeWidth(4f)

            canvas.drawLine(0f, (height/2).toFloat(), width.toFloat(), (height/2).toFloat(), paint)
            canvas.drawLine((width/2).toFloat(), (height - 10).toFloat(), (width/2).toFloat(), (10).toFloat(), paint)
            paint.color = Color.RED
            paint.setStrokeWidth(6f)

            val dx = width.toFloat() - 0f
            val dy = yFinish - yStart

            val ox = 0f + (dx - dy) / 2;
            val oy = yStart + (dx + dy) / 2;

            canvas.drawLine(0f,yStart , width.toFloat(), yFinish, paint)
            canvas.drawLine( ox, oy, ox+dy, oy-dx, paint)
            invalidate()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        loadNewSensorData(event) // Получаем данные с датчика
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerData,
            magnetData
        ) //Получаем матрицу поворота
        SensorManager.getOrientation(
            rotationMatrix,
            orientationData
        ) //Получаем данные ориентации устройства в пространстве

        Log.d("ROT", rotationMatrix[1].toString());

        Log.d("ANG", Math.toDegrees(orientationData[0].toDouble()).toFloat().toString() +" "+ Math.toDegrees(orientationData[1].toDouble()).toFloat().toString() + " " + Math.toDegrees(orientationData[2].toDouble()).toFloat().toString())
        canvass.onSensorChanged(Math.toDegrees(orientationData[0].toDouble()).toFloat(), Math.toDegrees(orientationData[1].toDouble()).toFloat(),Math.toDegrees(orientationData[2].toDouble()).toFloat(), rotationMatrix)

    }

    fun onResume() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun onPause() {
        sensorManager.unregisterListener(this)
    }

    private fun loadNewSensorData(event: SensorEvent) {
        val type = event.sensor.type //Определяем тип датчика
        if (type == Sensor.TYPE_ACCELEROMETER) { //Если акселерометр
            accelerometerData = lowPass(event.values.clone(), accelerometerData)
        }

        if (type == Sensor.TYPE_MAGNETIC_FIELD) { //Если геомагнитный датчик
            magnetData = lowPass(event.values.clone(), magnetData)
        }
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