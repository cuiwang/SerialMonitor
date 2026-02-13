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
    private val colors = listOf(
        Color(31, 119, 180),    // 蓝色
        Color(255, 127, 14),    // 橙色
        Color(44, 160, 44),     // 绿色
        Color(214, 39, 40),     // 红色
        Color(148, 103, 189),   // 紫色
        Color(140, 86, 75),     // 棕色
        Color(227, 119, 194),   // 粉红色
        Color(127, 127, 127),   // 灰色
        Color(188, 143, 143),   // 褐色
        Color(23, 190, 207)     // 青色
    )

    init {
        layout = BorderLayout()
        background = Color(240, 240, 240)
    }

    /**
     * 更新图例，显示当前的所有数据系列
     */
    fun updateLegend() {
        removeAll()
        legendItems.clear()

        val seriesNames = plotterPanel.getSeriesNames()

        // 创建内部面板用于横向排列图例项
        val legendItemsPanel = JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT, 10, 5)
            background = Color(240, 240, 240)
        }

        for ((index, seriesName) in seriesNames.withIndex()) {
            val color = colors[index % colors.size]
            val isVisible = plotterPanel.isSeriesVisible(seriesName)

            // 创建图例项面板
            val itemPanel = JPanel().apply {
                layout = FlowLayout(FlowLayout.LEFT, 5, 2)
                background = Color.WHITE
                border = LineBorder(if (isVisible) color else Color.LIGHT_GRAY, 1)
                cursor = Cursor(Cursor.HAND_CURSOR)

                // 如果隐藏，则整体变半透明
                if (!isVisible) {
                    this.background = Color(245, 245, 245)
                }
            }

            // 圆点图标
            val dotLabel = JLabel().apply {
                icon = createDotIcon(color, isVisible)
                preferredSize = Dimension(12, 12)
            }

            // 系列名称
            val nameLabel = JLabel(seriesName).apply {
                font = Font("Arial", Font.PLAIN, 11)
                foreground = if (isVisible) Color.BLACK else Color.GRAY
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
                    itemPanel.background = if (plotterPanel.isSeriesVisible(seriesName))
                        Color(230, 240, 255) else Color(235, 235, 235)
                }

                override fun mouseExited(e: MouseEvent?) {
                    itemPanel.background = if (plotterPanel.isSeriesVisible(seriesName))
                        Color.WHITE else Color(245, 245, 245)
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
            minimumSize = Dimension(100, 35)
            preferredSize = Dimension(0, 35)  // 固定高度35px
            maximumSize = Dimension(Integer.MAX_VALUE, 35)
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










