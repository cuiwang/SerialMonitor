package com.serialmonitor.serial

import java.io.File

/**
 * 跨平台串口检测器
 * 支持 Windows, macOS, Linux
 */
object PortDetector {

    /**
     * 获取所有可用的串口
     */
    fun getAvailablePorts(): List<String> {
        return when {
            isWindows() -> getWindowsPorts()
            isMac() -> getMacPorts()
            isLinux() -> getLinuxPorts()
            else -> emptyList()
        }
    }

    /**
     * 获取Windows上的串口
     */
    private fun getWindowsPorts(): List<String> {
        val ports = mutableListOf<String>()
        try {
            val command = arrayOf("powershell", "-Command", "Get-WmiObject Win32_SerialPort | Select-Object Name")
            val process = Runtime.getRuntime().exec(command)
            process.inputStream.bufferedReader().forEachLine { line ->
                if (line.matches(Regex("COM\\d+"))) {
                    ports.add(line.trim())
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            // Fallback: 检查常见的COM口
            for (i in 1..20) {
                if (File("COM$i").exists()) {
                    ports.add("COM$i")
                }
            }
        }
        return ports.sorted()
    }

    /**
     * 获取macOS上的串口
     */
    private fun getMacPorts(): List<String> {
        val ports = mutableListOf<String>()
        val devDir = File("/dev")
        devDir.listFiles { file ->
            file.name.startsWith("cu.") || file.name.startsWith("tty.")
        }?.forEach { file ->
            ports.add(file.absolutePath)
        }
        return ports.sorted()
    }

    /**
     * 获取Linux上的串口
     */
    private fun getLinuxPorts(): List<String> {
        val ports = mutableListOf<String>()
        val devDir = File("/dev")
        devDir.listFiles { file ->
            file.name.startsWith("ttyUSB") ||
            file.name.startsWith("ttyACM") ||
            file.name.startsWith("ttyS")
        }?.forEach { file ->
            ports.add(file.absolutePath)
        }
        return ports.sorted()
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name").toLowerCase().contains("win")
    }

    private fun isMac(): Boolean {
        return System.getProperty("os.name").toLowerCase().contains("mac")
    }

    private fun isLinux(): Boolean {
        return System.getProperty("os.name").toLowerCase().contains("linux")
    }
}

