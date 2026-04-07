# 🌱 SmartAgri Monitoring System

A dual-architecture smart agriculture simulation that monitors farm conditions through virtual sensors and automates irrigation and shade control based on configurable thresholds.

This repository contains two complete, production-ready implementations:

| | Java Edition | Rust Edition |
|---|---|---|
| **Paradigm** | Object-Oriented, threaded | Async, trait-based |
| **Concurrency** | `TimerTask` + `Thread` | `tokio::spawn` + async/await |
| **Shared State** | `synchronized` methods | `Arc<Mutex<T>>` |
| **Error Handling** | Checked exceptions | `Result<T, E>` + typed enums |
| **File Logging** | `FileWriter` (blocking) | `tokio::fs` (non-blocking) |
| **Runtime** | JDK 17+ | Rust 1.75+ |

---

## 📁 Project Structure

```
SmartAgri/
├── src/                              ← Java OOP Implementation
│   ├── main.java
│   └── com/smartagri/
│       ├── sensors/
│       │   ├── TemperatureSensor.java
│       │   ├── SoilMoistureSensor.java
│       │   ├── HumiditySensor.java
│       │   └── LightSensor.java
│       ├── irrigation_shade/
│       │   ├── IrrigationSystem.java
│       │   └── ShadeSystem.java
│       ├── core/
│       │   ├── MonitoringSystem.java
│       │   └── FileLogger.java
│       └── users/
│           ├── User.java
│           ├── AuthenticationService.java
│           └── AuthenticationException.java
│
└── smart-agri-rust/                  ← Rust Async Engine
    ├── Cargo.toml
    └── src/
        └── main.rs
```

---

## ⚙️ How It Works

### Startup (both editions)
1. All sensors, actuators, logger, and auth service are wired together at startup.
2. The user is prompted to log in (up to 3 attempts).
3. On successful login, the background monitoring loop starts — scanning sensors every **10 seconds**.
4. A role-based menu is shown for interactive control.
5. On logout, the timer/task stops and all logs are flushed to `smartagri_log.txt`.

### Automatic Actions
| Condition | Action |
|---|---|
| Soil moisture < 30% | Irrigation turns **ON** |
| Soil moisture ≥ 30% | Irrigation turns **OFF** |
| Temperature > 35°C **or** Light > 7000 lux | Shade turns **ON** |
| Temperature ≤ 35°C **and** Light ≤ 7000 lux | Shade turns **OFF** |

---

## ☕ Java Edition

### Package Descriptions

#### `main.java`
Application entry point containing two inner classes:
- **MenuManager** — handles the login screen and displays the correct menu based on user role.
- **Connector** — wires all components (sensors, actuators, users, monitoring system) together.

#### `com.smartagri.sensors`
Four sensor classes simulating real hardware by generating random values within realistic ranges.

| Class | Simulated Range |
|---|---|
| `TemperatureSensor` | 15°C – 45°C |
| `SoilMoistureSensor` | 10% – 90% |
| `HumiditySensor` | 20% – 95% |
| `LightSensor` | 100 – 10,000 lux |

#### `com.smartagri.irrigation_shade`
Two actuator classes switchable ON/OFF automatically or manually by a Farmer.
- **IrrigationSystem** — controls water delivery.
- **ShadeSystem** — deploys or retracts shade covers.

#### `com.smartagri.core`
- **MonitoringSystem** — reads all sensors on a 10-second `TimerTask`, evaluates thresholds, and triggers actuators.
- **FileLogger** — writes timestamped log entries to `smartagri_log.txt` via `FileWriter`.

#### `com.smartagri.users`
- **User** — represents a system user with a username and role (`ADMIN`, `FARMER`, `AGRONOMIST`).
- **AuthenticationService** — validates credentials and returns the authenticated `User`.
- **AuthenticationException** — thrown on login failure, carrying a descriptive message.

### How to Run (Java)

**Prerequisites:** Java JDK 17 or higher

```bash
# Compile
cd SmartAgri/src
javac -d ../out $(find . -name "*.java")

# Run
cd SmartAgri/out
java main
```

---

## 🦀 Rust Edition

### Architecture

