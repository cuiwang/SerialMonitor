package com.serialmonitor.plotter.ui

import com.serialmonitor.plotter.data.PlotterConfig
import javax.swing.*
import java.awt.*

/**
 * 串口绘图仪设置对话框
 */
class PlotterSettingsDialog(
    parent: Frame?,
    private val currentConfig: PlotterConfig,
    private val onApply: (PlotterConfig) -> Unit
) : JDialog(parent, "Plotter Settings", true) {

    private val itemSeparatorField = JTextField(currentConfig.itemSeparator, 10)
    private val nameSeparatorField = JTextField(currentConfig.nameSeparator, 10)
    private val windowSizeField = JTextField(currentConfig.windowSize.toString(), 10)
    private val maxCacheField = JTextField(currentConfig.maxCache.toString(), 10)

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        setupUI()
        setSize(400, 250)
        setLocationRelativeTo(parent)
    }

    private fun setupUI() {
        val contentPane = contentPane
        contentPane.layout = BorderLayout(10, 10)

        // 设置面板
        val settingsPanel = JPanel().apply {
            layout = GridLayout(5, 2, 10, 10)
            border = BorderFactory.createEmptyBorder(15, 15, 15, 15)

            // 数据项分隔符
            add(JLabel("Item Separator:"))
            add(itemSeparatorField)

            // 名称-值分隔符
            add(JLabel("Name-Value Separator:"))
            add(nameSeparatorField)

            // 窗口大小
            add(JLabel("Window Size:"))
            add(windowSizeField)

            // 最大缓存
            add(JLabel("Max Cache:"))
            add(maxCacheField)

            // 说明信息
            add(JLabel("Example:"))
            val exampleLabel = JLabel("<html>" +
                "Data: name1${itemSeparatorField.text}value1${nameSeparatorField.text}1<br/>" +
                "Data: name2${itemSeparatorField.text}value2${nameSeparatorField.text}2" +
                "</html>")
            exampleLabel.font = Font("Monospace", Font.PLAIN, 11)
            add(exampleLabel)
        }

        contentPane.add(settingsPanel, BorderLayout.CENTER)

        // 按钮面板
        val buttonPanel = JPanel().apply {
            layout = FlowLayout(FlowLayout.RIGHT, 10, 10)

            val applyButton = JButton("Apply").apply {
                addActionListener { applySettings() }
            }
            val cancelButton = JButton("Cancel").apply {
                addActionListener { dispose() }
            }
            val resetButton = JButton("Reset").apply {
                addActionListener { resetToDefault() }
            }

            add(resetButton)
            add(applyButton)
            add(cancelButton)
        }

        contentPane.add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun applySettings() {
        val itemSep = itemSeparatorField.text
        val nameSep = nameSeparatorField.text
        val windowSize = windowSizeField.text.toIntOrNull()
        val maxCache = maxCacheField.text.toIntOrNull()

        // 验证
        if (itemSep.isEmpty() || nameSep.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Separators cannot be empty!",
                "Invalid Configuration",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        if (itemSep == nameSep) {
            JOptionPane.showMessageDialog(
                this,
                "Item separator and name separator must be different!",
                "Invalid Configuration",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        if (windowSize == null || windowSize < 10) {
            JOptionPane.showMessageDialog(
                this,
                "Window size must be >= 10!",
                "Invalid Configuration",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        if (maxCache == null || maxCache < 100) {
            JOptionPane.showMessageDialog(
                this,
                "Max cache must be >= 100!",
                "Invalid Configuration",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val newConfig = PlotterConfig(itemSep, nameSep, windowSize, maxCache)
        onApply(newConfig)
        dispose()
    }

    private fun resetToDefault() {
        itemSeparatorField.text = ","
        nameSeparatorField.text = ":"
        windowSizeField.text = "20"
        maxCacheField.text = "2000"
    }
}
