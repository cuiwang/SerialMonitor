package com.serialmonitor.ui

import com.serialmonitor.data.SerialDataListener
import com.serialmonitor.data.SerialPortState
import com.serialmonitor.serial.SerialPortManager
import java.awt.*
import javax.swing.*

/**
 * 串口监视器主面板
 */
class SerialMonitorPanel : JPanel(), SerialDataListener {

    private val portManager = SerialPortManager()
    private val controlPanel = SerialControlPanel(portManager)
    private val outputPanel = SerialOutputPanel()

    init {
        layout = BorderLayout()

        // 添加控制面板
        add(controlPanel, BorderLayout.NORTH)

        // 添加输出面板
        add(outputPanel, BorderLayout.CENTER)

        // 为portManager添加监听器
        portManager.addListener(this)
        portManager.addListener(outputPanel)

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

