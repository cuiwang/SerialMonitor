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
    private val smoothLineCheckBox = JCheckBox("Enable Smooth Line", currentConfig.smoothLine)

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        setupUI()
        setSize(450, 400)  // 增加高度以容纳新的复选框
        setLocationRelativeTo(parent)
    }

    private fun setupUI() {
        val contentPane = contentPane
        contentPane.layout = BorderLayout(10, 10)

        // 设置面板 - 使用 BoxLayout 替代 GridLayout
        val settingsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

            // 数据项分隔符
            add(createLabeledField("Item Separator:", itemSeparatorField))
            add(Box.createVerticalStrut(15))  // 间距

            // 名称-值分隔符
            add(createLabeledField("Name-Value Separator:", nameSeparatorField))
            add(Box.createVerticalStrut(15))

            // 窗口大小
            add(createLabeledField("Window Size:", windowSizeField))
            add(Box.createVerticalStrut(15))

            // 最大缓存
            add(createLabeledField("Max Cache:", maxCacheField))
            add(Box.createVerticalStrut(15))

            // 平滑曲线复选框
            add(createCheckBoxPanel("Smooth Line:", smoothLineCheckBox))
            add(Box.createVerticalStrut(20))

            // 说明信息
            add(createExamplePanel())
            add(Box.createVerticalGlue())  // 底部弹性空间
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

    /**
     * 创建带标签的输入框面板
     */
    private fun createLabeledField(labelText: String, field: JTextField): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT

            val label = JLabel(labelText).apply {
                preferredSize = Dimension(180, 25)
                minimumSize = Dimension(180, 25)
                maximumSize = Dimension(180, 25)
            }

            field.apply {
                preferredSize = Dimension(200, 30)  // 设置合适的输入框高度
                minimumSize = Dimension(150, 30)
                maximumSize = Dimension(300, 30)
            }

            add(label)
            add(Box.createHorizontalStrut(10))
            add(field)
            add(Box.createHorizontalGlue())
        }
    }

    /**
     * 创建示例面板
     */
    private fun createExamplePanel(): JPanel {
        return JPanel().apply {
            layout = BorderLayout(5, 5)
            alignmentX = Component.LEFT_ALIGNMENT
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Example Format"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )

            val exampleLabel = JLabel("<html>" +
                "<b>Input format:</b><br/>" +
                "name1${itemSeparatorField.text}value1${nameSeparatorField.text}name2${itemSeparatorField.text}value2<br/><br/>" +
                "<b>Example:</b><br/>" +
                "温度:25.5,湿度:60.2" +
                "</html>").apply {
                font = Font("Dialog", Font.PLAIN, 12)
            }

            add(exampleLabel, BorderLayout.CENTER)
        }
    }

    /**
     * 创建带标签的复选框面板
     */
    private fun createCheckBoxPanel(labelText: String, checkBox: JCheckBox): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT

            val label = JLabel(labelText).apply {
                preferredSize = Dimension(180, 25)
                minimumSize = Dimension(180, 25)
                maximumSize = Dimension(180, 25)
            }

            checkBox.apply {
                preferredSize = Dimension(200, 30)
                minimumSize = Dimension(150, 30)
                maximumSize = Dimension(300, 30)
            }

            add(label)
            add(Box.createHorizontalStrut(10))
            add(checkBox)
            add(Box.createHorizontalGlue())
        }
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

        val smoothLine = smoothLineCheckBox.isSelected
        val newConfig = PlotterConfig(itemSep, nameSep, windowSize, maxCache, smoothLine)
        onApply(newConfig)
        dispose()
    }

    private fun resetToDefault() {
        itemSeparatorField.text = ","
        nameSeparatorField.text = ":"
        windowSizeField.text = "20"
        maxCacheField.text = "2000"
        smoothLineCheckBox.isSelected = false  // 默认不启用平滑曲线
    }
}
