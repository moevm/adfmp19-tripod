package com.example.tripod.services

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.widget.TextView
import android.hardware.SensorManager

class AxisService(activity: Activity) : SensorEventListener {
    val ALPHA = 0.2f

    private val sensorManager: SensorManager =
        activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager //Менеджер сенсоров аппрата

    private val rotationMatrix: FloatArray = FloatArray(16)   //Матрица поворота
    private var accelerometerData: FloatArray = FloatArray(3)           //Данные с акселерометра
    private var magnetData: FloatArray = FloatArray(3)       //Данные геомагнитного датчика
    private val orientationData: FloatArray = FloatArray(3) //Матрица положения в пространстве

    private val xyView: TextView = activity.findViewById(com.example.tripod.R.id.xyValue)
    private val xzView: TextView = activity.findViewById(com.example.tripod.R.id.xzValue)
    private val zyView: TextView = activity.findViewById(com.example.tripod.R.id.zyValue)

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

        //Выводим результат
        xyView.text = Math.round(Math.toDegrees(orientationData[0].toDouble())).toString()
        xzView.text = Math.round(Math.toDegrees(orientationData[1].toDouble())).toString()
        zyView.text = Math.round(Math.toDegrees(orientationData[2].toDouble())).toString()
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