package com.boxdotsize.boxdotsize_android.retrofit

data class ParamsDTO(
    val cx: Double,
    val cy: Double,
    val dist: List<List<Double>>,
    val fx: Double,
    val fy: Double,
    val rvec: List<List<Double>>
)
