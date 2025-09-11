package com.kashmir.meeqat.ui.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import com.kashmir.meeqat.data.KashmirLocations
import com.kashmir.meeqat.data.Location
import com.kashmir.meeqat.databinding.ActivityQiblaBinding
import com.kashmir.meeqat.utils.QiblaCalculator
import kotlin.math.abs

class QiblaActivity : AppCompatActivity(), SensorEventListener {
    
    private lateinit var binding: ActivityQiblaBinding
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null
    
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    
    private var currentLocation: Location? = null
    private var qiblaAngle: Double = 0.0
    private var currentAzimuth: Float = 0f
    private var lastRotationAngle: Float = 0f
    
    private var isCalibrated = false
    private val calibrationThreshold = 10f
    private var calibrationCount = 0
    private val requiredCalibrationReadings = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQiblaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        initializeSensors()
        setupLocation()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        if (magnetometer == null || accelerometer == null) {
            binding.tvCalibrationStatus.text = "Compass sensors not available"
        }
    }
    
    private fun setupLocation() {
        // Get location from preferences or use default
        val sharedPreferences = getSharedPreferences("meeqat_preferences", Context.MODE_PRIVATE)
        val locationMode = sharedPreferences.getString("location_mode", "auto") ?: "auto"
        
        currentLocation = if (locationMode == "manual") {
            val selectedLocationName = sharedPreferences.getString("selected_location", "Srinagar") ?: "Srinagar"
            KashmirLocations.getLocationByName(selectedLocationName)
        } else {
            // For auto mode, use Srinagar as default (in real app, this would be from GPS)
            KashmirLocations.getLocationByName("Srinagar")
        }
        
        currentLocation?.let { location ->
            binding.tvLocationName.text = "${location.name}, Kashmir"
            qiblaAngle = QiblaCalculator.calculateQiblaDirection(location)
            binding.tvQiblaAngle.text = QiblaCalculator.getQiblaDirectionText(qiblaAngle)
        }
    }
    
    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            }
        }
        
        updateOrientation()
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                binding.tvCalibrationStatus.text = "Compass calibrated"
                isCalibrated = true
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                binding.tvCalibrationStatus.text = "Calibration: Medium accuracy"
                isCalibrated = true
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                binding.tvCalibrationStatus.text = "Calibration: Low accuracy"
                isCalibrated = false
            }
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                binding.tvCalibrationStatus.text = "Please calibrate compass by moving device in figure-8 pattern"
                isCalibrated = false
            }
        }
    }
    
    private fun updateOrientation() {
        val rotationOK = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            
            // Azimuth (rotation around z-axis)
            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            
            // Normalize to 0-360 degrees
            if (azimuth < 0) {
                azimuth += 360f
            }
            
            // Smooth the compass movement
            val smoothedAzimuth = smoothCompassReading(azimuth)
            currentAzimuth = smoothedAzimuth
            
            // Update compass needles
            updateCompassNeedles()
            
            // Update calibration status
            updateCalibrationStatus()
        }
    }
    
    private fun smoothCompassReading(newAzimuth: Float): Float {
        // Simple low-pass filter for smoother compass movement
        val alpha = 0.1f
        val diff = abs(newAzimuth - currentAzimuth)
        
        return if (diff > 180f) {
            // Handle wraparound (359° to 1°)
            if (newAzimuth < currentAzimuth) {
                alpha * (newAzimuth + 360f) + (1f - alpha) * currentAzimuth
            } else {
                alpha * newAzimuth + (1f - alpha) * (currentAzimuth + 360f)
            }
        } else {
            alpha * newAzimuth + (1f - alpha) * currentAzimuth
        } % 360f
    }
    
    private fun updateCompassNeedles() {
        // Rotate compass background and north indicator to show current heading
        val backgroundRotation = -currentAzimuth
        rotateView(binding.ivCompassBackground, backgroundRotation)
        rotateView(binding.ivNorthIndicator, backgroundRotation)
        
        // Calculate qibla needle rotation
        // The qibla needle should point to qibla direction relative to current heading
        val qiblaRotation = (qiblaAngle - currentAzimuth).toFloat()
        rotateView(binding.ivQiblaNeedle, qiblaRotation)
    }
    
    private fun rotateView(view: android.view.View, angle: Float) {
        val rotateAnimation = RotateAnimation(
            lastRotationAngle,
            angle,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = 200
            fillAfter = true
        }
        
        view.startAnimation(rotateAnimation)
        lastRotationAngle = angle
    }
    
    private fun updateCalibrationStatus() {
        if (!isCalibrated) {
            calibrationCount = 0
            binding.tvCalibrationStatus.text = "Move phone in figure-8 to calibrate compass"
        } else {
            calibrationCount++
            if (calibrationCount >= requiredCalibrationReadings) {
                binding.tvCalibrationStatus.text = "Compass ready - Point green arrow towards Qibla"
            } else {
                binding.tvCalibrationStatus.text = "Calibrating... ${calibrationCount}/${requiredCalibrationReadings}"
            }
        }
    }
}