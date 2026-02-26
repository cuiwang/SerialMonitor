package com.serialmonitor.ui

import com.serialmonitor.data.SerialDataListener
import com.serialmonitor.data.SerialPortState
import com.serialmonitor.serial.SerialPortManager
import com.serialmonitor.plotter.ui.SerialPlotterMainPanel
import java.awt.*
import javax.swing.*

/**
 * 串口监视器主面板
 * 包含Monitor（监视）和Plotter（绘图仪）两个视图
 */
class SerialMonitorPanel : JPanel(), SerialDataListener {

    private val portManager = SerialPortManager()
    private val controlPanel = SerialControlPanel(portManager)
    private val outputPanel = SerialOutputPanel(portManager)  // 传递 portManager，显示发送面板
    private val plotterPanel = SerialPlotterMainPanel(portManager)  // 传递 portManager

    // 选项卡面板
    private val tabbedPane = JTabbedPane()

    init {
        layout = BorderLayout()

        // 增加整体窗口高度以便更好地显示曲线图
        preferredSize = Dimension(1000, 1000)  // 从 800 增加到 1000
        minimumSize = Dimension(800, 700)      // 从 500 增加到 700

        // 添加控制面板在顶部
        add(controlPanel, BorderLayout.NORTH)

        // 设置选项卡
        tabbedPane.addTab("Monitor", outputPanel)
        tabbedPane.addTab("Plotter", plotterPanel)

        // 监听选项卡切换，在 Plotter 选项卡中隐藏 controlPanel
        tabbedPane.addChangeListener { e ->
            val selectedIndex = tabbedPane.selectedIndex
            if (selectedIndex == 0) {
                // Monitor 选项卡：显示 controlPanel
                if (controlPanel.parent == null) {
                    add(controlPanel, BorderLayout.NORTH)
                }
                controlPanel.isVisible = true
            } else if (selectedIndex == 1) {
                // Plotter 选项卡：隐藏 controlPanel
                controlPanel.isVisible = false
            }
            revalidate()
            repaint()
        }

        add(tabbedPane, BorderLayout.CENTER)

        // 为portManager添加监听器
        portManager.addListener(this)
        portManager.addListener(outputPanel)
        portManager.addListener(plotterPanel)  // 绘图仪也需要接收数据

        // 为controlPanel添加连接状态变化监听
        portManager.addListener(object : SerialDataListener {
            override fun onDataReceived(data: String) {}
            override fun onConnectionStatusChanged(state: SerialPortState) {
                controlPanel.onConnectionStatusChanged(state)
            }
            override fun onError(errorMessage: String) {}
        })
    }

    override fun onDataReceived(data: String) {
        // 可以在这里添加其他处理逻辑
    }

    override fun onConnectionStatusChanged(state: SerialPortState) {
        // 状态变化处理
    }

    override fun onError(errorMessage: String) {
        // 错误处理
    }
}


