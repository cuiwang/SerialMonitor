package com.serialmonitor.plotter.ui

import com.serialmonitor.data.SerialDataListener
import com.serialmonitor.data.SerialPortState
import com.serialmonitor.serial.SerialPortManager
import com.intellij.icons.AllIcons
import javax.swing.*
import java.awt.*

/**
 * 串口绘图仪主面板
 * 集成绘图面板、图例和工具栏
 */
class SerialPlotterMainPanel(private val portManager: SerialPortManager? = null) : JPanel(), SerialDataListener {

    private val plotterPanel = SerialPlotterPanel()
    private val legendPanel = PlotterLegendPanel(plotterPanel)

    private var lastDataUpdateTime = 0L
    private val updateInterval = 100  // 100ms更新一次UI

    // X轴滚动条
    private val xScrollBar = JScrollBar(JScrollBar.HORIZONTAL)

    // 暂停按钮引用，用于更新状态
    private var pauseButton: JButton? = null

    // 防止程序设置滚动条时触发用户操作监听器
    private var isUpdatingScrollBarProgrammatically = false

    init {
        layout = BorderLayout(0, 0)  // 移除间距，自己控制

        // 设置绘图面板的最小尺寸，确保曲线图有足够的显示空间
        preferredSize = Dimension(1000, 1200)  // 增加到 1200 (从 700)
        minimumSize = Dimension(800, 700)      // 最小增加到 700

        // 创建工具栏
        val toolBar = createToolBar()

        // 顶部面板：工具栏 + 图例
        val topPanel = JPanel(BorderLayout(0, 3)).apply {  // 设置 3px 垂直间距
            border = BorderFactory.createEmptyBorder(0, 0, 4, 0)  // 下边距 4px
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
            // 忽略正在调整的事件和程序设置的事件
            if (e.valueIsAdjusting || isUpdatingScrollBarProgrammatically) {
                return@addAdjustmentListener
            }

            val window = plotterPanel.getWindowSize()
            val start = e.value
            val count = plotterPanel.getDataPointCount()
            val max = maxOf(0, count - window)

            // 智能判断：如果用户滚动到最右端（±1容差），自动启用跟随模式
            if (max > 0 && start >= max - 1) {
                plotterPanel.setAutoScrollEnabled(true)
            } else {
                // 用户在其他位置查看历史数据，禁用自动跟随
                plotterPanel.setAutoScrollEnabled(false)
            }

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

        // 使用标志位防止触发 AdjustmentListener
        isUpdatingScrollBarProgrammatically = true
        xScrollBar.setValues(value, window, 0, max + window)
        isUpdatingScrollBarProgrammatically = false
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

            // 暂停/恢复按钮 (仅当有 portManager 时显示)
            if (portManager != null) {
                pauseButton = JButton("Pause").apply {
                    icon = AllIcons.Actions.Pause
                    isEnabled = false  // 默认禁用，连接后启用
                    addActionListener {
                        if (portManager.isPaused()) {
                            portManager.resume()
                            text = "Pause"
                            icon = AllIcons.Actions.Pause
                        } else {
                            portManager.pause()
                            text = "Resume"
                            icon = AllIcons.Actions.Resume
                        }
                    }
                }
                add(pauseButton!!)
                addSeparator()
            }

            // 重置视图按钮
            add(JButton("Reset").apply {
                addActionListener {
                    plotterPanel.resetView()
                    plotterPanel.setAutoScrollEnabled(true)  // 确保启用自动滚动
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
                // 启用暂停按钮
                pauseButton?.isEnabled = true
                pauseButton?.text = "Pause"
                pauseButton?.icon = AllIcons.Actions.Pause
            }
            SerialPortState.DISCONNECTED -> {
                // 断开连接时禁用暂停按钮并恢复状态
                pauseButton?.isEnabled = false
                pauseButton?.text = "Pause"
                pauseButton?.icon = AllIcons.Actions.Pause
                portManager?.resume()  // 确保恢复状态
            }
            SerialPortState.CONNECTING -> {
                // 连接中时不做特殊处理
            }
            SerialPortState.ERROR -> {
                // 错误时禁用暂停按钮
                pauseButton?.isEnabled = false
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
