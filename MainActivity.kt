package com.example.gymnasticsapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gymnasticsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var score = 0
    private var element = 0
    private var routineEnded = false
    private var deductionTaken = false

    companion object {
        private const val TAG = "GymnasticsApp"
        private const val MAX_SCORE = 20
        private const val KEY_SCORE = "score"
        private const val KEY_ELEMENT = "element"
        private const val KEY_ENDED = "ended"
        private const val KEY_DEDUCTION = "deduction"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            score = savedInstanceState.getInt(KEY_SCORE, 0)
            element = savedInstanceState.getInt(KEY_ELEMENT, 0)
            routineEnded = savedInstanceState.getBoolean(KEY_ENDED, false)
            deductionTaken = savedInstanceState.getBoolean(KEY_DEDUCTION, false)
            Log.d(TAG, "onCreate: restored state score=$score, element=$element, ended=$routineEnded, deduction=$deductionTaken")
        } else {
            Log.d(TAG, "onCreate: fresh start score=$score, element=$element, ended=$routineEnded, deduction=$deductionTaken")
        }

        updateUi()

        binding.btnPerform.setOnClickListener {
            Log.d(TAG, "Perform clicked: before -> element=$element, score=$score, ended=$routineEnded, deduction=$deductionTaken")

            if (routineEnded) {
                Log.d(TAG, "Perform blocked: routine already ended")
                return@setOnClickListener
            }
            if (deductionTaken) {
                Log.d(TAG, "Perform blocked: deduction already taken")
                return@setOnClickListener
            }
            if (element >= 10) {
                routineEnded = true
                Log.d(TAG, "Perform blocked: already at element 10 -> ending routine")
                Toast.makeText(this, getString(R.string.msg_complete), Toast.LENGTH_SHORT).show()
                updateUi()
                return@setOnClickListener
            }

            element++
            val delta = pointsFor(element)
            score += delta
            if (score > MAX_SCORE) {
                Log.d(TAG, "Score clamped: attempted=$score > $MAX_SCORE")
                score = MAX_SCORE
            }
            Log.d(TAG, "Performed element=$element, delta=+$delta, newScore=$score")

            if (element == 10) {
                routineEnded = true
                Log.d(TAG, "Routine complete at element=10, finalScore=$score")
                Toast.makeText(this, getString(R.string.msg_complete), Toast.LENGTH_SHORT).show()
            }
            updateUi()
        }

        binding.btnDeduction.setOnClickListener {
            Log.d(TAG, "Deduction clicked: before -> element=$element, score=$score, ended=$routineEnded, deductionTaken=$deductionTaken")

            if (routineEnded) {
                Log.d(TAG, "Deduction blocked: routine already ended")
                return@setOnClickListener
            }
            if (element == 0) {
                Log.d(TAG, "Deduction blocked: cannot deduct before first element")
                return@setOnClickListener
            }

            if (!deductionTaken) {
                score -= 2
                if (score < 0) {
                    Log.d(TAG, "Score clamped: attempted below 0, setting to 0")
                    score = 0
                }
                deductionTaken = true
                routineEnded = true
                Log.d(TAG, "Deduction applied (-2). newScore=$score; deductionTaken=$deductionTaken; routineEnded=$routineEnded")
                Toast.makeText(this, getString(R.string.msg_deduction_end), Toast.LENGTH_SHORT).show()
                updateUi()
            } else {
                Log.d(TAG, "Deduction ignored: already taken once")
            }
        }

        binding.btnReset.setOnClickListener {
            Log.d(TAG, "Reset clicked: clearing state")
            score = 0
            element = 0
            routineEnded = false
            deductionTaken = false
            Log.d(TAG, "After reset -> score=$score, element=$element, ended=$routineEnded, deduction=$deductionTaken")
            updateUi()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SCORE, score)
        outState.putInt(KEY_ELEMENT, element)
        outState.putBoolean(KEY_ENDED, routineEnded)
        outState.putBoolean(KEY_DEDUCTION, deductionTaken)
        Log.d(TAG, "onSaveInstanceState: score=$score, element=$element, ended=$routineEnded, deduction=$deductionTaken")
    }

    private fun pointsFor(elem: Int): Int {
        val p = when (elem) {
            in 1..3 -> 1
            in 4..7 -> 2
            in 8..10 -> 3
            else -> 0
        }
        Log.d(TAG, "pointsFor(element=$elem) -> $p")
        return p
    }

    private fun updateUi() {
        binding.tvElement.text = getString(R.string.element_label, element)
        binding.tvScore.text = getString(R.string.score_label, score)

        val (colorRes, zone) = when (element) {
            in 1..3 -> R.color.zone_basic_blue to "BASIC"
            in 4..7 -> R.color.zone_intermediate_green to "INTERMEDIATE"
            in 8..10 -> R.color.zone_advanced_orange to "ADVANCED"
            else -> android.R.color.black to "NONE"
        }
        binding.tvScore.setTextColor(ContextCompat.getColor(this, colorRes))

        binding.btnPerform.isEnabled = !routineEnded && !deductionTaken && element < 10
        binding.btnDeduction.isEnabled = !routineEnded && element >= 1
        binding.btnReset.isEnabled = true

        Log.d(TAG, "updateUi: element=$element, score=$score, zone=$zone, ended=$routineEnded, deduction=$deductionTaken")
    }
}
