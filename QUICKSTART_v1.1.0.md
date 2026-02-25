# Serial Monitor Plugin v1.1.0 - Quick Start Guide

## Installation

### Step 1: Obtain the Plugin

**Option A: From Marketplace (Recommended)**
1. Open CLion
2. Go to **Settings** → **Plugins**
3. Search for "Serial Monitor"
4. Click **Install**
5. Restart CLion

**Option B: From File**
1. Download JAR file from release
2. Go to **Settings** → **Plugins**
3. Click ⚙️ icon → **Install Plugin from Disk**
4. Select the JAR file
5. Restart CLion

### Step 2: Verify Installation

1. Open CLion Tool Windows
2. Click **View** → **Tool Windows** → **Serial Monitor**
3. You should see two tabs: **Monitor** and **Plotter**

## Basic Usage

### Connecting to Serial Port

1. **Select Port:** Use the "Port" dropdown to select your serial port
2. **Set Baud Rate:** Choose from dropdown (default: 115200)
3. **Click Connect:** Button changes to "Disconnect"
4. **Status Message:** You'll see "=== Serial Port Connected ===" in Monitor

### Monitor Tab - Viewing Logs

#### Basic View
- **Red text**: Error messages
- **Black text**: Normal log output
- **Auto Scroll**: Enabled by default (stays at bottom)
- **Clear Button**: Removes all logs

#### Filtering Logs

1. **Enter filter text** in the "Filter:" field
2. **Choose filter mode:**
   - **Normal**: Simple text search (case-insensitive)
   - **Regex**: Regular expressions for advanced filtering
3. **Press Enter or click "Apply"** to filter
4. **Check "Live"** to filter as you type
5. **Click "Clear"** to show all logs again

**Filter Examples:**
- Normal: `ERROR` - finds all lines containing "ERROR"
- Regex: `^\[.*\]` - finds all lines starting with `[`
- Regex: `(temp|humidity)` - finds lines with either word

#### Timestamp Display

1. **Check "Timestamp"** checkbox in filter panel
2. Each log line now shows: `[HH:mm:ss.SSS] your log message`
3. **Uncheck** to remove timestamps
4. **Works with filters:** Timestamps remain visible when filtering

**Example Output:**
```
[14:23:45.123] System started
[14:23:46.456] Temperature: 25.5°C
[14:23:47.789] Humidity: 60%
```

#### Sending Data

1. **Type message** in the send field at bottom
2. **Press Enter** or click **Send** button
3. Message is sent with newline appended
4. Use this to send commands to your device

### Plotter Tab - Viewing Data

#### Data Format

Your serial output should contain comma-separated values:

```
series1,series2,series3
12.5,45.2,30.1
12.6,45.3,30.2
12.7,45.4,30.3
```

First line: Series names (optional)
Subsequent lines: Numeric values

#### Basic Plotter Controls

- **Clear**: Remove all data and reset
- **Reset**: Return to auto-scrolling mode
- **Settings**: Configure data parsing and appearance

#### Settings Dialog

1. Click **Settings** button
2. Configure:
   - **Example Format**: Show sample data format
   - **Max Points**: Maximum data points to store
   - **Window Size**: Number of points visible
   - **Smooth Line**: Enable smooth curve rendering

### Pause/Resume - New in v1.1.0

#### When to Use

- **Freeze data** for detailed inspection
- **Sync with other tools** that monitor same device
- **Prevent data loss** during UI navigation
- **Take snapshot** at specific moment

#### How to Use

1. **While connected**, click **"Pause"** button
2. **Monitor and Plotter stop updating** (data still being read)
3. **Analyze current data** without new updates
4. **Click "Resume"** to continue receiving data
5. **Button auto-disables** when disconnecting

## Common Tasks

### Task 1: Monitor Temperature Values

```
// Serial Output from Device:
temp,humidity,pressure
25.3,60.1,1013.5
25.4,60.2,1013.6
25.5,60.1,1013.7
```

1. Connect device
2. Go to Plotter tab
3. Data appears as three series
4. Smooth curves show trends
5. Use Reset to follow latest data

### Task 2: Find Error Messages

