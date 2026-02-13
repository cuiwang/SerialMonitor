package com.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.serialmonitor.data.SerialDataListener
import com.serialmonitor.data.SerialPortState
import java.awt.*
import java.awt.datatransfer.StringSelection
import javax.swing.*

/**
 * 串口输出面板 - 显示接收到的数据
 */
class SerialOutputPanel : JPanel(), SerialDataListener {

    private val outputArea = JTextArea().apply {
        isEditable = false
        font = Font("Courier New", Font.PLAIN, 12)
        background = JBColor.background()
        foreground = JBColor.foreground()
        lineWrap = false
    }

    private val scrollPane = JBScrollPane(outputArea).apply {
        horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
    }

    private var autoScroll = true

    init {
        layout = BorderLayout()
        add(scrollPane, BorderLayout.CENTER)
        add(createBottomPanel(), BorderLayout.SOUTH)
    }

    private fun createBottomPanel(): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            background = JBColor.background()

            // 自动滚动复选框
            val autoScrollCheckbox = JCheckBox("Auto Scroll", true).apply {
                addActionListener {
                    autoScroll = isSelected
                }
            }
            add(autoScrollCheckbox)

            add(JLabel(" | "))

            // 清空按钮
            val clearButton = JButton("Clear").apply {
                icon = AllIcons.Actions.GC
                addActionListener {
                    clearOutput()
                }
            }
            add(clearButton)

            // 复制按钮
            val copyButton = JButton("Copy All").apply {
                icon = AllIcons.Actions.Copy
                addActionListener {
                    val toolkit = Toolkit.getDefaultToolkit()
                    val clipboard = toolkit.systemClipboard
                    val selection = StringSelection(outputArea.text)
                    clipboard.setContents(selection, null)
                    JOptionPane.showMessageDialog(this@SerialOutputPanel, "Copied to clipboard")
                }
            }
            add(copyButton)
        }
    }

    fun clearOutput() {
        outputArea.text = ""
    }

    override fun onDataReceived(data: String) {
        SwingUtilities.invokeLater {
            outputArea.append(data)
            if (autoScroll) {
                outputArea.caretPosition = outputArea.document.length
            }
        }
    }

    override fun onConnectionStatusChanged(state: SerialPortState) {
        SwingUtilities.invokeLater {
            val message = when (state) {
                SerialPortState.CONNECTED -> "=== Serial Port Connected ===\n"
                SerialPortState.DISCONNECTED -> "=== Serial Port Disconnected ===\n"
                SerialPortState.CONNECTING -> "=== Connecting... ===\n"
                SerialPortState.ERROR -> "=== Error ===\n"
            }
            outputArea.append(message)
            if (autoScroll) {
                outputArea.caretPosition = outputArea.document.length
            }
        }
    }

    override fun onError(errorMessage: String) {
        SwingUtilities.invokeLater {
            outputArea.append("ERROR: $errorMessage\n")
            if (autoScroll) {
                outputArea.caretPosition = outputArea.document.length
            }
        }
    }

    fun getText(): String = outputArea.text
}





