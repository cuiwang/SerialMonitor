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
    private val outputPanel = SerialOutputPanel()
    private val plotterPanel = SerialPlotterMainPanel()

    // 选项卡面板
    private val tabbedPane = JTabbedPane()

    init {
        layout = BorderLayout()

        // 添加控制面板在顶部
        add(controlPanel, BorderLayout.NORTH)

        // 设置选项卡
        tabbedPane.addTab("Monitor", outputPanel)
        tabbedPane.addTab("Plotter", plotterPanel)

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


