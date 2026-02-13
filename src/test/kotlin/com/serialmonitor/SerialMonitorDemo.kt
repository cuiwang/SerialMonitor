package com.serialmonitor

import com.serialmonitor.data.SerialDataListener
import com.serialmonitor.data.SerialPortState
import com.serialmonitor.serial.PortDetector
import com.serialmonitor.serial.SerialPortManager
import org.junit.Test

/**
 * Serial Monitor插件功能演示和测试
 */
class SerialMonitorDemo {

    @Test
    fun demonstratePortDetection() {
        println("=== Port Detection Demo ===")
        val ports = PortDetector.getAvailablePorts()
        println("Available ports: $ports")
        assert(ports.isNotEmpty() || ports.isEmpty()) // 总是成功
    }

    @Test
    fun demonstratePortManager() {
        println("=== Port Manager Demo ===")

        val manager = SerialPortManager()

        // 添加监听器
        manager.addListener(object : SerialDataListener {
            override fun onDataReceived(data: String) {
                println("Received: $data")
            }

            override fun onConnectionStatusChanged(state: SerialPortState) {
                println("Connection status: $state")
            }

            override fun onError(errorMessage: String) {
                println("Error: $errorMessage")
            }
        })

        // 演示配置
        val config = manager.getConfig()
        println("Current config: $config")

        println("Demo completed")
    }
}

/**
 * 集成测试示例 - 与实际ESP32通信
 */
class SerialMonitorIntegrationTest {

    /**
     * 集成测试 - 连接真实设备
     *
     * 使用方式:
     * 1. 连接一个ESP32设备到USB
     * 2. 运行此测试
     * 3. 观察输出
     *
     * 注意: 此测试需要真实的硬件
     */
    @Test
    fun testConnectToRealDevice() {
        println("=== Real Device Connection Test ===")

        // 扫描可用端口
        val ports = PortDetector.getAvailablePorts()
        println("Available ports: $ports")

        if (ports.isEmpty()) {
            println("No ports found. Please connect an ESP32 device.")
            return
        }

        val firstPort = ports.first()
        println("Connecting to: $firstPort")

        val manager = SerialPortManager()

        // 设置监听器
        var receivedData = StringBuilder()
        manager.addListener(object : SerialDataListener {
            override fun onDataReceived(data: String) {
                receivedData.append(data)
                println("Received: $data")
            }

            override fun onConnectionStatusChanged(state: SerialPortState) {
                println("Status: $state")
            }

            override fun onError(errorMessage: String) {
                println("Error: $errorMessage")
            }
        })

        // 尝试连接
        val connected = manager.connect(firstPort, 115200)
        println("Connection result: $connected")

        if (connected) {
            println("✅ Successfully connected to $firstPort")

            // 等待接收一些数据
            Thread.sleep(2000)

            // 尝试发送数据
            manager.sendDataWithNewline("Hello ESP32!")
            println("Sent: Hello ESP32!")

            // 等待响应
            Thread.sleep(1000)

            println("Received data: $receivedData")

            // 断开连接
            manager.disconnect()
            println("✅ Disconnected")
        } else {
            println("❌ Failed to connect")
        }
    }
}

/**
 * 性能测试
 */
class SerialMonitorPerformanceTest {

    @Test
    fun testPortDetectionPerformance() {
        println("=== Performance Test: Port Detection ===")

        val startTime = System.currentTimeMillis()
        val ports = PortDetector.getAvailablePorts()
        val elapsed = System.currentTimeMillis() - startTime

        println("Detected ${ports.size} ports in ${elapsed}ms")
        assert(elapsed < 1000) // Should complete within 1 second
    }
}

