package com.serialmonitor.plotter.data

/**
 * 单个数据点
 */
data class DataPoint(
    val name: String,  // 数据名称（如"长", "宽", "高"）
    val value: Double  // 数值
)

/**
 * 一行数据（包含多个数据点）
 * 例如: "长:100,宽:200,高:300" 解析后包含3个DataPoint
 */
data class DataLine(
    val points: List<DataPoint>,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 获取所有数据的名称列表
     */
    fun getNames(): List<String> = points.map { it.name }

    /**
     * 获取指定名称的值
     */
    fun getValue(name: String): Double? = points.find { it.name == name }?.value
}

/**
 * 串口数据解析器
 * 解析格式: "名称1:值1,名称2:值2,名称3:值3"
 */
object SerialDataParser {

    /**
     * 解析一行串口数据
     * @param line 单行数据，格式: "名称1:值1,名称2:值2,名称3:值3"
     * @return 解析后的DataLine，如果格式错误返回null
     */
    fun parseLine(line: String): DataLine? {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null

        val points = mutableListOf<DataPoint>()

        try {
            // 按逗号分割
            val items = trimmed.split(",")

            for (item in items) {
                // 每个item格式: "名称:值"
                val parts = item.trim().split(":")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val value = parts[1].trim().toDoubleOrNull()

                    if (value != null && name.isNotEmpty()) {
                        points.add(DataPoint(name, value))
                    }
                }
            }

            return if (points.isNotEmpty()) {
                DataLine(points)
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 解析多行数据
     * @param data 多行数据，用\n分隔
     * @return 解析后的DataLine列表
     */
    fun parseData(data: String): List<DataLine> {
        return data.split("\n")
            .mapNotNull { parseLine(it) }
    }
}

/**
 * 绘图数据管理器
 * 管理时间序列数据和所有数据系列，支持自定义解析配置
 */
class PlotterDataManager {

    private val dataLines = mutableListOf<DataLine>()
    private val dataSeriesMap = mutableMapOf<String, MutableList<Double>>()  // 数据系列 {名称 -> [值1, 值2, ...]}
    private val timePoints = mutableListOf<Long>()  // 时间戳

    // 当前的解析配置
    var config = PlotterConfig()
        private set

    /**
     * 更新解析配置
     */
    fun updateConfig(newConfig: PlotterConfig) {
        config = newConfig
        // 更新后立刻裁剪到新的最大缓存
        trimToMaxCache()
    }

    /**
     * 获取数据点总数
     */
    fun getDataPointCount(): Int = timePoints.size

    /**
     * 添加一行数据
     */
    fun addDataLine(line: DataLine) {
        dataLines.add(line)

        // 首次见到的数据名称需要初始化
        for (point in line.points) {
            if (!dataSeriesMap.containsKey(point.name)) {
                dataSeriesMap[point.name] = mutableListOf()
            }
        }

        // 添加时间戳
        timePoints.add(line.timestamp)

        // 添加各系列的值
        for ((name, values) in dataSeriesMap) {
            val value = line.getValue(name) ?: 0.0
            values.add(value)
        }

        // 超过最大缓存时裁剪
        trimToMaxCache()
    }

    /**
     * 超过最大缓存时裁剪最老数据
     */
    private fun trimToMaxCache() {
        val max = config.maxCache
        while (timePoints.size > max) {
            timePoints.removeAt(0)
            for (values in dataSeriesMap.values) {
                if (values.isNotEmpty()) {
                    values.removeAt(0)
                }
            }
        }
    }

    /**
     * 使用当前配置解析并添加串口数据
     */
    fun addSerialData(data: String) {
        val lines = PlotterDataParserV2.parseData(data, config)
        for (line in lines) {
            addDataLine(line)
        }
    }

    /**
     * 清除所有数据
     */
    fun clear() {
        dataLines.clear()
        dataSeriesMap.clear()
        timePoints.clear()
    }

    /**
     * 获取所有的数据系列名称（稳定排序，保证颜色映射一致）
     */
    fun getSeriesNames(): List<String> = dataSeriesMap.keys.sorted()

    /**
     * 获取指定数据系列
     */
    fun getSeries(name: String): List<Double>? = dataSeriesMap[name]?.toList()

    /**
     * 获取所有时间戳
     */
    fun getTimePoints(): List<Long> = timePoints.toList()
}
