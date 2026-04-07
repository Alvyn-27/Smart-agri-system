# 🌱 SmartAgri Monitoring System (Java & Rust Editions)

A dual-architecture smart agriculture simulation that monitors farm conditions through virtual sensors and automates irrigation and shade control. 

This repository contains two complete implementations of the system:
1. **Original Java Implementation:** A traditional Object-Oriented design utilizing threaded `TimerTasks`.
2. **High-Performance Rust Rewrite:** A modernized, asynchronous streaming engine utilizing `tokio` and thread-safe concurrency (`Arc<Mutex<T>>`).

---

## 📁 Project Structure

```text
SmartAgri/
├── src/                        <-- Original Java OOP Implementation
│   ├── main.java
│   └── com/smartagri/
│       ├── sensors/            (Temperature, Moisture, Humidity, Light)
│       ├── irrigation_shade/   (Irrigation & Shade Actuators)
│       ├── core/               (MonitoringSystem & FileLogger)
│       └── users/              (Auth Service & Role Management)
│
└── smart-agri-rust/            <-- High-Performance Rust Async Engine
    ├── Cargo.toml
    └── src/
        └── main.rs             (Unified async engine, traits, and simulation loop)
