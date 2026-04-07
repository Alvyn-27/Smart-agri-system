use std::io::{self, Write};
use std::sync::{Arc, Mutex};
use tokio::time::{sleep, Duration};

// ==========================================
// 1. USERS PACKAGE (Auth & Roles)
// ==========================================
mod users {
    use std::fmt;

    #[derive(Debug, Clone, PartialEq)]
    pub enum Role {
        Admin,
        Agronomist,
        Farmer,
    }

    impl fmt::Display for Role {
        fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
            match self {
                Role::Admin => write!(f, "ADMIN"),
                Role::Agronomist => write!(f, "AGRONOMIST"),
                Role::Farmer => write!(f, "FARMER"),
            }
        }
    }

    #[derive(Debug, Clone)]
    pub struct User {
        pub username: String,
        hashed_password: String,
        pub role: Role,
    }

    impl User {
        pub fn new(username: &str, password: &str, role: Role) -> Self {
            Self {
                username: username.to_string(),
                hashed_password: Self::hash_password(password),
                role,
            }
        }

        // Exact translation of the Java string reversal hashing
        fn hash_password(password: &str) -> String {
            let reversed: String = password.chars().rev().collect();
            format!("{}_hashed", reversed)
        }

        pub fn check_password(&self, password: &str) -> bool {
            self.hashed_password == Self::hash_password(password)
        }
    }

    pub enum AuthError {
        UserNotFound(String),
        IncorrectPassword(String),
    }

    impl fmt::Display for AuthError {
        fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
            match self {
                AuthError::UserNotFound(user) => write!(f, "Login failed: Username '{}' not found.\n  -> Username does not exist.", user),
                AuthError::IncorrectPassword(user) => write!(f, "Login failed: Incorrect password for user '{}'.\n  -> Password is incorrect.", user),
            }
        }
    }

    pub struct AuthService {
        users: Vec<User>, // Replaces the manual User[] array expansion
    }

    impl AuthService {
        pub fn new() -> Self {
            let mut service = Self { users: Vec::new() };
            service.register("admin", "admin123", Role::Admin);
            service.register("agro", "agro123", Role::Agronomist);
            service.register("farmer", "farmer123", Role::Farmer);
            service
        }

        pub fn register(&mut self, username: &str, password: &str, role: Role) -> bool {
            if self.users.iter().any(|u| u.username == username) {
                return false;
            }
            self.users.push(User::new(username, password, role));
            true
        }

        pub fn login(&self, username: &str, password: &str) -> Result<User, AuthError> {
            let user = self
                .users
                .iter()
                .find(|u| u.username == username)
                .ok_or_else(|| AuthError::UserNotFound(username.to_string()))?;

            if !user.check_password(password) {
                return Err(AuthError::IncorrectPassword(username.to_string()));
            }

            Ok(user.clone())
        }
    }
}

// ==========================================
// 2. SENSORS PACKAGE 
// ==========================================
mod sensors {
    use rand::Rng;

    pub trait Sensor: Send + Sync {
        fn id(&self) -> &str;
        fn sensor_type(&self) -> &str;
        fn read(&mut self) -> f64;
        fn current_value(&self) -> f64;
    }

    pub struct TemperatureSensor { id: String, current: f64, min: f64, max: f64 }
    impl TemperatureSensor {
        pub fn new(id: &str, min: f64, max: f64) -> Self {
            Self { id: id.to_string(), current: 0.0, min, max }
        }
    }
    impl Sensor for TemperatureSensor {
        fn id(&self) -> &str { &self.id }
        fn sensor_type(&self) -> &str { "Temperature" }
        fn current_value(&self) -> f64 { self.current }
        fn read(&mut self) -> f64 {
            let mut rng = rand::thread_rng();
            self.current = self.min + (self.max - self.min) * rng.gen::<f64>();
            self.current
        }
    }

