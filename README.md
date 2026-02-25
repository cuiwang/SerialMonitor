# Serial Monitor for CLion/IntelliJ IDEA

A powerful Serial Port Monitor plugin for JetBrains IDEs (CLion, IntelliJ IDEA, PyCharm, etc.).

![Version](https://img.shields.io/badge/version-1.1.0-blue.svg)
![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## ✨ Features

### 📟 Monitor Tab
- **Real-time Serial Communication**: Monitor data from serial ports in real-time
- **Auto Port Detection**: Automatically detect available serial ports
- **Flexible Baud Rates**: Support for common baud rates (9600 - 921600)
- **Bi-directional Communication**: Send and receive data
- **Pause/Resume Control** *(v1.1.0)*: Pause data reception without disconnecting
- **Optional Timestamp Display** *(v1.1.0)*: Show HH:mm:ss.SSS prefix for each log entry
- **Advanced Filtering**: 
  - Normal text search (case-insensitive)
  - Regular expression filtering
  - Real-time log filtering
- **User-friendly Controls**:
  - Auto-scroll toggle
  - Clear output
  - Copy all logs to clipboard

### 📊 Plotter Tab
- **Real-time Data Visualization**: Plot numerical data in real-time
- **Multiple Data Series**: Support for multiple data channels
- **Smooth Curves**: Optional smooth line rendering using Catmull-Rom spline interpolation
- **Scrolling Window**: ECG-style auto-scrolling display
- **Interactive Controls**:
  - Zoom in/out with mouse wheel
  - Pan by dragging
  - Manual scrolling with scrollbar
  - Reset view button
- **Configurable Data Format**:
  - Custom item separator (default: `,`)
  - Custom name-value separator (default: `:`)
  - Adjustable window size
- **Legend Management**: Toggle visibility of individual data series

## 📥 Installation

### Method 1: From ZIP File (Recommended for testing)

1. Download the latest `Serial-Monitor-1.1.0.zip` from [Releases](../../releases)
2. Open your JetBrains IDE (CLion/IntelliJ IDEA/PyCharm)
3. Go to `Settings/Preferences` → `Plugins`
4. Click the ⚙️ icon → `Install Plugin from Disk...`
5. Select the downloaded ZIP file
6. Restart the IDE

### Method 2: From JetBrains Marketplace (Coming soon)

1. Open `Settings/Preferences` → `Plugins`
2. Search for "Serial Monitor"
3. Click `Install`
4. Restart the IDE

## 🚀 Quick Start

### Opening the Tool Window

After installation, you'll find "SerialMonitor" in the bottom tool window bar.

### Basic Usage

#### 1. Monitor Tab - Serial Communication

1. **Select Port**: Choose your serial port from the dropdown
2. **Set Baud Rate**: Select the appropriate baud rate (default: 115200)
3. **Connect**: Click the "Connect" button
4. **View Data**: Received data will appear in the output area
5. **Send Data**: Type in the input field and click "Send"

**Pause/Resume Communication** *(v1.1.0)*:
- Click the **"Pause"** button to pause data reception (both Monitor and Plotter stop updating)
- Click **"Resume"** to continue receiving data
- Useful for freezing data for analysis without disconnecting

**Timestamp Display** *(v1.1.0)*:
- Check **"Timestamp"** checkbox in the Filter panel
- Each log entry will display with format: `[HH:mm:ss.SSS] message`
- Timestamps work with all filter modes (normal text and regex)

**Filtering Logs:**
- Enter filter text in the "Filter" field
- Choose mode: `Normal` (text search) or `Regex` (regular expression)
- Click `Apply` or press `Enter`
- Click `Clear` to show all logs

#### 2. Plotter Tab - Data Visualization

**Data Format:**
```
name1:value1,name2:value2,name3:value3
```

**Example:**
```
temperature:25.5,humidity:60.2,pressure:1013.2
```

**Controls:**
- Mouse wheel: Zoom in/out
- Mouse drag: Pan the view
- Scrollbar: Manual scrolling (disables auto-scroll)
- Reset button: Return to auto-scroll mode
- Settings button: Configure data format and display options

**Settings:**
- **Item Separator**: Character separating different data items (default: `,`)
- **Name-Value Separator**: Character separating name and value (default: `:`)
- **Window Size**: Number of data points to display (default: 20)
- **Smooth Line**: Enable smooth curve rendering

## 📖 Use Cases

### Arduino/ESP32 Development
Perfect for monitoring serial output from microcontrollers:
```cpp
Serial.println("temp:25.5,humidity:60");
```

### Embedded Systems Debugging
Monitor real-time sensor data with visualization:
```
sensor1:123,sensor2:456,status:OK
```

### IoT Device Communication
Track device metrics and status in real-time.

## ⚙️ Configuration

### Global Settings

Go to `Settings/Preferences` → `Tools` → `Serial Monitor` to configure:
- Default baud rate
- Auto-connect on startup
- Data format preferences

### Plotter Settings

Click the `Settings` button in the Plotter tab to customize:
- Data parsing format
- Display window size
- Smooth line rendering

## 🔧 Requirements

- **JetBrains IDE**: 2024.1 or later
  - CLion
  - IntelliJ IDEA (Community/Ultimate)
  - PyCharm
  - Any JetBrains IDE based on IntelliJ Platform
- **Java**: 17 or later (bundled with IDE)
- **Operating System**: 
  - Windows
  - macOS
  - Linux

## 🐛 Known Issues

- On some Linux systems, you may need to add your user to the `dialout` group:
  ```bash
  sudo usermod -a -G dialout $USER
  ```
  Then logout and login again.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [jSerialComm](https://github.com/Fazecast/jSerialComm) - Serial communication library
- [XChart](https://github.com/knowm/XChart) - Charting library for real-time plotting

## 📮 Contact

- **Issues**: [GitHub Issues](https://github.com/cuiwang/SerialMonitor/issues)
- **Email**: 1991.cuiwang@gmail.com

## 🗺️ Roadmap

- [ ] Support for multiple serial ports simultaneously
- [ ] Export log data to file
- [ ] Custom color schemes for plotter
- [ ] Data recording and playback
- [ ] Advanced protocol analysis (HEX, Binary)

---

**⭐ If you find this plugin useful, please star the repository!**

