package com.serialmonitor.plotter.ui

import javax.swing.*
import javax.swing.border.LineBorder
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

/**
 * 串口绘图仪的图例和控制面板
 * 支持点击图例项控制折线的显隐，使用圆点样式，支持横向滚动
 */
class PlotterLegendPanel(private val plotterPanel: SerialPlotterPanel) : JPanel() {

    private val legendItems = mutableMapOf<String, JPanel>()

    // 科技色彩方案（与 SerialPlotterPanel 保持一致）
    private val colors = listOf(
        Color(100, 200, 255),   // 科技蓝
        Color(255, 165, 0),     // 科技橙
        Color(80, 255, 100),    // 科技绿
        Color(255, 100, 130),   // 科技红
        Color(180, 120, 255),   // 科技紫
        Color(255, 200, 100),   // 科技金黄
        Color(255, 130, 200),   // 科技粉
        Color(100, 255, 200),   // 科技青
        Color(255, 255, 100),   // 科技黄
        Color(180, 255, 130)    // 科技浅绿
    )

    init {
        layout = BorderLayout(0, 0)
        background = getBackgroundColor()
        // 增加上下边距使图例垂直居中，去掉下方的横线
        border = BorderFactory.createEmptyBorder(8, 0, 0, 0)  // 上边距 8px，下边距 0
    }

    /**
     * 判断是否为暗色主题
     */
    private fun isDarkTheme(): Boolean {
        val bgColor = UIManager.getColor("Panel.background")
        return if (bgColor != null) {
            val brightness = (bgColor.red + bgColor.green + bgColor.blue) / 3
            brightness < 128
        } else {
            false
        }
    }

    /**
     * 获取背景色
     */
    private fun getBackgroundColor(): Color {
        return if (isDarkTheme()) Color(40, 40, 40) else Color(250, 250, 250)
    }

    /**
     * 获取图例项背景色
     */
    private fun getItemBackgroundColor(isVisible: Boolean): Color {
        return if (isDarkTheme()) {
            if (isVisible) Color(50, 50, 50) else Color(45, 45, 45)
        } else {
            if (isVisible) Color.WHITE else Color(245, 245, 245)
        }
    }

    /**
     * 获取文本颜色
     */
    private fun getTextColor(isVisible: Boolean): Color {
        return if (isDarkTheme()) {
            if (isVisible) Color(200, 200, 200) else Color(120, 120, 120)
        } else {
            if (isVisible) Color.BLACK else Color.GRAY
        }
    }

    /**
     * 更新图例，显示当前的所有数据系列
     */
    fun updateLegend() {
        removeAll()
        legendItems.clear()

        val seriesNames = plotterPanel.getSeriesNames()
        val isDark = isDarkTheme()

        // 创建内部面板用于横向排列图例项
        val legendItemsPanel = JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT, 6, 0)  // 水平间距 6px，垂直间距 0
            background = getBackgroundColor()
        }

        for ((index, seriesName) in seriesNames.withIndex()) {
            val color = colors[index % colors.size]
            val isVisible = plotterPanel.isSeriesVisible(seriesName)

            // 创建图例项面板
            val itemPanel = JPanel().apply {
                layout = FlowLayout(FlowLayout.LEFT, 3, 2)  // 紧凑布局
                background = getItemBackgroundColor(isVisible)
                // 去掉边框，只用内边距和背景色
                border = BorderFactory.createEmptyBorder(2, 4, 2, 4)  // 内边距
                cursor = Cursor(Cursor.HAND_CURSOR)
                preferredSize = Dimension(110, 18)  // 调整到 18px 高度，宽度 110px
            }

            // 圆点图标
            val dotLabel = JLabel().apply {
                icon = createDotIcon(color, isVisible)
                preferredSize = Dimension(10, 10)
            }

            // 系列名称
            val nameLabel = JLabel(seriesName).apply {
                font = Font("Monospaced", Font.PLAIN, 10)
                foreground = getTextColor(isVisible)
            }

            // 组装图例项
            itemPanel.add(dotLabel)
            itemPanel.add(nameLabel)

            // 添加点击事件监听
            val mouseListener = object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    toggleSeriesVisibility(seriesName)
                }

                override fun mouseEntered(e: MouseEvent?) {
                    itemPanel.background = if (plotterPanel.isSeriesVisible(seriesName)) {
                        if (isDark) Color(65, 65, 65) else Color(235, 242, 255)
                    } else {
                        if (isDark) Color(50, 50, 50) else Color(240, 240, 240)
                    }
                }

                override fun mouseExited(e: MouseEvent?) {
                    itemPanel.background = getItemBackgroundColor(plotterPanel.isSeriesVisible(seriesName))
                }
            }

            itemPanel.addMouseListener(mouseListener)
            dotLabel.addMouseListener(mouseListener)
            nameLabel.addMouseListener(mouseListener)

            legendItemsPanel.add(itemPanel)
            legendItems[seriesName] = itemPanel
        }

        // 使用JScrollPane支持横向滚动
        val scrollPane = JScrollPane(legendItemsPanel).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
            border = null
            minimumSize = Dimension(100, 22)
            preferredSize = Dimension(0, 22)  // 调整到 22px (18px 图例项 + 2px 上下边距)
            maximumSize = Dimension(Integer.MAX_VALUE, 22)
            background = getBackgroundColor()
            // 设置滚动面板背景
            viewport.background = getBackgroundColor()
        }

        add(scrollPane, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    /**
     * 创建圆点图标
     */
    private fun createDotIcon(color: Color, isVisible: Boolean): ImageIcon {
        val size = 10
        val icon = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = icon.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // 绘制圆点
        g2d.color = if (isVisible) color else Color.LIGHT_GRAY
        g2d.fillOval(0, 0, size - 1, size - 1)

        // 绘制边框
        g2d.color = if (isVisible) color.darker() else Color.GRAY
        g2d.drawOval(0, 0, size - 1, size - 1)

        g2d.dispose()
        return ImageIcon(icon)
    }

    /**
     * 切换数据系列的显隐
     */
    private fun toggleSeriesVisibility(seriesName: String) {
        val isCurrentlyVisible = plotterPanel.isSeriesVisible(seriesName)
        plotterPanel.setSeriesVisible(seriesName, !isCurrentlyVisible)

        // 更新图例显示
        updateLegend()
    }

    /**
     * 清除所有图例
     */
    fun clearLegend() {
        removeAll()
        legendItems.clear()
        revalidate()
        repaint()
    }
}

/**
 * 绘图仪的工具栏
 */
class PlotterToolBar(
    private val plotterPanel: SerialPlotterPanel
) : JToolBar() {

    init {
        isFloatable = false

        // 清除数据按钮
        val clearButton = JButton("Clear").apply {
            addActionListener {
                plotterPanel.clearData()
            }
        }
        add(clearButton)

        addSeparator()

        // 重置视图按钮
        val resetButton = JButton("Reset").apply {
            addActionListener {
                plotterPanel.resetView()
            }
        }
        add(resetButton)

        addSeparator()

        // 设置按钮
        val settingsButton = JButton("Settings").apply {
            addActionListener {
                showSettingsDialog()
            }
        }
        add(settingsButton)
    }

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
}

/**
 * PlotterPanel需要暴露dataManager供工具栏访问
 */

