package com.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.serialmonitor.data.SerialPortState
import com.serialmonitor.serial.PortDetector
import com.serialmonitor.serial.SerialPortManager
import java.awt.*
import javax.swing.*

/**
 * 串口控制面板 - 端口选择、波特率配置等
 */
class SerialControlPanel(private val portManager: SerialPortManager) : JPanel() {

    private val portComboBox = JComboBox<String>()
    private val baudRateComboBox = JComboBox<Int>()
    private val connectButton = JButton("Connect").apply {
        icon = AllIcons.Actions.Execute
    }
    private val refreshButton = JButton("Refresh").apply {
        icon = AllIcons.Actions.Refresh
    }
    private val sendPanel = JPanel()

    private var isConnected = false

    private val baudRates = arrayOf(9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600)

    init {
        layout = GridBagLayout()
        background = JBColor.background()

        // 端口标签
        add(JLabel("Port:"), GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            insets = Insets(5, 5, 5, 5)
        })

        // 端口选择框
        portComboBox.isEditable = true
        add(portComboBox, GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            gridwidth = 1
            insets = Insets(5, 5, 5, 5)
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        })

        // 波特率标签
        add(JLabel("Baud Rate:"), GridBagConstraints().apply {
            gridx = 2
            gridy = 0
            insets = Insets(5, 5, 5, 5)
        })

        // 波特率选择框
        for (rate in baudRates) {
            baudRateComboBox.addItem(rate)
        }
        baudRateComboBox.selectedItem = 115200
        add(baudRateComboBox, GridBagConstraints().apply {
            gridx = 3
            gridy = 0
            insets = Insets(5, 5, 5, 5)
        })

        // 刷新按钮
        refreshButton.addActionListener {
            refreshPorts()
        }
        add(refreshButton, GridBagConstraints().apply {
            gridx = 4
            gridy = 0
            insets = Insets(5, 5, 5, 5)
        })

        // 连接按钮
        connectButton.addActionListener {
            if (isConnected) {
                disconnect()
            } else {
                connect()
            }
        }
        add(connectButton, GridBagConstraints().apply {
            gridx = 5
            gridy = 0
            insets = Insets(5, 5, 5, 5)
        })

        // 发送面板
        add(createSendPanel(), GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 6
            insets = Insets(5, 5, 5, 5)
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        })

        // 刷新端口列表
        refreshPorts()
    }

    private fun createSendPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = JBColor.background()
        }

        val sendInput = JTextField().apply {
            font = Font("Courier New", Font.PLAIN, 12)
        }
        panel.add(sendInput, BorderLayout.CENTER)

        val sendButton = JButton("Send").apply {
            icon = AllIcons.Actions.Forward
            addActionListener {
                val text = sendInput.text
                if (text.isNotEmpty()) {
                    portManager.sendDataWithNewline(text)
                    sendInput.text = ""
                }
            }
        }
        panel.add(sendButton, BorderLayout.EAST)

        // 添加回车快捷键
        sendInput.addKeyListener(object : java.awt.event.KeyListener {
            override fun keyTyped(e: java.awt.event.KeyEvent) {}
            override fun keyPressed(e: java.awt.event.KeyEvent) {
                if (e.keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                    val text = sendInput.text
                    if (text.isNotEmpty()) {
                        portManager.sendDataWithNewline(text)
                        sendInput.text = ""
                    }
                }
            }
            override fun keyReleased(e: java.awt.event.KeyEvent) {}
        })

        return panel
    }

    fun refreshPorts() {
        val selectedPort = portComboBox.selectedItem as? String
        portComboBox.removeAllItems()

        val ports = PortDetector.getAvailablePorts()
        ports.forEach { portComboBox.addItem(it) }

        // 恢复之前的选择，或者选择最后一个
        if (selectedPort != null && ports.contains(selectedPort)) {
            portComboBox.selectedItem = selectedPort
        } else if (ports.isNotEmpty()) {
            portComboBox.selectedIndex = ports.size - 1
        }
    }

    private fun connect() {
        val port = portComboBox.selectedItem as? String ?: return
        val baudRate = baudRateComboBox.selectedItem as Int

        if (portManager.connect(port, baudRate)) {
            isConnected = true
            connectButton.text = "Disconnect"
            connectButton.icon = AllIcons.Actions.Pause
            portComboBox.isEnabled = false
            baudRateComboBox.isEnabled = false
            refreshButton.isEnabled = false
        } else {
            JOptionPane.showMessageDialog(this, "Failed to connect to $port", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    private fun disconnect() {
        portManager.disconnect()
        isConnected = false
        connectButton.text = "Connect"
        connectButton.icon = AllIcons.Actions.Execute
        portComboBox.isEnabled = true
        baudRateComboBox.isEnabled = true
        refreshButton.isEnabled = true
    }

    fun onConnectionStatusChanged(state: SerialPortState) {
        SwingUtilities.invokeLater {
            when (state) {
                SerialPortState.CONNECTED -> {
                    isConnected = true
                    connectButton.text = "Disconnect"
                    connectButton.icon = AllIcons.Actions.Pause
                    portComboBox.isEnabled = false
                    baudRateComboBox.isEnabled = false
                    refreshButton.isEnabled = false
                }
                SerialPortState.DISCONNECTED -> {
                    isConnected = false
                    connectButton.text = "Connect"
                    connectButton.icon = AllIcons.Actions.Execute
                    portComboBox.isEnabled = true
                    baudRateComboBox.isEnabled = true
                    refreshButton.isEnabled = true
                }
                SerialPortState.ERROR -> {
                    isConnected = false
                    connectButton.text = "Connect"
                    connectButton.icon = AllIcons.Actions.Execute
                    portComboBox.isEnabled = true
                    baudRateComboBox.isEnabled = true
                    refreshButton.isEnabled = true
                }
                else -> {}
            }
        }
    }
}

