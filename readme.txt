# Healthcare Management System

This is a comprehensive Java-based **Healthcare Management System** that simulates functionalities like appointments, chat, prescriptions, vital signs monitoring, feedback, video consultations, and emergency alerts. It is designed to demonstrate the integration of different modules in a healthcare application.

## 🚀 Features

- **User Types**: Patient, Doctor, Admin
- **Appointments**: Scheduling, confirmation, and viewing doctor schedules
- **Chat System**: Real-time doctor-patient messaging
- **Video Consultations**: Google Meet-like video call simulation
- **Vital Signs Monitoring**: Tracks vitals and triggers alerts on abnormal readings
- **Emergency System**: Panic button, automated alerts via Email and WhatsApp
- **Prescriptions**: Issuance, refill tracking, medication reminders
- **Feedback System**: Patients can rate and review doctors
- **Medical Records**: Diagnosis, clinical notes, and prescriptions
- **Notification System**: Email and WhatsApp reminders for appointments and medications

## 📦 Technologies Used

- **Java (JDK 17+)**
- **JavaMail API** for email notifications
- **Custom WhatsApp API integration**
- **OOP Principles** (Encapsulation, Abstraction, Inheritance, Polymorphism)
- **Collections**: HashMap, Lists, etc.
- **Multi-threading** for Chat System

## 🧰 Modules Overview

### `App.java`
Entry point of the application. Demonstrates and tests all features with sample data.

### `Patient`, `Doctor`, `Admin`
User roles with specific responsibilities and relationships.

### `AppointmentManager`
Handles scheduling, confirmation, and retrieval of appointments.

### `ChatServer`, `ChatClient`
Implements basic real-time chat functionality using threads.

### `VideoCallService`
Mocks video consultations using generated links.

### `VitalDatabase` & `VitalSignsAlertSystem`
Tracks and alerts on patient vital signs using predefined thresholds.

### `ReminderService`
Sends reminders via email and WhatsApp for:
- Appointments
- Medication timings

### `PanicButton`
Triggers emergency alerts to predefined contacts.

### `Prescription`
Handles medication issuance, instructions, and refill management.

### `FeedbackManager`
Collects and calculates average ratings for doctors.

### `MedicalRecord`
Stores diagnoses, notes, and prescribed medications.

## 🔧 Setup & Configuration

1. **Clone the Repository**

   ```bash
   git clone https://github.com/your-username/healthcare-system.git
   cd healthcare-system
