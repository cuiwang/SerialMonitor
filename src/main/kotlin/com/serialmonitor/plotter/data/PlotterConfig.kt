package com.serialmonitor.plotter.data

/**
 * 串口绘图仪的解析配置
 */
data class PlotterConfig(
    // 数据项分隔符（默认逗号）
    val itemSeparator: String = ",",
    // 名称和值的分隔符（默认冒号）
    val nameSeparator: String = ":",
    // 窗口显示点数（默认20）
    val windowSize: Int = 20,
    // 最大缓存点数（默认2000）
    val maxCache: Int = 2000
) {
    /**
     * 验证配置的有效性
     */
    fun validate(): Boolean {
        return itemSeparator.isNotEmpty() &&
               nameSeparator.isNotEmpty() &&
               itemSeparator != nameSeparator &&
               windowSize >= 10 &&
               maxCache >= 100
    }
}

/**
 * 改进的数据解析器，支持自定义分隔符
 */
object PlotterDataParserV2 {

    /**
     * 使用自定义配置解析一行数据
     * @param line 单行数据
     * @param config 解析配置
     * @return 解析后的DataLine，解析失败返回null
     */
    fun parseLine(line: String, config: PlotterConfig): DataLine? {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null

        val points = mutableListOf<DataPoint>()

        try {
            // 按自定义分隔符分割
            val items = trimmed.split(config.itemSeparator)

            for (item in items) {
                // 每个item格式: "名称{分隔符}值"
                val parts = item.trim().split(config.nameSeparator)
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val valueStr = parts[1].trim()

                    // 尝试转换为数字（支持int和float）
                    val value = try {
                        valueStr.toDouble()
                    } catch (e: Exception) {
                        null
                    }

                    // 只有名称和值都有效时才添加
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
     * @param config 解析配置
     * @return 解析后的DataLine列表
     */
    fun parseData(data: String, config: PlotterConfig): List<DataLine> {
        return data.split("\n")
            .mapNotNull { parseLine(it, config) }
    }
}
