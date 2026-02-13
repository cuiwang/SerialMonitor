package com.serialmonitor.plotter

import com.serialmonitor.plotter.data.SerialDataParser
import com.serialmonitor.plotter.data.PlotterDataManager

/**
 * Serial Plotter 功能说明和测试
 */
fun main() {
    // 示例1: 解析单行数据
    println("=== Serial Plotter 使用说明 ===\n")

    println("1. 数据格式:")
    println("   单行格式: 名称1:值1,名称2:值2,名称3:值3")
    println("   例如: 长:100,宽:200,高:300\n")

    println("2. 数据解析示例:")
    val line1 = "长:100,宽:200,高:300"
    val parsedLine = SerialDataParser.parseLine(line1)
    println("   输入: $line1")
    println("   解析结果: $parsedLine\n")

    println("3. 多行数据处理:")
    val multiLineData = """
        长:100,宽:200,高:300
        长:105,宽:205,高:305
        长:110,宽:210,高:310
    """.trimIndent()

    val manager = PlotterDataManager()
    manager.addSerialData(multiLineData)

    println("   输入数据:")
    println(multiLineData)
    println("\n   解析后的数据系列:")
    for (seriesName in manager.getSeriesNames()) {
        val values = manager.getSeries(seriesName)
        println("   $seriesName: $values")
    }

    println("\n4. 功能特点:")
    println("   - 支持多个数据系列同时显示")
    println("   - X轴表示时间序列（数据索引）")
    println("   - Y轴表示数据值")
    println("   - 自动调整Y轴范围")
    println("   - 支持隐显各数据系列（通过图例点击）")
    println("   - 支持清除数据和重置视图")

    println("\n5. 使用场景:")
    println("   - 实时监控传感器数据")
    println("   - 调试多路ADC输入")
    println("   - 性能监测（温度、电流、电压等）")
    println("   - 机器学习模型的实时验证")
}

/**
 * Plotter 模块概览
 *
 * 文件结构:
 *   com.serialmonitor.plotter/
 *   ├── data/
 *   │   └── PlotterData.kt       # 数据解析和管理
 *   └── ui/
 *       ├── SerialPlotterPanel.kt      # 主绘图面板
 *       ├── PlotterLegendPanel.kt      # 图例和工具栏
 *       └── SerialPlotterMainPanel.kt  # 容器面板
 *
 * 核心类:
 *   - DataPoint: 单个数据点 (名称+值)
 *   - DataLine: 一行数据 (多个DataPoint + 时间戳)
 *   - SerialDataParser: 数据解析器
 *   - PlotterDataManager: 数据管理器
 *   - SerialPlotterPanel: 图表绘制
 *   - PlotterLegendPanel: 图例显示和控制
 *   - SerialPlotterMainPanel: 集成面板
 *
 * 功能流程:
 *   1. 串口接收数据 -> onDataReceived()
 *   2. SerialDataParser.parseData() -> DataLine列表
 *   3. PlotterDataManager.addDataLine() -> 存储数据
 *   4. SerialPlotterPanel.updateChart() -> 更新XChart图表
 *   5. PlotterLegendPanel.updateLegend() -> 更新图例
 *   6. 用户点击图例勾选框 -> 控制数据系列显隐
 *
 * 数据系列管理:
 *   - 自动识别所有唯一的数据名称
 *   - 为每个数据系列分配不同颜色
 *   - 支持最多500个数据点（自动滑动窗口）
 *   - X轴自动调整到数据范围
 *   - Y轴自动调整到数据值范围 ± 10% margin
 */