    pub struct SoilMoistureSensor { id: String, current: f64 }
    impl SoilMoistureSensor {
        pub fn new(id: &str) -> Self { Self { id: id.to_string(), current: 0.0 } }
    }
    impl Sensor for SoilMoistureSensor {
        fn id(&self) -> &str { &self.id }
        fn sensor_type(&self) -> &str { "Soil Moisture" }
        fn current_value(&self) -> f64 { self.current }
        fn read(&mut self) -> f64 {
            let mut rng = rand::thread_rng();
            self.current = 10.0 + (60.0 * rng.gen::<f64>());
            self.current
        }
    }

    pub struct HumiditySensor { id: String, current: f64 }
    impl HumiditySensor {
        pub fn new(id: &str) -> Self { Self { id: id.to_string(), current: 0.0 } }
    }
    impl Sensor for HumiditySensor {
        fn id(&self) -> &str { &self.id }
        fn sensor_type(&self) -> &str { "Humidity" }
        fn current_value(&self) -> f64 { self.current }
        fn read(&mut self) -> f64 {
            let mut rng = rand::thread_rng();
            self.current = 30.0 + (60.0 * rng.gen::<f64>());
            self.current
        }
    }

    pub struct LightIntensitySensor { id: String, current: f64 }
    impl LightIntensitySensor {
        pub fn new(id: &str) -> Self { Self { id: id.to_string(), current: 0.0 } }
    }
    impl Sensor for LightIntensitySensor {
        fn id(&self) -> &str { &self.id }
        fn sensor_type(&self) -> &str { "Light Intensity" }
        fn current_value(&self) -> f64 { self.current }
        fn read(&mut self) -> f64 {
            let mut rng = rand::thread_rng();
            self.current = 1000.0 + (49000.0 * rng.gen::<f64>());
            self.current
        }
    }
}

// ==========================================
// 3. ACTUATORS PACKAGE
// ==========================================
mod actuators {
    pub trait Actuator: Send + Sync {
        fn id(&self) -> &str;
        fn activate(&mut self, logger: &mut crate::core_sys::FileLogger);
        fn deactivate(&mut self, logger: &mut crate::core_sys::FileLogger);
        fn status(&self) -> String;
    }

    pub struct IrrigationSystem { id: String, is_on: bool }
    impl IrrigationSystem {
        pub fn new(id: &str) -> Self { Self { id: id.to_string(), is_on: false } }
    }
    impl Actuator for IrrigationSystem {
        fn id(&self) -> &str { &self.id }
        fn activate(&mut self, logger: &mut crate::core_sys::FileLogger) {
            if !self.is_on {
                self.is_on = true;
                logger.log_event("Irrigation", &format!("System {} activated.", self.id));
            }
        }
        fn deactivate(&mut self, logger: &mut crate::core_sys::FileLogger) {
            if self.is_on {
                self.is_on = false;
                logger.log_event("Irrigation", &format!("System {} deactivated.", self.id));
            }
        }
        fn status(&self) -> String { format!("{} is {}", self.id, if self.is_on { "ON" } else { "OFF" }) }
    }

    pub struct ShadeSystem { id: String, is_deployed: bool }
    impl ShadeSystem {
        pub fn new(id: &str) -> Self { Self { id: id.to_string(), is_deployed: false } }
    }
    impl Actuator for ShadeSystem {
        fn id(&self) -> &str { &self.id }
        fn activate(&mut self, logger: &mut crate::core_sys::FileLogger) {
            if !self.is_deployed {
                self.is_deployed = true;
                logger.log_event("SHADE_SYSTEM", &format!("Shades {} on.", self.id));
            }
        }
        fn deactivate(&mut self, logger: &mut crate::core_sys::FileLogger) {
            if self.is_deployed {
                self.is_deployed = false;
                logger.log_event("SHADE_SYSTEM", &format!("Shades {} off.", self.id));
            }
        }
        fn status(&self) -> String { format!("{} is {}", self.id, if self.is_deployed { "ON" } else { "OFF" }) }
    }
}

