package com.serialmonitor.data

/**
 * 串口配置数据类
 */
data class SerialPortConfig(
    var portName: String = "",
    var baudRate: Int = 115200,
    var dataBits: Int = 8,
    var stopBits: Int = 1,
    var parity: Int = 0 // 0: None, 1: Odd, 2: Even
)

/**
 * 串口状态枚举
 */
enum class SerialPortState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

/**
 * 串口数据监听器接口
 */
interface SerialDataListener {
    fun onDataReceived(data: String)
    fun onConnectionStatusChanged(state: SerialPortState)
    fun onError(errorMessage: String)
}

