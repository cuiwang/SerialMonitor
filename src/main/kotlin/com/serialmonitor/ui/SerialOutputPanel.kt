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

    // 过滤功能相关
    private val allReceivedData = StringBuilder()  // 保存所有原始数据
    private var filterText = ""  // 当前过滤文本
    private var isRegexFilter = false  // 是否使用正则表达式过滤
    private var liveFilter = false  // 是否启用实时过滤

    init {
        layout = BorderLayout()
        add(createFilterPanel(), BorderLayout.NORTH)  // 过滤面板在顶部
        add(scrollPane, BorderLayout.CENTER)
        add(createBottomPanel(), BorderLayout.SOUTH)
    }

    /**
     * 创建过滤面板
     */
    private fun createFilterPanel(): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT, 5, 5)).apply {
            background = JBColor.background()

            add(JLabel("Filter:"))

            // 过滤输入框
            val filterField = JTextField(30).apply {
                toolTipText = "Enter text to filter log output"
            }
            add(filterField)

            // 过滤模式选择
            val filterModeCombo = JComboBox(arrayOf("Normal", "Regex")).apply {
                toolTipText = "Select filter mode: Normal (text search) or Regex (regular expression)"
                addActionListener {
                    isRegexFilter = selectedIndex == 1
                    if (liveFilter && filterField.text.isNotEmpty()) {
                        applyFilter(filterField.text)
                    }
                }
            }
            add(filterModeCombo)

            // 应用过滤按钮
            val applyButton = JButton("Apply").apply {
                icon = AllIcons.Actions.Find
                toolTipText = "Apply filter to log output"
                addActionListener {
                    filterText = filterField.text
                    applyFilter(filterText)
                }
            }
            add(applyButton)

            // 清除过滤按钮
            val clearFilterButton = JButton("Clear").apply {
                icon = AllIcons.Actions.Close
                toolTipText = "Clear filter and show all logs"
                addActionListener {
                    filterField.text = ""
                    filterText = ""
                    applyFilter("")
                }
            }
            add(clearFilterButton)

            // 实时过滤复选框
            val liveFilterCheckbox = JCheckBox("Live", false).apply {
                toolTipText = "Apply filter as you type"
                addActionListener {
                    liveFilter = isSelected
                }
            }
            add(liveFilterCheckbox)

            // 为实时过滤添加文档监听器
            filterField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) {
                    if (liveFilter) {
                        filterText = filterField.text
                        applyFilter(filterText)
                    }
                }
                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) {
                    if (liveFilter) {
                        filterText = filterField.text
                        applyFilter(filterText)
                    }
                }
                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) {}
            })

            // Enter键快速应用过滤
            filterField.addActionListener {
                filterText = filterField.text
                applyFilter(filterText)
            }
        }
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
        allReceivedData.clear()
    }

    /**
     * 应用过滤器到日志输出
     */
    private fun applyFilter(filter: String) {
        SwingUtilities.invokeLater {
            if (filter.isEmpty()) {
                // 无过滤，显示所有数据
                outputArea.text = allReceivedData.toString()
            } else {
                // 应用过滤
                val filteredText = filterLines(allReceivedData.toString(), filter)
                outputArea.text = filteredText
            }

            // 保持自动滚动
            if (autoScroll) {
                outputArea.caretPosition = outputArea.document.length
            }
        }
    }

    /**
     * 根据过滤条件筛选行
     */
    private fun filterLines(text: String, filter: String): String {
        if (filter.isEmpty()) return text

        return try {
            val lines = text.split("\n")
            val filtered = if (isRegexFilter) {
                // 正则表达式过滤
                val regex = Regex(filter)
                lines.filter { line -> regex.containsMatchIn(line) }
            } else {
                // 普通文本过滤（不区分大小写）
                lines.filter { line -> line.contains(filter, ignoreCase = true) }
            }
            filtered.joinToString("\n")
        } catch (e: Exception) {
            // 正则表达式错误时，返回错误提示
            "ERROR: Invalid filter pattern - ${e.message}\n"
        }
    }

    override fun onDataReceived(data: String) {
        SwingUtilities.invokeLater {
            // 保存到原始数据缓存
            allReceivedData.append(data)

            // 如果有过滤条件，应用过滤
            if (filterText.isNotEmpty()) {
                // 重新应用过滤到所有数据
                applyFilter(filterText)
            } else {
                // 无过滤，直接追加显示
                outputArea.append(data)
                if (autoScroll) {
                    outputArea.caretPosition = outputArea.document.length
                }
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

            // 保存到原始数据缓存
            allReceivedData.append(message)

            // 如果有过滤条件，应用过滤
            if (filterText.isNotEmpty()) {
                applyFilter(filterText)
            } else {
                outputArea.append(message)
                if (autoScroll) {
                    outputArea.caretPosition = outputArea.document.length
                }
            }
        }
    }

    override fun onError(errorMessage: String) {
        SwingUtilities.invokeLater {
            val message = "ERROR: $errorMessage\n"

            // 保存到原始数据缓存
            allReceivedData.append(message)

            // 如果有过滤条件，应用过滤
            if (filterText.isNotEmpty()) {
                applyFilter(filterText)
            } else {
                outputArea.append(message)
                if (autoScroll) {
                    outputArea.caretPosition = outputArea.document.length
                }
            }
        }
    }

    fun getText(): String = outputArea.text
}





