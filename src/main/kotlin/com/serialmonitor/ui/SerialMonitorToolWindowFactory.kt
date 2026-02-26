package com.serialmonitor.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * 串口监视器工具窗口工厂
 */
class SerialMonitorToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val serialMonitorPanel = SerialMonitorPanel()
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(serialMonitorPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}


