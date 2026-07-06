package com.example.landscapepro.data

import kotlin.math.sin
import kotlin.math.floor

class SeededRandom(var seed: Int) {
    fun nextFloat(): Float {
        val x = sin(seed.toDouble()) * 10000
        seed++
        val frac = x - floor(x)
        return frac.toFloat()
    }
    
    fun nextFloat(min: Float, max: Float): Float {
        return min + nextFloat() * (max - min)
    }
    
    fun nextInt(min: Int, max: Int): Int {
        return min + (nextFloat() * (max - min + 1)).toInt().coerceAtMost(max)
    }
}
