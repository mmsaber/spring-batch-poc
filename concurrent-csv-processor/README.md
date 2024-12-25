# 📄 Concurrent CSV Processor

🚀 **Concurrent CSV Processor** is a Spring Boot-based microservice designed to handle large-scale CSV file processing efficiently using multi-threading and batch configurations. The service leverages Spring Batch for parallel processing, ensuring scalability and performance.

---

## 🛠️ Project Structure

This project consists of three microservices working in conjunction:

1. **CSV Processor** - Core service handling CSV file import/export operations.
2. **Log Service** - Tracks processing logs and errors.
3. **Settings Service** - Manages configuration settings and processing parameters.
4. **Test File** - you can find the [custom_2017_2020.csv](src/main/resources/custom_2017_2020.csv) for testing purposes
---

## 📂 Folder Structure
```
concurrent-csv-processor/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/concurrentcsvprocessor/
│   │   │   ├── config/        # Batch and Thread Configurations
│   │   │   ├── controller/    # REST Controllers
│   │   │   ├── model/         # Data Models and DTOs
│   │   │   ├── service/       # Core Services
│   │   │   └── repository/    # Data Repositories
│   │   └── resources/         # YAML Configuration Files
│   └── test/                  # Unit and Integration Tests
│
├── pom.xml                    # Project Build File
└── README.md                  # Project Documentation
```

---

## 🚧 Requirements

- 🧑‍💻 **Java 17** or later
- 🐘 **PostgreSQL** for database management (Optional: H2 for local profile)
- ☕ **Maven** for dependency management
- 🐳 **Docker** (Optional - for containerized deployment)

---

## 🔧 Setup & Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd concurrent-csv-processor
```

### 2. Configure Profiles
- **Local Profile (H2 Database):**
  ```yaml
  spring:
    profiles:
      active: local
  ```
- **Dev Profile (PostgreSQL):**
  ```yaml
  spring:
    profiles:
      active: dev
  ```
The database is created automatically on startup using Liquibase migrations.

---
### 3. Run Other Services

please make sure that export-import-sum-api, export-import-divide-api and export-import-multiply-api are running

---
## 🚀 Running the Application

### 1. Run Directly
```bash
./mvnw spring-boot:run
```

### 2. Using Docker
```bash
docker build -t csv-processor .
docker run -p 8080:8080 csv-processor
```

---

## 📡 API Endpoints

### 1. Upload CSV File
```bash
curl --location 'localhost:8080/upload' \
--form 'file=@"/D:/workspaces/workspaces-lab/spring-batch-poc/concurrent-csv-processor/src/main/resources/custom_2017_2020.csv"' \
--form 'operation="multiply"'
```

### 2. Download Processed CSV
```bash
curl --location --request GET 'localhost:8080/ml0r2KAz9Ortqkje' \
--form 'file=@"/D:/workspaces-lab/spring-batch-poc/concurrent-csv-processor/src/main/resources/custom_2017_2020.csv"' \
--form 'operation="sum"'
```

---

## ⚙️ Configuration

- **Batch Size:** Adjust the batch size by modifying `application.yml`:
```yaml
batch:
  size: 500
```
- **Thread Pool:**
```yaml
thread:
  pool:
    size: 10
```

---

## 🧪 Testing
Run unit tests with:
```bash
./mvnw test
```

---

## 🏗️ Future Enhancements
- ✅ Support for additional file formats (e.g., XML, JSON)
- 📊 Real-time processing dashboard
- 📅 Scheduling for automatic batch jobs

---

## 🛟 Support
For issues, please create an [issue](https://github.com/example/concurrent-csv-processor/issues) or contact the development team.

---

🎉 **Happy Processing!**