1. In Monitor tab, enter `ERROR` in Filter
2. Click "Apply"
3. Only lines with "ERROR" appear
4. Check "Live" to filter as messages arrive
5. Check "Timestamp" to see when errors occurred

### Task 3: Pause Before Analysis

1. Device is sending logs
2. Click "Pause" button
3. Messages stop appearing
4. Scroll through captured data
5. Copy all to clipboard
6. Click "Resume" to continue

### Task 4: Export Logs

1. In Monitor tab, click "Copy All"
2. Paste into text editor
3. Save as .txt file
4. Optional: Enable Timestamp before copying to include times

## Tips & Tricks

### Performance Tips

- Disable "Auto Scroll" if receiving very fast data
- Use filters to reduce visible content
- Pause when not actively monitoring
- Clear old data periodically (Monitor → Clear)

### Filtering Tips

- Use `|` (pipe) for OR: `ERROR|WARNING`
- Use `.*` for any characters: `temp.*C`
- Use `^` for line start: `^\\[`
- Case-insensitive by default in Normal mode

### Timestamp Tips

- Timestamps are generated when data is displayed
- May vary slightly from actual reception time
- Useful for correlating with other log sources
- Works with all filter modes

### Plotter Tips

- First data line can be series names
- All values must be numeric
- Use comma separator between values
- Empty lines are skipped
- Scroll bar allows viewing historical data

## Troubleshooting

### Issue: Port not appearing in list

**Solution:**
1. Click **Refresh** button
2. Ensure device is powered on
3. Check USB cable connection
4. Try different USB port

### Issue: Data not appearing

**Solution:**
1. Verify baud rate matches device (usually 115200)
2. Check cable for loose connection
3. Device may not be transmitting
4. Try sending test data from Monitor tab

### Issue: Pause button disabled

**Solution:**
- Pause button only works when connected
- Connect to serial port first
- Button will enable automatically

### Issue: Timestamp not showing

**Solution:**
1. Ensure "Timestamp" checkbox is checked
2. New data will have timestamps
3. Previous data won't have timestamps (refresh page)
4. Verify data is actually being received

### Issue: Plotter shows no curves

**Solution:**
1. Check data format: `value1,value2,value3`
2. Ensure all values are numbers
3. First line can be names: `temp,humidity,pressure`
4. Clear plotter and wait for new data

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Send Log Message | Enter (in send field) |
| Apply Filter | Enter (in filter field) |
| Pause/Resume | None (use button) |
| Clear Monitor | Ctrl+L (not implemented) |
| Copy All | Ctrl+A then Ctrl+C |

## Settings Menu

### Monitor Settings

- Auto Scroll: Keep view at latest message
- Filter Mode: Normal text or Regex
- Live Filter: Apply filters in real-time
- Timestamp: Show HH:mm:ss.SSS prefix

### Plotter Settings

- Data Format: Specify how to parse data
- Smooth Line: Enable curve smoothing
- Max Points: Cache size (default 2000)
- Window Size: Visible points (default 20)

## FAQ

**Q: Can I pause Plotter while keeping Monitor active?**
A: Not in v1.1.0 - pause affects both. v1.2.0 planned for selective pause.

**Q: Do timestamps affect Plotter data parsing?**
A: No! Timestamps only appear in Monitor display. Plotter receives clean data.

**Q: How long can I keep data paused?**
A: Indefinitely. Data continues to be buffered. Click Resume to continue.

**Q: Can I change timestamp format?**
A: Not in v1.1.0. v1.2.0 planned for custom formats.

**Q: Does this plugin work with other JetBrains IDEs?**
A: Currently CLion only. IntelliJ IDEA support possible in future.

## Getting Help

- **Plugin Issues**: Check marketplace reviews
- **Feature Requests**: Comment on marketplace or GitHub
- **Bug Reports**: Submit with steps to reproduce
- **Contact**: [Include contact information]

## Next Steps

- Explore Plotter tab with your data
- Try filtering with regex patterns
- Use Pause to analyze critical moments
- Export logs with timestamps for reporting

---

**Version:** 1.1.0  
**Last Updated:** 2026-02-25  
**Plugin ID:** com.serialmonitor.cuiwang