// ==========================================
// 4. CORE SYSTEM (Monitoring & Logging)
// ==========================================
mod core_sys {
    use std::collections::HashMap;
    use std::fs::OpenOptions;
    use std::io::Write;
    use chrono::Local;
    use crate::sensors::Sensor;
    use crate::actuators::Actuator;

    pub struct FileLogger {
        file: Option<std::fs::File>,
    }

    impl FileLogger {
        pub fn new() -> Self {
            let file = OpenOptions::new()
                .create(true)
                .append(true)
                .open("smart_agri_log.txt")
                .map_err(|e| eprintln!("CRITICAL ERROR: Could not open log file. {}", e))
                .ok();
            Self { file }
        }

        pub fn log(&mut self, message: &str) {
            let timestamp = Local::now().format("%H:%M:%S").to_string();
            let log_entry = format!("[{}] {}\n", timestamp, message);
            if let Some(f) = &mut self.file {
                let _ = f.write_all(log_entry.as_bytes());
            }
        }

        pub fn log_cat(&mut self, category: &str, message: &str) {
            self.log(&format!("{}: {}", category, message));
        }

        pub fn log_event(&mut self, event_type: &str, details: &str) {
            self.log(&format!("EVENT: {} ({})", event_type, details));
        }
    }

    pub struct MonitoringSystem {
        pub sensors: Vec<Box<dyn Sensor>>,
        pub actuators: Vec<Box<dyn Actuator>>,
        pub thresholds: HashMap<String, f64>,
        pub logger: FileLogger,
    }

    impl MonitoringSystem {
        pub fn new() -> Self {
            let mut thresholds = HashMap::new();
            thresholds.insert("Moisture_Low".to_string(), 30.0);
            thresholds.insert("Temp_High".to_string(), 35.0);

            Self {
                sensors: Vec::new(),
                actuators: Vec::new(),
                thresholds,
                logger: FileLogger::new(),
            }
        }

