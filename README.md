# Android Free Fall Detection App
This is an Android app that detects a free fall of the device.

- The program is able to differentiate a free fall from other kinds of sensor events 
  - (**shaking, etc.**).
- The detection also works while no user interface is in the foreground
  -  (A **foreground service** created for listening sensor events).

- The app also displays a list of detected free falls in screen with timestamp and duration.
- When a free fall occurs a **notification displays** as well.
- The app created in **Kotlin** to implement the specification.

### Free fall algorithms;
1. [Free Fall Detection Using 3-Axis Accelerometer](https://www.hackster.io/RVLAD/free-fall-detection-using-3-axis-accelerometer-06383e)
2. [Stack Overflow](https://stackoverflow.com/questions/36540058/can-anyone-tell-me-how-i-get-toast-when-mobile-falls-down)
