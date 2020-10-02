package edu.stanford.kassimo.tippy

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15
private const val KEY_LAST_TIP_PERCENT = "kLastTipPercent"

class MainActivity : AppCompatActivity() {
    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        val savedTipPercent = loadLastTipPercent()
        seekBarTip.progress = savedTipPercent
        tvTipPercent.text = "$savedTipPercent%"
        updateTipDescription(savedTipPercent)
        updateSeekBarTipProgressColor(savedTipPercent)
        seekBarTip.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "onProgressChanged $progress")
                tvTipPercent.text = "$progress%"
                updateTipDescription(progress)
                updateSeekBarTipProgressColor(progress)
                computeTipAndTotal()
                storeLastTipPercent(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        etBase.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                computeTipAndTotal()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun init() {
        if (prefs == null) {
            prefs = getSharedPreferences("Setting", Context.MODE_PRIVATE)
        }
    }

    private fun loadLastTipPercent(): Int {
        init()
        return try {
            prefs?.getInt(KEY_LAST_TIP_PERCENT, INITIAL_TIP_PERCENT) ?: INITIAL_TIP_PERCENT
        } catch (e: ClassCastException) {
            INITIAL_TIP_PERCENT
        }
    }

    private fun storeLastTipPercent(tipPercent: Int) {
        init()
        val editor = prefs!!.edit()
        editor.putInt(KEY_LAST_TIP_PERCENT, tipPercent)
        editor.apply()
    }

    private fun updateSeekBarTipProgressColor(tipPercent: Int) {
        val color = ArgbEvaluator().evaluate(
            tipPercent.toFloat() / seekBarTip.max,
            ContextCompat.getColor(this, R.color.colorWorstTip),
            ContextCompat.getColor(this, R.color.colorBestTip)
        ) as Int
        seekBarTip.progressDrawable.setTint(color)
    }

    private fun updateTipDescription(tipPercent: Int) {
        val tipDescription: String
        when (tipPercent) {
            in 0..9 -> tipDescription = "Poor"
            in 10..14 -> tipDescription = "Acceptable"
            in 15..19 -> tipDescription = "Good"
            in 20..24 -> tipDescription = "Great"
            else -> tipDescription = "Amazing"
        }
        tvTipDescription.text = tipDescription
        val color = ArgbEvaluator().evaluate(
            tipPercent.toFloat() / seekBarTip.max,
            ContextCompat.getColor(this, R.color.colorWorstTip),
            ContextCompat.getColor(this, R.color.colorBestTip)
        ) as Int
        tvTipDescription.setTextColor(color)
    }

    private fun computeTipAndTotal() {
        if (etBase.text.toString().isEmpty()) {
            tvTipAmount.text = ""
            tvTotalAmount.text = ""
            return
        }
        // Get the value of the base and tip percent
        val baseAmount = etBase.text.toString().toDouble()
        val tipPercent = seekBarTip.progress
        val tipAmount = baseAmount * tipPercent / 100
        val totalAmount = baseAmount + tipAmount
        tvTipAmount.text = "%.2f".format(tipAmount)
        tvTotalAmount.text = "%.2f".format(totalAmount)
    }
}