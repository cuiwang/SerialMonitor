package com.serialmonitor.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * 串口监视器设置 - 用于保存用户配置
 */
@Service
@State(
    name = "SerialMonitorSettings",
    storages = [Storage("serialMonitorSettings.xml")]
)
class SerialMonitorSettings : PersistentStateComponent<SerialMonitorSettings> {

    var lastPortName: String = ""
    var lastBaudRate: Int = 115200
    var autoScroll: Boolean = true
    var addTimestamp: Boolean = false
    var addLineNumbers: Boolean = false

    override fun getState(): SerialMonitorSettings = this

    override fun loadState(state: SerialMonitorSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): SerialMonitorSettings {
            return com.intellij.openapi.components.ServiceManager.getService(SerialMonitorSettings::class.java)
        }
    }
}

