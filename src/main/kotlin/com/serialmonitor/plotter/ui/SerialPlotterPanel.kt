package com.serialmonitor.plotter.ui

import com.serialmonitor.plotter.data.PlotterDataManager
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import javax.swing.JPanel
import java.util.concurrent.CopyOnWriteArraySet
import javax.swing.JLabel
import javax.swing.JPopupMenu
import org.knowm.xchart.style.markers.SeriesMarkers

/**
 * 串口绘图仪面板
 * 使用XChart库绘制实时数据图表
 * 支持：鼠标滚动缩放、自动滚动、移除节点标记
 */
class SerialPlotterPanel : JPanel() {

    private val dataManager = PlotterDataManager()
    private var chart: XYChart? = null

    // 数据系列可见性控制
    private val hiddenSeries = CopyOnWriteArraySet<String>()

    // 颜色表（预定义的颜色）
    private val colors = listOf(
        Color(31, 119, 180),    // 蓝色
        Color(255, 127, 14),    // 橙色
        Color(44, 160, 44),     // 绿色
        Color(214, 39, 40),     // 红色
        Color(148, 103, 189),   // 紫色
        Color(140, 86, 75),     // 棕色
        Color(227, 119, 194),   // 粉红色
        Color(127, 127, 127),   // 灰色
        Color(188, 143, 143),   // 褐色
        Color(23, 190, 207)     // 青色
    )

    // 滚动和缩放参数
    private var xAxisMin = 0.0
    private var xAxisMax = 200.0
    private var yAxisMin = 0.0
    private var yAxisMax = 100.0
    private var autoScroll = true  // 自动滚动标志
    private var windowSize = 200
    private val lineWidth = 2.0f
    private val markerSize = 6

    // 视口变化回调（供X轴滚动条同步）
    var onViewportChanged: (() -> Unit)? = null

    init {
        // 设置最小尺寸以支持窗口自适应
        minimumSize = Dimension(400, 300)
        // 使用配置默认窗口大小
        windowSize = dataManager.config.windowSize
        xAxisMax = windowSize.toDouble()
        initChart()
        addMouseWheelListener(createMouseWheelListener())
        addMouseListener(createMouseListener())
        addMouseListener(createClickListener())
    }

    /**
     * 根据数据点数量更新面板宽度，支持X轴滚动
     */
    private fun updatePanelSize() {
        // 由X轴滚动条控制窗口，不再通过面板宽度滚动
    }

    /**
     * 初始化图表
     */
    private fun initChart() {
        chart = XYChartBuilder()
            .width(800)
            .height(400)
            .build()

        chart?.let { c ->
            c.styler.apply {
                legendFont = java.awt.Font("Arial", java.awt.Font.PLAIN, 10)
                isPlotGridLinesVisible = true
                isLegendVisible = false  // 禁用内置图例，使用自定义图例
                markerSize = this@SerialPlotterPanel.markerSize
            }
        }
    }

    /**
     * 创建鼠标滚动监听器（用于缩放）
     */
    private fun createMouseWheelListener(): MouseWheelListener {
        return object : MouseWheelListener {
            override fun mouseWheelMoved(e: MouseWheelEvent) {
                // Trackpad uses preciseWheelRotation (double)
                val delta = e.preciseWheelRotation
                if (delta == 0.0) return

                // Negative delta = scroll up (zoom in), positive = zoom out
                val zoomFactor = (1.0 + (delta * 0.12)).coerceIn(0.2, 5.0)

                val rangeX = (xAxisMax - xAxisMin).coerceAtLeast(1.0)
                val centerX = xAxisMin + rangeX / 2
                val newRangeX = (rangeX * zoomFactor).coerceAtLeast(10.0)

                xAxisMin = centerX - newRangeX / 2
                xAxisMax = centerX + newRangeX / 2

                // 缩放时同步窗口大小
                windowSize = newRangeX.toInt().coerceAtLeast(10)

                autoScroll = false
                updateChart()
            }
        }
    }

