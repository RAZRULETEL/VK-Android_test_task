package com.mastik.currencyconverter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.mastik.currencyconverter.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val apiClient by lazy { ApiClient.create() }
    private val disposable = CompositeDisposable()
    private val apiKey by lazy { BuildConfig.CURRENCY_API_KEY }
    private val currencies by lazy { getString(R.string.currencies) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currencyList = currencies.split(",")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.fromCurrencySpinner.adapter = adapter
        binding.toCurrencySpinner.adapter = adapter
        binding.toCurrencySpinner.setSelection(currencyList.size - 1)

        val itemSelectedListener: AdapterView.OnItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    checkAndSwapCurrencies()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        // Set listeners to both spinners
        binding.fromCurrencySpinner.onItemSelectedListener = itemSelectedListener
        binding.toCurrencySpinner.onItemSelectedListener = itemSelectedListener

        binding.convertButton.setOnClickListener {
            val amount = binding.amountEditText.text.toString().toDoubleOrNull()
            val fromCurrency = binding.fromCurrencySpinner.selectedItem.toString()
            val toCurrency = binding.toCurrencySpinner.selectedItem.toString()
            if (amount != null) {
                showLoadingLayout()
                convertCurrency(amount, fromCurrency, toCurrency)
            } else {
                Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String) {
        disposable.add(
            apiClient.getRates(apiKey, toCurrency, fromCurrency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    val rate = response.data[toCurrency]
                    hideLoadingLayout()
                    if (rate != null) {
                        val convertedAmount = amount * rate
                        // Start ResultActivity with the converted amount and target currency
                        val intent = Intent(this, ResultActivity::class.java).apply {
                            putExtra("convertedAmount", convertedAmount)
                            putExtra("targetCurrency", toCurrency)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Currency not found", Toast.LENGTH_SHORT).show()
                    }
                }, { error ->
                    println(error)
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                    hideLoadingLayout()
                })
        )
    }

    private fun showLoadingLayout() {
        binding.amountEditText.clearFocus()
        binding.loadingLayout.visibility = View.VISIBLE
    }

    private fun hideLoadingLayout() {
        binding.loadingLayout.visibility = View.GONE
    }

    var oldFromCurrencyIndex: Int = -1
    var oldToCurrencyIndex: Int = -1
    private fun checkAndSwapCurrencies() {
        val fromCurrency = binding.fromCurrencySpinner.selectedItem.toString()
        val toCurrency = binding.toCurrencySpinner.selectedItem.toString()
        if (fromCurrency == toCurrency) {
            binding.fromCurrencySpinner.setSelection(oldToCurrencyIndex)
            binding.toCurrencySpinner.setSelection(oldFromCurrencyIndex)
        }
        oldFromCurrencyIndex = binding.fromCurrencySpinner.selectedItemPosition
        oldToCurrencyIndex = binding.toCurrencySpinner.selectedItemPosition
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}
