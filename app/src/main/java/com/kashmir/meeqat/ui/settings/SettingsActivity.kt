package com.kashmir.meeqat.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.kashmir.meeqat.R
import com.kashmir.meeqat.data.KashmirLocations
import com.kashmir.meeqat.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "meeqat_preferences"
        private const val PREF_LOCATION_MODE = "location_mode"
        private const val PREF_SELECTED_LOCATION = "selected_location"
        private const val PREF_CALCULATION_METHOD = "calculation_method"
        private const val PREF_FAJR_NOTIFICATION = "fajr_notification"
        private const val PREF_DHUHR_NOTIFICATION = "dhuhr_notification"
        private const val PREF_ASR_NOTIFICATION = "asr_notification"
        private const val PREF_MAGHRIB_NOTIFICATION = "maghrib_notification"
        private const val PREF_ISHA_NOTIFICATION = "isha_notification"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        initializePreferences()
        setupLocationSettings()
        setupCalculationMethod()
        setupNotificationSettings()
        loadSavedSettings()
        setupListeners()
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
    
    private fun initializePreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private fun setupLocationSettings() {
        // Setup location spinner
        val locationNames = KashmirLocations.locations.map { it.name }
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locationNames)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.adapter = locationAdapter
    }
    
    private fun setupCalculationMethod() {
        val calculationMethods = arrayOf(
            "Karachi (University of Islamic Sciences)",
            "ISNA (Islamic Society of North America)",
            "MWL (Muslim World League)",
            "Makkah (Umm Al-Qura University)",
            "Egypt (Egyptian General Authority)",
            "Kashmir Custom"
        )
        
        val methodAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, calculationMethods)
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCalculationMethod.adapter = methodAdapter
    }
    
    private fun setupNotificationSettings() {
        // Notification switches are already setup in XML
    }
    
    private fun loadSavedSettings() {
        // Load location mode
        val locationMode = sharedPreferences.getString(PREF_LOCATION_MODE, "auto") ?: "auto"
        binding.rbAutoLocation.isChecked = locationMode == "auto"
        binding.rbManualLocation.isChecked = locationMode == "manual"
        
        // Show/hide location spinner based on mode
        binding.spinnerLocation.visibility = if (locationMode == "manual") View.VISIBLE else View.GONE
        
        // Load selected location
        val selectedLocation = sharedPreferences.getString(PREF_SELECTED_LOCATION, "Srinagar") ?: "Srinagar"
        val locationIndex = KashmirLocations.locations.indexOfFirst { it.name == selectedLocation }
        if (locationIndex >= 0) {
            binding.spinnerLocation.setSelection(locationIndex)
        }
        
        // Load calculation method
        val calculationMethod = sharedPreferences.getString(PREF_CALCULATION_METHOD, "KARACHI") ?: "KARACHI"
        val methodIndex = when (calculationMethod) {
            "KARACHI" -> 0
            "ISNA" -> 1
            "MWL" -> 2
            "MAKKAH" -> 3
            "EGYPT" -> 4
            "KASHMIR_CUSTOM" -> 5
            else -> 0
        }
        binding.spinnerCalculationMethod.setSelection(methodIndex)
        
        // Load notification settings
        binding.switchFajrNotification.isChecked = sharedPreferences.getBoolean(PREF_FAJR_NOTIFICATION, true)
        binding.switchDhuhrNotification.isChecked = sharedPreferences.getBoolean(PREF_DHUHR_NOTIFICATION, true)
        binding.switchAsrNotification.isChecked = sharedPreferences.getBoolean(PREF_ASR_NOTIFICATION, true)
        binding.switchMaghribNotification.isChecked = sharedPreferences.getBoolean(PREF_MAGHRIB_NOTIFICATION, true)
        binding.switchIshaNotification.isChecked = sharedPreferences.getBoolean(PREF_ISHA_NOTIFICATION, true)
        
        // Update current location display
        updateCurrentLocationDisplay()
    }
    
    private fun setupListeners() {
        // Location mode radio buttons
        binding.rgLocationMode.setOnCheckedChangeListener { _, checkedId ->
            val isManual = checkedId == R.id.rbManualLocation
            binding.spinnerLocation.visibility = if (isManual) View.VISIBLE else View.GONE
            
            val mode = if (isManual) "manual" else "auto"
            sharedPreferences.edit().putString(PREF_LOCATION_MODE, mode).apply()
            updateCurrentLocationDisplay()
        }
        
        // Location spinner
        binding.spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLocation = KashmirLocations.locations[position].name
                sharedPreferences.edit().putString(PREF_SELECTED_LOCATION, selectedLocation).apply()
                updateCurrentLocationDisplay()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Calculation method spinner
        binding.spinnerCalculationMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val method = when (position) {
                    0 -> "KARACHI"
                    1 -> "ISNA"
                    2 -> "MWL"
                    3 -> "MAKKAH"
                    4 -> "EGYPT"
                    5 -> "KASHMIR_CUSTOM"
                    else -> "KARACHI"
                }
                sharedPreferences.edit().putString(PREF_CALCULATION_METHOD, method).apply()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Notification switches
        binding.switchFajrNotification.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(PREF_FAJR_NOTIFICATION, isChecked).apply()
        }
        
        binding.switchDhuhrNotification.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(PREF_DHUHR_NOTIFICATION, isChecked).apply()
        }
        
        binding.switchAsrNotification.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(PREF_ASR_NOTIFICATION, isChecked).apply()
        }
        
        binding.switchMaghribNotification.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(PREF_MAGHRIB_NOTIFICATION, isChecked).apply()
        }
        
        binding.switchIshaNotification.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(PREF_ISHA_NOTIFICATION, isChecked).apply()
        }
    }
    
    private fun updateCurrentLocationDisplay() {
        val locationMode = sharedPreferences.getString(PREF_LOCATION_MODE, "auto") ?: "auto"
        val currentLocation = if (locationMode == "manual") {
            val selectedLocation = sharedPreferences.getString(PREF_SELECTED_LOCATION, "Srinagar") ?: "Srinagar"
            "Current: $selectedLocation, Kashmir"
        } else {
            "Current: Auto-detected location"
        }
        binding.tvCurrentLocation.text = currentLocation
    }
}