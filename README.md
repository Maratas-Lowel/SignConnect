# SignConnect

Here is a professional and structured README.md text tailored for your SignConnect repository. You can copy and paste this right into your README.md file in Android Studio or VS Code!

SignConnect
SignConnect is a minimalist, offline-first Android telehealth application designed to translate Filipino Sign Language (FSL) in real-time for medical contexts. It bridges the communication gap between healthcare providers and the Deaf community by running on-device hand tracking and machine learning inference without requiring an active internet connection.

Key Features
Offline-First Architecture: Operates fully on-device to ensure privacy, reliability, and usability in low-connectivity or remote medical environments.

Real-Time FSL Translation: Translates medical sign gestures (such as common symptoms, terms, and inquiries) into readable text for clinicians.

MediaPipe Integration: Utilizes advanced computer vision hand-tracking pipelines for high-accuracy gesture extraction.

Lightweight TFLite Inference: Employs optimized TensorFlow Lite models for fast, low-latency classification on mobile hardware.

Tech Stack
Platform: Android (Java)

Computer Vision: MediaPipe

Machine Learning: TensorFlow Lite (TFLite)

IDE: Android Studio

Version Control: Git & GitHub

Project Structure
app/ — Core Android application source code and resources.

dataset/ — Organized image/video directories for Filipino Sign Language medical signs.

model-training/ — Python scripts, notebooks, and exported .tflite model artifacts.

Getting Started
Clone the repository:

Bash
git clone https://github.com/Maratas-Lowel/SignConnect.git
Open the project folder in Android Studio.

Sync Gradle dependencies and build the project on an Android device or emulator (API 24 or higher recommended).

Developed by Lowel Maratas
