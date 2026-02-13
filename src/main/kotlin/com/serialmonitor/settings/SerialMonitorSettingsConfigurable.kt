package com.serialmonitor.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import javax.swing.*

/**
 * 串口监视器设置配置界面提供者
 */
class SerialMonitorSettingsConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable = SerialMonitorSettingsConfigurable()
}

/**
 * 串口监视器设置配置界面
 */
class SerialMonitorSettingsConfigurable : Configurable {

    private var settingsPanel: SerialMonitorSettingsPanel? = null
    private val settings = SerialMonitorSettings.getInstance()

    override fun getDisplayName(): String = "Serial Monitor"

    override fun createComponent(): JComponent {
        settingsPanel = SerialMonitorSettingsPanel(settings)
        return settingsPanel!!
    }

    override fun isModified(): Boolean {
        return settingsPanel?.isModified() ?: false
    }

    override fun apply() {
        settingsPanel?.apply()
    }

    override fun reset() {
        settingsPanel?.reset()
    }
}

/**
 * 设置面板
 */
class SerialMonitorSettingsPanel(private val settings: SerialMonitorSettings) : JPanel() {

    private val autoScrollCheckbox = JCheckBox("Auto Scroll", settings.autoScroll)
    private val addTimestampCheckbox = JCheckBox("Add Timestamp", settings.addTimestamp)
    private val addLineNumbersCheckbox = JCheckBox("Add Line Numbers", settings.addLineNumbers)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        add(JLabel("Serial Monitor Settings"))
        add(Box.createVerticalStrut(10))

        add(autoScrollCheckbox)
        add(Box.createVerticalStrut(5))

        add(addTimestampCheckbox)
        add(Box.createVerticalStrut(5))

        add(addLineNumbersCheckbox)
        add(Box.createVerticalStrut(5))

        add(Box.createVerticalGlue())
    }

    fun isModified(): Boolean {
        return autoScrollCheckbox.isSelected != settings.autoScroll ||
               addTimestampCheckbox.isSelected != settings.addTimestamp ||
               addLineNumbersCheckbox.isSelected != settings.addLineNumbers
    }

    fun apply() {
        settings.autoScroll = autoScrollCheckbox.isSelected
        settings.addTimestamp = addTimestampCheckbox.isSelected
        settings.addLineNumbers = addLineNumbersCheckbox.isSelected
    }

    fun reset() {
        autoScrollCheckbox.isSelected = settings.autoScroll
        addTimestampCheckbox.isSelected = settings.addTimestamp
        addLineNumbersCheckbox.isSelected = settings.addLineNumbers
    }
}

