package com.mastik.currencyconverter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mastik.currencyconverter.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val convertedAmount = intent.getDoubleExtra("convertedAmount", 0.0)
        val targetCurrency = intent.getStringExtra("targetCurrency")

        binding.resultTextView.text = String.format("%.2f %s", convertedAmount, targetCurrency)

        binding.backButton.setOnClickListener { finish() }
    }
}
