package com.boxdotsize.boxdotsize_android

import java.io.File

fun detectBox(file: File) : BoxDetectResult {
    // TODO : YOLO 작업

    // 파이썬에서의 xyxy: tensor([[1879.8983, 1956.3298, 2851.4976, 2786.9048]])
    // xyxy[0]하여 주자
    return BoxDetectResult(file, file, listOf(1879.8983, 1956.3298, 2851.4976, 2786.9048))
}

data class BoxDetectResult(
    val original: File,
    val crop: File,
    val xyxy: List<Double>
)