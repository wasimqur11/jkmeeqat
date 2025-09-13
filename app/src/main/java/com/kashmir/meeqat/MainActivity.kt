package com.kashmir.meeqat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.kashmir.meeqat.data.*
import com.kashmir.meeqat.databinding.ActivityMainBinding
import com.kashmir.meeqat.ui.main.PrayerTimeItem
import com.kashmir.meeqat.ui.main.PrayerTimesAdapter
import com.kashmir.meeqat.ui.qibla.QiblaActivity
import com.kashmir.meeqat.ui.settings.SettingsActivity
import com.kashmir.meeqat.utils.PrayerCalculator
import com.kashmir.meeqat.services.PrayerNotificationManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var prayerTimesAdapter: PrayerTimesAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: MeeqatDatabase
    private lateinit var prayerCalculator: PrayerCalculator
    private lateinit var notificationManager: PrayerNotificationManager
    
    private var currentLocation: Location? = null
    private var currentPrayerTime: PrayerTime? = null
    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                getCurrentLocation()
            }
            else -> {
                useDefaultLocation()
                Toast.makeText(this, "Location permission denied. Using Srinagar as default.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeComponents()
        setupUI()
        requestLocationPermissions()
    }
    
    private fun initializeComponents() {
        try {
            database = MeeqatDatabase.getDatabase(this)
            prayerCalculator = PrayerCalculator()
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            notificationManager = PrayerNotificationManager(this)
            
            prayerTimesAdapter = PrayerTimesAdapter()
            binding.rvPrayerTimes.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = prayerTimesAdapter
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        
        // Set current date
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(Date())
        
        // Setup click listeners
        binding.btnQibla.setOnClickListener {
            startActivity(Intent(this, QiblaActivity::class.java))
        }
        
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        binding.locationCard.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun requestLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                locationPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }
    
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Find closest Kashmir location
                    val kashmirLocation = findClosestKashmirLocation(location.latitude, location.longitude)
                    currentLocation = kashmirLocation
                    updateLocationUI(kashmirLocation)
                    calculateAndDisplayPrayerTimes(kashmirLocation)
                } else {
                    useDefaultLocation()
                }
            }.addOnFailureListener {
                useDefaultLocation()
            }
        } catch (e: SecurityException) {
            useDefaultLocation()
        }
    }
    
    private fun findClosestKashmirLocation(lat: Double, lng: Double): Location {
        return try {
            var closestLocation = KashmirLocations.locations.first()
            var minDistance = Double.MAX_VALUE
            
            for (location in KashmirLocations.locations) {
                val distance = calculateDistance(lat, lng, location.latitude, location.longitude)
                if (distance < minDistance) {
                    minDistance = distance
                    closestLocation = location
                }
            }
            
            closestLocation
        } catch (e: Exception) {
            // Return Srinagar as fallback
            Location(name = "Srinagar", latitude = 34.0837, longitude = 74.7973)
        }
    }
    
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val latDiff = abs(lat1 - lat2)
        val lngDiff = abs(lng1 - lng2)
        return latDiff + lngDiff // Simple distance calculation
    }
    
    private fun useDefaultLocation() {
        currentLocation = KashmirLocations.getLocationByName("Srinagar") 
            ?: Location(name = "Srinagar", latitude = 34.0837, longitude = 74.7973)
        currentLocation?.let { location ->
            updateLocationUI(location)
            calculateAndDisplayPrayerTimes(location)
        }
    }
    
    private fun updateLocationUI(location: Location) {
        binding.tvLocationName.text = "${location.name}, Kashmir"
    }
    
    private fun calculateAndDisplayPrayerTimes(location: Location) {
        lifecycleScope.launch {
            try {
                val today = Date()
                
                // Check if we have cached prayer times for today
                var prayerTime = database.prayerTimeDao().getPrayerTimeForDate(
                    today, location.latitude, location.longitude
                )
                
                // If not cached, calculate new prayer times
                if (prayerTime == null) {
                    prayerTime = prayerCalculator.calculatePrayerTimes(location, today)
                    database.prayerTimeDao().insertPrayerTime(prayerTime)
                }
                
                currentPrayerTime = prayerTime
                updatePrayerTimesUI(prayerTime)
                startTimeUpdateTimer()
                
                // Schedule notifications for today's prayers
                notificationManager.scheduleNotificationsForDay(prayerTime)
                
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error calculating prayer times", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updatePrayerTimesUI(prayerTime: PrayerTime) {
        val currentTime = Calendar.getInstance()
        val currentMinutes = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
        
        val prayers = listOf(
            PrayerTimeItem("Fajr", prayerTime.fajr, R.drawable.ic_prayer),
            PrayerTimeItem("Sunrise", prayerTime.sunrise, R.drawable.ic_prayer),
            PrayerTimeItem("Dhuhr", prayerTime.dhuhr, R.drawable.ic_prayer),
            PrayerTimeItem("Asr", prayerTime.asr, R.drawable.ic_prayer),
            PrayerTimeItem("Maghrib", prayerTime.maghrib, R.drawable.ic_prayer),
            PrayerTimeItem("Isha", prayerTime.isha, R.drawable.ic_prayer)
        )
        
        // Mark prayers as passed or next
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        var nextPrayerFound = false
        
        val updatedPrayers = prayers.map { prayer ->
            try {
                val prayerDate = timeFormat.parse(prayer.time)
                val prayerCal = Calendar.getInstance().apply { 
                    time = prayerDate
                    set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
                    set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))
                }
                val prayerMinutes = prayerCal.get(Calendar.HOUR_OF_DAY) * 60 + prayerCal.get(Calendar.MINUTE)
                
                when {
                    !nextPrayerFound && prayerMinutes > currentMinutes -> {
                        nextPrayerFound = true
                        prayer.copy(isNext = true)
                    }
                    prayerMinutes <= currentMinutes -> prayer.copy(isPassed = true)
                    else -> prayer
                }
            } catch (e: Exception) {
                prayer
            }
        }
        
        prayerTimesAdapter.submitList(updatedPrayers)
        
        // Update next prayer card
        updateNextPrayerCard()
    }
    
    private fun updateNextPrayerCard() {
        currentPrayerTime?.let { prayerTime ->
            val (nextPrayerName, remainingMs) = prayerCalculator.getNextPrayerInfo(prayerTime)
            
            binding.tvNextPrayerName.text = nextPrayerName
            
            // Get next prayer time
            val nextPrayerTimeStr = when (nextPrayerName) {
                "Fajr" -> prayerTime.fajr
                "Sunrise" -> prayerTime.sunrise
                "Dhuhr" -> prayerTime.dhuhr
                "Asr" -> prayerTime.asr
                "Maghrib" -> prayerTime.maghrib
                "Isha" -> prayerTime.isha
                else -> "00:00"
            }
            
            binding.tvNextPrayerTime.text = nextPrayerTimeStr
            
            // Format remaining time
            val hours = remainingMs / (1000 * 60 * 60)
            val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
            
            binding.tvTimeRemaining.text = when {
                hours > 0 -> "in ${hours}h ${minutes}m"
                minutes > 0 -> "in ${minutes}m"
                else -> "Now"
            }
        }
    }
    
    private fun startTimeUpdateTimer() {
        updateRunnable?.let { updateHandler.removeCallbacks(it) }
        
        updateRunnable = object : Runnable {
            override fun run() {
                updateNextPrayerCard()
                currentPrayerTime?.let { updatePrayerTimesUI(it) }
                updateHandler.postDelayed(this, 60000) // Update every minute
            }
        }
        updateRunnable?.let { updateHandler.post(it) }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        updateRunnable?.let { updateHandler.removeCallbacks(it) }
    }
}