    /**
     * 创建鼠标监听器
     */
    private fun createMouseListener(): MouseAdapter {
        return object : MouseAdapter() {
            private var lastX = 0

            override fun mouseDragged(e: MouseEvent?) {
                if (e != null) {
                    val deltaX = e.x - lastX
                    val rangeX = xAxisMax - xAxisMin
                    val shift = -deltaX * rangeX / (width / 100)

                    xAxisMin += shift
                    xAxisMax += shift

                    autoScroll = false
                    updateChart()
                }
                lastX = e?.x ?: 0
            }

            override fun mousePressed(e: MouseEvent?) {
                lastX = e?.x ?: 0
            }
        }
    }

    /**
     * 创建鼠标点击监听器（用于显示数据详情）
     */
    private fun createClickListener(): MouseAdapter {
        return object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e == null) return
                showDataPopup(e.x, e.y)
            }
        }
    }

    /**
     * 在鼠标点击位置显示数据详情
     */
    private fun showDataPopup(mouseX: Int, mouseY: Int) {
        val count = dataManager.getDataPointCount()
        if (count <= 0) return

        val plotWidth = width.coerceAtLeast(1)
        val plotHeight = height.coerceAtLeast(1)

        // 将鼠标X映射到数据索引
        val xRange = (xAxisMax - xAxisMin).coerceAtLeast(1.0)
        val xRatio = mouseX.toDouble() / plotWidth
        val index = (xAxisMin + xRange * xRatio).toInt().coerceIn(0, count - 1)

        val seriesNames = dataManager.getSeriesNames()
        if (seriesNames.isEmpty()) return

        val lines = mutableListOf<String>()
        for (name in seriesNames) {
            if (!isSeriesVisible(name)) continue
            val series = dataManager.getSeries(name) ?: continue
            if (index < series.size) {
                val value = series[index]
                lines.add("$name: $value")
            }
        }
        if (lines.isEmpty()) return

        val popup = JPopupMenu()
        val label = JLabel("<html>${lines.joinToString("<br/>")}</html>")
        label.border = javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)
        popup.add(label)
        popup.show(this, mouseX + 8, mouseY + 8)
    }

    /**
     * 获取数据管理器（供外部访问）
     */
    fun getDataManager(): PlotterDataManager = dataManager

    /**
     * 添加串口数据
     */
    fun addSerialData(data: String) {
        dataManager.addSerialData(data)
        updateChart()
    }

    /**
     * 更新图表显示 - 心电图效果（滚动窗口）
     */
    private fun updateChart() {
        chart?.let { c ->
            // 清除旧的数据系列
            val seriesToRemove = mutableListOf<String>()
            c.getSeriesMap().keys.forEach { seriesName ->
                seriesToRemove.add(seriesName)
            }
            seriesToRemove.forEach { seriesName ->
                c.removeSeries(seriesName)
            }

            val timePoints = dataManager.getTimePoints()
            val allSeriesNames = dataManager.getSeriesNames()

            if (timePoints.isEmpty() || allSeriesNames.isEmpty()) {
                return
            }

            val count = timePoints.size

            // 自动滚动：显示最后 windowSize 个数据点（窗口效果）
            if (autoScroll) {
                if (count <= windowSize) {
                    // 数据少于窗口大小，显示从0开始
                    xAxisMin = 0.0
                    xAxisMax = windowSize.toDouble()
                } else {
                    // 数据超过窗口大小，显示最后windowSize个数据点
                    xAxisMin = (count - windowSize).toDouble()
                    xAxisMax = count.toDouble()
                }
            } else {
                // 手动滚动模式：限制边界
                if (xAxisMin < 0) xAxisMin = 0.0
                if (xAxisMax < xAxisMin + 1) xAxisMax = xAxisMin + 1
                if (xAxisMax > count) {
                    val shift = xAxisMax - count
                    xAxisMin = (xAxisMin - shift).coerceAtLeast(0.0)
                    xAxisMax = count.toDouble()
                }
            }

            // 添加可见的数据系列到图表
            for ((index, seriesName) in allSeriesNames.withIndex()) {
                if (!isSeriesVisible(seriesName)) continue
                val values = dataManager.getSeries(seriesName) ?: continue
                val xData = (0 until values.size).map { it.toDouble() }
                val color = colors[index % colors.size]
                c.addSeries(seriesName, xData, values).apply {
                    lineWidth = this@SerialPlotterPanel.lineWidth
                    lineColor = color
                    marker = SeriesMarkers.CIRCLE
                    markerColor = color
                }
            }

            // 自动调整Y轴范围
            val allValues = allSeriesNames
                .mapNotNull { dataManager.getSeries(it) }
                .flatten()

            if (allValues.isNotEmpty()) {
                val minValue = allValues.minOrNull() ?: 0.0
                val maxValue = allValues.maxOrNull() ?: 100.0
                val margin = (maxValue - minValue) * 0.1

                yAxisMin = minValue - margin
                yAxisMax = maxValue + margin
            }

            // 设置坐标轴范围
            chart?.styler?.apply {
                this.xAxisMin = this@SerialPlotterPanel.xAxisMin
                this.xAxisMax = this@SerialPlotterPanel.xAxisMax
                this.yAxisMin = this@SerialPlotterPanel.yAxisMin
                this.yAxisMax = this@SerialPlotterPanel.yAxisMax
            }

            repaint()
            onViewportChanged?.invoke()
        }
    }

    /**
     * 获取所有数据系列名称
     */
    fun getSeriesNames(): List<String> = dataManager.getSeriesNames()

    /**
     * 设置数据系列的可见性
     */
    fun setSeriesVisible(seriesName: String, visible: Boolean) {
        if (visible) {
            hiddenSeries.remove(seriesName)
        } else {
            hiddenSeries.add(seriesName)
        }
        updateChart()
    }

    /**
     * 获取数据系列的可见性
     */
    fun isSeriesVisible(seriesName: String): Boolean {
        return !hiddenSeries.contains(seriesName)
    }

    /**
     * 设置X轴窗口范围（用于滚动条控制）
     */
    fun setXAxisWindow(min: Double, max: Double) {
        autoScroll = false
        xAxisMin = min
        xAxisMax = max
        windowSize = (max - min).toInt().coerceAtLeast(10)
        updateChart()
    }

    /**
     * 设置是否自动滚动
     */
    fun setAutoScrollEnabled(enabled: Boolean) {
        autoScroll = enabled
        updateChart()
    }

    fun isAutoScrollEnabled(): Boolean = autoScroll

    /**
     * 设置窗口大小
     */
    fun setWindowSize(size: Int) {
        windowSize = size.coerceAtLeast(10)
    }

    /**
     * 获取当前X轴范围
     */
    fun getXAxisRange(): Pair<Double, Double> = Pair(xAxisMin, xAxisMax)

    /**
     * 获取当前窗口大小（X轴可视范围）
     */
    fun getWindowSize(): Int = windowSize

    /**
     * 获取数据点数量
     */
    fun getDataPointCount(): Int = dataManager.getDataPointCount()

    /**
     * 清除所有数据
     */
    fun clearData() {
        dataManager.clear()
        hiddenSeries.clear()
        autoScroll = true
        xAxisMin = 0.0
        xAxisMax = windowSize.toDouble()
        chart?.let { c ->
            val seriesToRemove = mutableListOf<String>()
            c.getSeriesMap().keys.forEach { seriesName ->
                seriesToRemove.add(seriesName)
            }
            seriesToRemove.forEach { seriesName ->
                c.removeSeries(seriesName)
            }
        }
        repaint()
    }

    /**
     * 重置视图
     */
    fun resetView() {
        autoScroll = true
        xAxisMin = 0.0
        xAxisMax = windowSize.toDouble()
        updateChart()
    }

    /**
     * 更新解析配置
     */
    fun updateConfig(newConfig: com.serialmonitor.plotter.data.PlotterConfig) {
        dataManager.updateConfig(newConfig)
        windowSize = newConfig.windowSize.coerceAtLeast(10)
        if (autoScroll) {
            xAxisMin = 0.0
            xAxisMax = windowSize.toDouble()
        }
        updateChart()
    }

    /**
     * 获取图表对象
     */
    fun getChart(): XYChart? = chart

    override fun paintComponent(g: java.awt.Graphics?) {
        super.paintComponent(g)

        if (g == null) return

        chart?.let { c ->
            try {
                val g2d = g as? java.awt.Graphics2D ?: return@let
                c.paint(g2d, width, height)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
