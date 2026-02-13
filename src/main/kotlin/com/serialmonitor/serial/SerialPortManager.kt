package com.serialmonitor.serial

import com.serialmonitor.data.SerialDataListener
import com.serialmonitor.data.SerialPortConfig
import com.serialmonitor.data.SerialPortState
import com.fazecast.jSerialComm.SerialPort
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 串口管理器 - 处理连接、读写等
 * 使用jSerialComm库进行跨平台串口通信
 */
class SerialPortManager {

    private var serialPort: SerialPort? = null
    private var config = SerialPortConfig()
    private var currentState = SerialPortState.DISCONNECTED
    private var readThread: Thread? = null
    private var shouldKeepReading = false

    private val listeners = CopyOnWriteArrayList<SerialDataListener>()

    fun addListener(listener: SerialDataListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SerialDataListener) {
        listeners.remove(listener)
    }

    /**
     * 连接到串口
     */
    fun connect(portName: String, baudRate: Int = 115200): Boolean {
        return try {
            // 更新配置
            config = SerialPortConfig(
                portName = portName,
                baudRate = baudRate
            )

            if (serialPort != null && serialPort!!.isOpen) {
                serialPort!!.closePort()
            }

            serialPort = SerialPort.getCommPort(portName)
            serialPort!!.apply {
                this.baudRate = baudRate
                setComPortParameters(
                    baudRate,
                    config.dataBits,
                    config.stopBits,
                    config.parity
                )
                // 使用非阻塞读取降低延迟
                setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0)
            }

            if (serialPort!!.openPort()) {
                updateState(SerialPortState.CONNECTED)
                startReadThread()
                true
            } else {
                updateState(SerialPortState.ERROR)
                notifyError("Failed to open port: $portName")
                false
            }
        } catch (e: Exception) {
            updateState(SerialPortState.ERROR)
            notifyError("Failed to connect to $portName: ${e.message}")
            false
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        try {
            shouldKeepReading = false
            readThread?.join(1000) // 等待读线程退出
            serialPort?.closePort()
            serialPort = null
            updateState(SerialPortState.DISCONNECTED)
        } catch (e: Exception) {
            notifyError("Failed to disconnect: ${e.message}")
        }
    }

    /**
     * 发送数据到串口
     */
    fun sendData(data: String): Boolean {
        return try {
            if (serialPort?.isOpen == true) {
                val bytes = data.toByteArray()
                serialPort!!.writeBytes(bytes, bytes.size.toLong())
                true
            } else {
                notifyError("Serial port is not connected")
                false
            }
        } catch (e: Exception) {
            notifyError("Failed to send data: ${e.message}")
            false
        }
    }

    /**
     * 发送数据加换行符
     */
    fun sendDataWithNewline(data: String): Boolean {
        return sendData(data + "\n")
    }

    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean {
        return serialPort?.isOpen == true && currentState == SerialPortState.CONNECTED
    }

    /**
     * 获取当前配置
     */
    fun getConfig(): SerialPortConfig {
        return config.copy()
    }

    /**
     * 获取当前状态
     */
    fun getState(): SerialPortState {
        return currentState
    }

    /**
     * 启动读取线程
     */
    private fun startReadThread() {
        shouldKeepReading = true
        readThread = Thread {
            val buffer = ByteArray(1024)
            while (shouldKeepReading && serialPort?.isOpen == true) {
                try {
                    val available = serialPort!!.bytesAvailable()
                    if (available > 0) {
                        val readSize = minOf(buffer.size, available)
                        val numRead = serialPort!!.readBytes(buffer, readSize.toLong())
                        if (numRead > 0) {
                            val data = String(buffer, 0, numRead.toInt())
                            notifyDataReceived(data)
                        }
                    } else {
                        Thread.sleep(2)
                    }
                } catch (e: Exception) {
                    if (shouldKeepReading) {
                        notifyError("Error reading data: ${e.message}")
                    }
                    break
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun updateState(newState: SerialPortState) {
        currentState = newState
        listeners.forEach { it.onConnectionStatusChanged(newState) }
    }

    private fun notifyDataReceived(data: String) {
        listeners.forEach { it.onDataReceived(data) }
    }

    private fun notifyError(message: String) {
        listeners.forEach { it.onError(message) }
    }
}
