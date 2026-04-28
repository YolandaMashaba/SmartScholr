package com.example.smartscholr.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartscholr.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis

class ChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporting)

        val lineChart = findViewById<LineChart>(R.id.lineChart)

        setupChart(lineChart)
        loadSampleData(lineChart)
    }

    private fun setupChart(chart: LineChart) {
        // Remove description label
        chart.description.isEnabled = false

        // Enable touch gestures
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)

        // X Axis styling
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        // Y Axis styling
        chart.axisRight.isEnabled = false

        // Legend
        chart.legend.isEnabled = true
    }

    private fun loadSampleData(chart: LineChart) {
        val entries = listOf(
            Entry(0f, 500f),
            Entry(1f, 800f),
            Entry(2f, 650f),
            Entry(3f, 900f),
            Entry(4f, 700f)
        )

        val dataSet = LineDataSet(entries, "Monthly Expenses")

        // Styling the line
        dataSet.color = Color.parseColor("#FF6D00")
        dataSet.setCircleColor(Color.parseColor("#FF6D00"))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.valueTextSize = 10f

        val lineData = LineData(dataSet)

        chart.data = lineData
        chart.animateX(1000)
        chart.invalidate()
    }
}