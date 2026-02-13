package com.serialmonitor.plotter.ui

import com.serialmonitor.data.SerialDataListener
import com.serialmonitor.data.SerialPortState
import javax.swing.*
import java.awt.*

/**
 * 串口绘图仪主面板
 * 集成绘图面板、图例和工具栏
 */
class SerialPlotterMainPanel : JPanel(), SerialDataListener {

    private val plotterPanel = SerialPlotterPanel()
    private val legendPanel = PlotterLegendPanel(plotterPanel)

    private var lastDataUpdateTime = 0L
    private val updateInterval = 100  // 100ms更新一次UI

    // X轴滚动条
    private val xScrollBar = JScrollBar(JScrollBar.HORIZONTAL)

    init {
        layout = BorderLayout()

        // 创建工具栏
        val toolBar = createToolBar()

        // 顶部面板：工具栏 + 图例
        val topPanel = JPanel(BorderLayout()).apply {
            // 工具栏在最上方
            add(toolBar, BorderLayout.NORTH)
            // 图例在工具栏下方
            add(legendPanel, BorderLayout.CENTER)
        }
        add(topPanel, BorderLayout.NORTH)

        // 中部面板：折线图 + X轴滚动条
        val centerPanel = JPanel(BorderLayout()).apply {
            add(plotterPanel, BorderLayout.CENTER)
            add(xScrollBar, BorderLayout.SOUTH)
        }
        add(centerPanel, BorderLayout.CENTER)

        setupScrollBar()
        plotterPanel.onViewportChanged = { updateScrollBar() }
    }

    private fun setupScrollBar() {
        xScrollBar.minimum = 0
        xScrollBar.maximum = 0
        xScrollBar.visibleAmount = plotterPanel.getWindowSize()
        xScrollBar.unitIncrement = 1
        xScrollBar.blockIncrement = 10
        xScrollBar.preferredSize = Dimension(0, 16)
        xScrollBar.minimumSize = Dimension(0, 16)
        
        // 强制滚动条始终可见，不透明、不隐藏
        xScrollBar.isOpaque = true
        xScrollBar.putClientProperty("JScrollBar.showButtons", true)
        
        xScrollBar.addAdjustmentListener { e ->
            if (e.valueIsAdjusting) return@addAdjustmentListener
            val window = plotterPanel.getWindowSize()
            val start = e.value
            plotterPanel.setAutoScrollEnabled(false)
            plotterPanel.setXAxisWindow(start.toDouble(), (start + window).toDouble())
        }
    }

    private fun updateScrollBar() {
        val count = plotterPanel.getDataPointCount()
        val (xMin, xMax) = plotterPanel.getXAxisRange()
        val window = maxOf(1, (xMax - xMin).toInt())
        val max = maxOf(0, count - window)

        xScrollBar.isVisible = count > window
        xScrollBar.minimum = 0
        xScrollBar.maximum = max + window
        xScrollBar.visibleAmount = window

        val value = if (plotterPanel.isAutoScrollEnabled()) {
            max
        } else {
            xScrollBar.value.coerceIn(0, max)
        }
        xScrollBar.setValues(value, window, 0, max + window)
    }

    /**
     * 创建工具栏
     */
    private fun createToolBar(): JToolBar {
        return JToolBar().apply {
            isFloatable = false

            // 清除数据按钮
            add(JButton("Clear").apply {
                addActionListener {
                    plotterPanel.clearData()
                    legendPanel.clearLegend()
                    updateScrollBar()
                }
            })

            addSeparator()

            // 重置视图按钮
            add(JButton("Reset").apply {
                addActionListener {
                    plotterPanel.resetView()
                    updateScrollBar()
                }
            })

            addSeparator()

            // 设置按钮
            add(JButton("Settings").apply {
                addActionListener {
                    showSettingsDialog()
                }
            })
        }
    }

    /**
     * 显示设置对话框
     */
    private fun showSettingsDialog() {
        val currentConfig = plotterPanel.getDataManager().config
        val dialog = PlotterSettingsDialog(
            SwingUtilities.getWindowAncestor(this) as? Frame,
            currentConfig
        ) { newConfig ->
            plotterPanel.updateConfig(newConfig)
        }
        dialog.isVisible = true
    }

    /**
     * 接收串口数据
     */
    override fun onDataReceived(data: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDataUpdateTime > updateInterval) {
            plotterPanel.addSerialData(data)
            legendPanel.updateLegend()
            updateScrollBar()
            lastDataUpdateTime = currentTime
        }
    }

    /**
     * 连接状态改变
     */
    override fun onConnectionStatusChanged(state: SerialPortState) {
        when (state) {
            SerialPortState.CONNECTED -> {
                // 连接时清除旧数据
                plotterPanel.clearData()
                legendPanel.clearLegend()
            }
            SerialPortState.DISCONNECTED -> {
                // 断开连接时不做特殊处理
            }
            SerialPortState.CONNECTING -> {
                // 连接中时不做特殊处理
            }
            SerialPortState.ERROR -> {
                // 错误时显示提示
            }
        }
    }

    /**
     * 错误回调
     */
    override fun onError(errorMessage: String) {
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(
                this,
                "Serial Plotter Error: $errorMessage",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
}
