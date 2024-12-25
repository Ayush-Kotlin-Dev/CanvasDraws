# CanvasDraws

CanvasDraws is a modern Android drawing application built with Jetpack Compose that provides an intuitive canvas for digital drawing, text manipulation, and creative expression.


## Features

### Drawing Capabilities
- 🎨 Freehand drawing with customizable colors
- ↩️ Undo/Redo functionality
- 🗑️ Clear canvas option
- 🎯 Smooth path drawing with touch gestures

### Text Management
- ✍️ Add text elements anywhere on canvas
- 🔄 Edit text content through a dialog interface
- 📏 Resize and move text elements
- 🎯 Select and modify existing text elements

### Text Styling
- 🎨 Customize text color
- 📝 Multiple font families support
- 🔠 Text formatting options:
  - Bold
  - Italic
  - Underline
  - Custom font size

### User Experience
- 📱 Edge-to-edge design
- 💫 Haptic feedback for interactions
- 🎯 Intuitive controls and UI
- 🌓 Support for both light and dark themes

## Technical Details

### Built With
- Kotlin
- Jetpack Compose
- Material Design 3
- MVVM Architecture
- Coroutines & Flow
- ViewModel
- Lifecycle Components

### Key Components
- `DrawingViewModel`: Manages drawing state and user actions
- `DrawingCanvas`: Main canvas component for drawing and text rendering
- `VibratorService`: Handles haptic feedback
- `CanvasControls`: UI controls for drawing tools and text manipulation

## Upcoming Features
- [ ] Settings option for text placement preferences
- [ ] Local storage for saving and restoring drawings
- [ ] Additional drawing tools and brushes
- [ ] Export/Share functionality