All components from the Java packages are unified in a single, well-sectioned `main.rs`, organized by logical comment headers. The key architectural shift is from blocking threads to a fully async runtime.

```rust
tokio::spawn(async move {
    let mut ticker = interval(Duration::from_secs(10));
    loop {
        ticker.tick().await;
        // read sensors → evaluate thresholds → trigger actuators → log
    }
});
```

### Sensor Trait

```rust
pub trait Sensor: Send + Sync {
    fn name(&self) -> &str;
    fn read(&self) -> f64;   // simulates a hardware read
    fn unit(&self) -> &str;
}
```

`Send + Sync` bounds allow `Arc<dyn Sensor>` trait objects to be safely shared across async tasks.

| Struct | Simulated Range |
|---|---|
| `TemperatureSensor` | 15.0°C – 45.0°C |
| `SoilMoistureSensor` | 10.0% – 90.0% |
| `HumiditySensor` | 20.0% – 95.0% |
| `LightSensor` | 100.0 – 10,000.0 lux |

### Actuator Trait

```rust
pub trait Actuator: Send + Sync {
    fn name(&self) -> &str;
    fn is_on(&self) -> bool;
    fn turn_on(&mut self);
    fn turn_off(&mut self);
}
```

Actuators are wrapped in `Arc<Mutex<dyn Actuator>>` so both the async monitoring loop and the interactive menu can safely mutate state without data races.

### Auth Error Handling

Failed logins return a typed `AuthError` enum instead of throwing an exception:

```rust
pub enum AuthError {
    UserNotFound,
    WrongPassword,
    AccountLocked,
}
```

### Crate Dependencies

```toml
[dependencies]
tokio   = { version = "1", features = ["full"] }  # async runtime
rand    = "0.8"                                    # sensor simulation
chrono  = "0.4"                                    # timestamped log entries
```

### How to Run (Rust)

**Prerequisites:** Rust toolchain 1.75+ (`rustup` recommended)

```bash
cd smart-agri-rust
cargo run --release

# Run tests
cargo test
```

---

## 👤 User Roles & Menus (both editions)

### Default Credentials
| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin |
| `farmer1` | `farm123` | Farmer |
| `agro1` | `agro123` | Agronomist |

### Permissions
| Role | Permissions |
|---|---|
| **Admin** | View sensors, view actuators, trigger manual scan, view system info |
| **Farmer** | View sensors, manually control irrigation and shade, view actuators |
| **Agronomist** | View sensors and actuator status (read-only) |

---

## 📝 Log Output

Both editions write to `smartagri_log.txt` in the working directory. The Java edition uses a blocking `FileWriter`; the Rust edition uses `tokio::fs` for non-blocking async writes.

```
[2025-06-01 10:00:00] [SYSTEM]  SmartAgri system starting up …
[2025-06-01 10:00:01] [AUTH]    Login attempt by 'farmer1': SUCCESS
[2025-06-01 10:00:10] [MONITOR] --- Auto-scan started ---
[2025-06-01 10:00:10] [SENSOR]  SoilMoistureSensor reading: 24.30 %
[2025-06-01 10:00:10] [ACTION]  MonitoringSystem -> Irrigation ON — moisture 24.3% < threshold 30.0%
[2025-06-01 10:00:10] [MONITOR] --- Auto-scan complete ---
```

---

## 🔮 Potential Extensions

### Shared / Both Editions
- Connect to real hardware sensors via serial/GPIO
- Add email/SMS alerts when thresholds are breached
- Support additional roles (e.g. Supervisor) with custom permissions
- Build a web or GUI dashboard for remote monitoring

### Java-Specific
- Add a database backend via JDBC / Hibernate for historical sensor data
- Expose a REST API with Spring Boot for remote control
- Migrate from `TimerTask` to `ScheduledExecutorService` for more robust scheduling

### Rust-Specific
- Replace in-memory credentials with a `SQLite` backend via `sqlx`
- Stream sensor data over WebSockets using `axum` for a live dashboard
- Add real GPIO/serial hardware support via the `rppal` crate (Raspberry Pi)
- Expose a REST API layer with `axum` or `actix-web`
- Add structured JSON logging with `tracing` + `tracing-subscriber`