        pub fn check_all_conditions(&mut self) {
            self.logger.log_cat("MONITOR", "Running automation check");
            let mut current_moisture = -1.0;
            let mut current_temp = -1.0;

            for sensor in &mut self.sensors {
                let val = sensor.read();
                let log_msg = format!("Sensor {} ({}): {:.2}", sensor.id(), sensor.sensor_type(), val);
                self.logger.log(&log_msg);

                match sensor.sensor_type() {
                    "Soil Moisture" => current_moisture = val,
                    "Temperature" => current_temp = val,
                    _ => {}
                }
            }

            // Rules Engine
            if current_moisture != -1.0 {
                if let Some(&low_thresh) = self.thresholds.get("Moisture_Low") {
                    if let Some(irrigation) = self.actuators.iter_mut().find(|a| a.id() == "IRR-MAIN") {
                        if current_moisture < low_thresh {
                            self.logger.log_event("AUTOMATION", &format!("Soil moisture LOW ({:.1}). Activating irrigation.", current_moisture));
                            irrigation.activate(&mut self.logger);
                        } else {
                            irrigation.deactivate(&mut self.logger);
                        }
                    }
                }
            }

            if current_temp != -1.0 {
                if let Some(&high_thresh) = self.thresholds.get("Temp_High") {
                    if let Some(shade) = self.actuators.iter_mut().find(|a| a.id() == "SHD-MAIN") {
                        if current_temp > high_thresh {
                            self.logger.log_event("AUTOMATION", &format!("Temperature HIGH ({:.1}). Deploying shades.", current_temp));
                            shade.activate(&mut self.logger);
                        } else {
                            shade.deactivate(&mut self.logger);
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. MAIN CLI & ASYNC LOOP
// ==========================================
use users::{AuthService, Role, User};
use sensors::*;
use actuators::*;
use core_sys::MonitoringSystem;

#[tokio::main]
async fn main() {
    let auth_service = AuthService::new();
    // Using Arc<Mutex<T>> explicitly demonstrates thread-safe ownership for concurrent access
    let monitor = Arc::new(Mutex::new(MonitoringSystem::new()));

    // Initialization
    {
        let mut m = monitor.lock().unwrap();
        m.logger.log_cat("SYSTEM", "Smart Agriculture System Initializing...");
        m.sensors.push(Box::new(TemperatureSensor::new("T1", 10.0, 40.0)));
        m.sensors.push(Box::new(SoilMoistureSensor::new("M1")));
        m.sensors.push(Box::new(HumiditySensor::new("H1")));
        m.sensors.push(Box::new(LightIntensitySensor::new("L1")));
        
        m.actuators.push(Box::new(IrrigationSystem::new("IRR-MAIN")));
        m.actuators.push(Box::new(ShadeSystem::new("SHD-MAIN")));
        m.logger.log_cat("SYSTEM", "Initialization complete.");
    }

    loop {
        println!("\nSmart Agriculture Login");
        println!("1. Login");
        println!("2. Exit");
        print!("Choose an option: ");
        io::stdout().flush().unwrap();

        let mut choice = String::new();
        io::stdin().read_line(&mut choice).unwrap();

        match choice.trim() {
            "1" => {
                if let Some(user) = handle_login(&auth_service) {
                    // Spawn background simulation using Tokio (replaces java.util.Timer)
                    let monitor_clone = Arc::clone(&monitor);
                    let sim_task = tokio::spawn(async move {
                        loop {
                            sleep(Duration::from_millis(10000)).await;
                            let mut m = monitor_clone.lock().unwrap();
                            m.logger.log_cat("Simulation", "Running periodic sensor scan and automation");
                            m.check_all_conditions();
                        }
                    });

                    run_user_menu(&user, &monitor);
                    
                    // Stop simulation on logout
                    sim_task.abort();
                    monitor.lock().unwrap().logger.log_cat("Simulation", "Simulation stopped.");
                }
            }
            "2" => break,
            _ => println!("Invalid option. Please try again."),
        }
    }

    println!("Shutting down Smart Agriculture System\nGoodbye.");
}

fn handle_login(auth: &AuthService) -> Option<User> {
    print!("Enter username: ");
    io::stdout().flush().unwrap();
    let mut username = String::new();
    io::stdin().read_line(&mut username).unwrap();

    print!("Enter password: ");
    io::stdout().flush().unwrap();
    let mut password = String::new();
    io::stdin().read_line(&mut password).unwrap();

    match auth.login(username.trim(), password.trim()) {
        Ok(user) => {
            println!("Welcome, {} ({})", user.username, user.role);
            Some(user)
        }
        Err(e) => {
            println!("{}", e);
            None
        }
    }
}

fn run_user_menu(user: &User, monitor: &Arc<Mutex<MonitoringSystem>>) {
    loop {
        let keep_going = match user.role {
            Role::Admin => show_admin_menu(monitor),
            Role::Agronomist => show_agronomist_menu(monitor),
            Role::Farmer => show_farmer_menu(monitor),
        };
        if !keep_going { break; }
    }
    println!("You have been logged out.");
}

fn show_admin_menu(monitor: &Arc<Mutex<MonitoringSystem>>) -> bool {
    println!("\n[Admin Menu]\n1. View All Sensor Data\n2. View All Actuator Status\n3. View System Thresholds\n4. Set New Threshold\n5. Logout");
    print!("Choose option: ");
    io::stdout().flush().unwrap();

    let mut choice = String::new();
    io::stdin().read_line(&mut choice).unwrap();

    let mut m = monitor.lock().unwrap();
    match choice.trim() {
        "1" => view_sensors(&m),
        "2" => view_actuators(&m),
        "3" => view_thresholds(&m),
        "4" => set_threshold(&mut m),
        "5" => return false,
        _ => println!("Invalid option."),
    }
    true
}

fn show_agronomist_menu(monitor: &Arc<Mutex<MonitoringSystem>>) -> bool {
    println!("\n[Agronomist Menu]\n1. View All Sensor Data\n2. View System Thresholds\n3. Run Manual System Check\n4. Logout");
    print!("Choose option: ");
    io::stdout().flush().unwrap();

    let mut choice = String::new();
    io::stdin().read_line(&mut choice).unwrap();

    let mut m = monitor.lock().unwrap();
    match choice.trim() {
        "1" => view_sensors(&m),
        "2" => view_thresholds(&m),
        "3" => {
            println!("Running manual system check...");
            m.check_all_conditions();
        }
        "4" => return false,
        _ => println!("Invalid option."),
    }
    true
}

fn show_farmer_menu(monitor: &Arc<Mutex<MonitoringSystem>>) -> bool {
    println!("\n[Farmer Menu]\n1. View My Sensor Data\n2. View Actuator Status\n3. Manually Activate Irrigation\n4. Manually Deactivate Irrigation\n5. Logout");
    print!("Choose option: ");
    io::stdout().flush().unwrap();

    let mut choice = String::new();
    io::stdin().read_line(&mut choice).unwrap();

    // 1. Lock the thread
    let mut m = monitor.lock().unwrap();
    
    // 2. THE FIX: Dereference the MutexGuard into a standard mutable reference.
    // Rust can now see that 'actuators' and 'logger' are disjoint fields!
    let sys = &mut *m;

    match choice.trim() {
        "1" => view_sensors(sys),
        "2" => view_actuators(sys),
        "3" => {
            if let Some(a) = sys.actuators.iter_mut().find(|a| a.id() == "IRR-MAIN") { 
                a.activate(&mut sys.logger); 
                println!("Activated."); 
            }
        },
        "4" => {
            if let Some(a) = sys.actuators.iter_mut().find(|a| a.id() == "IRR-MAIN") { 
                a.deactivate(&mut sys.logger); 
                println!("Deactivated."); 
            }
        },
        "5" => return false,
        _ => println!("Invalid option."),
    }
    true
}

// Helpers
fn view_sensors(m: &MonitoringSystem) {
    println!("\nCurrent Sensor Readings");
    for s in &m.sensors {
        println!("  - {} ({}): {:.2}", s.id(), s.sensor_type(), s.current_value());
    }
}

fn view_actuators(m: &MonitoringSystem) {
    println!("\nCurrent Actuator Status");
    for a in &m.actuators {
        println!("  - {}", a.status());
    }
}

fn view_thresholds(m: &MonitoringSystem) {
    println!("\nSystem Automation Thresholds");
    if m.thresholds.is_empty() { println!("  No thresholds set."); return; }
    for (k, v) in &m.thresholds {
        println!("  - {}: {}", k, v);
    }
}

fn set_threshold(m: &mut MonitoringSystem) {
    println!("Current Thresholds:");
    view_thresholds(m);
    
    print!("Enter threshold key to change (e.g. Moisture_Low): ");
    io::stdout().flush().unwrap();
    let mut key = String::new();
    io::stdin().read_line(&mut key).unwrap();
    let key = key.trim();

    print!("Enter new value: ");
    io::stdout().flush().unwrap();
    let mut val_str = String::new();
    io::stdin().read_line(&mut val_str).unwrap();

    if let Ok(val) = val_str.trim().parse::<f64>() {
        if m.thresholds.contains_key(key) {
            m.thresholds.insert(key.to_string(), val);
            println!("Threshold updated successfully.");
            m.logger.log_cat("CONFIG", &format!("Threshold '{}' set to {}", key, val));
        } else {
            println!("Error: Invalid threshold key: {}", key);
        }
    } else {
        println!("Error: Invalid number format.");
    }
}