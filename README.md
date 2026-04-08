# Hyundai Dealer Management System (DMS)

A comprehensive full-stack dealer management system built with Spring Boot and Angular, designed for automotive dealerships to manage inventory, sales, service, and test drives.

## 🚀 Features

### Inventory Management
- Vehicle stock tracking with real-time status updates
- GRN (Goods Receipt Note) management
- Centralized vehicle model management
- Vehicle aging analysis
- Branch-wise inventory distribution

### Sales Management
- Lead management with stage tracking
- Customer relationship management
- Booking management with vehicle allocation
- Payment processing and tracking
- Sales reporting and analytics

### Service Management
- Service booking and scheduling
- Service history tracking
- Parts and labor management
- Service reminders

### Test Drive Management
- Test drive fleet management
- Test drive booking and scheduling
- Fleet maintenance tracking
- Customer license verification

### User Management
- Role-based access control (RBAC)
- Multi-branch support
- User authentication with account locking
- Branch context switching

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.x
- **Language**: Java 21
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with QueryDSL
- **Security**: Spring Security with JWT
- **Database Migration**: Liquibase
- **API Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven

### Frontend
- **Framework**: Angular 17
- **UI Library**: Angular Material
- **State Management**: NgRx
- **HTTP Client**: Angular HttpClient
- **Build Tool**: Angular CLI

### DevOps
- **Containerization**: Docker & Docker Compose
- **Database**: MySQL in Docker
- **Reverse Proxy**: Nginx (optional)

## 📋 Prerequisites

- Java 21 or higher
- Node.js 18+ and npm
- Docker and Docker Compose
- Maven 3.8+
- Git

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/pksanjayy/Domain.git
cd Domain
```

### 2. Start the Database
```bash
docker-compose up -d
```

This will start MySQL on port 3306 with:
- Database: `dms_db`
- Username: `dms_user`
- Password: `dms_password`
- Root Password: `root_password`

### 3. Start the Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 4. Start the Frontend
```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:4200`

### 5. Access the Application
- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Default Credentials
- **Username**: `admin`
- **Password**: `admin123`

## 📁 Project Structure

```
Domain/
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/hyundai/dms/
│   │   │   │       ├── common/           # Common utilities
│   │   │   │       ├── config/           # Configuration classes
│   │   │   │       ├── exception/        # Exception handling
│   │   │   │       ├── module/           # Business modules
│   │   │   │       │   ├── inventory/    # Inventory management
│   │   │   │       │   ├── sales/        # Sales management
│   │   │   │       │   ├── service/      # Service management
│   │   │   │       │   ├── testdrive/    # Test drive management
│   │   │   │       │   └── user/         # User management
│   │   │   │       └── security/         # Security configuration
│   │   │   └── resources/
│   │   │       ├── db/changelog/         # Liquibase migrations
│   │   │       └── application.yml       # Application config
│   │   └── test/                         # Unit tests
│   └── pom.xml                           # Maven dependencies
│
├── frontend/                   # Angular frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/                     # Core services
│   │   │   ├── layout/                   # Layout components
│   │   │   ├── modules/                  # Feature modules
│   │   │   │   ├── admin/                # Admin module
│   │   │   │   ├── auth/                 # Authentication
│   │   │   │   ├── inventory/            # Inventory module
│   │   │   │   ├── sales/                # Sales module
│   │   │   │   ├── service/              # Service module
│   │   │   │   ├── testdrive/            # Test drive module
│   │   │   │   └── reports/              # Reports module
│   │   │   └── shared/                   # Shared components
│   │   └── assets/                       # Static assets
│   ├── angular.json                      # Angular config
│   └── package.json                      # npm dependencies
│
├── docker-compose.yml          # Docker compose config
├── .gitignore                  # Git ignore rules
└── README.md                   # This file
```

## 🔧 Configuration

### Backend Configuration
Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dms_db
    username: dms_user
    password: dms_password
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours
```

### Frontend Configuration
Edit `frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## 📚 API Documentation

Once the backend is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## 🗄️ Database Schema

The application uses Liquibase for database migrations. All migrations are located in:
```
backend/src/main/resources/db/changelog/
```

Key tables:
- `users` - User accounts
- `branches` - Dealership branches
- `vehicles` - Vehicle inventory
- `vehicle_models` - Centralized vehicle models
- `customers` - Customer information
- `leads` - Sales leads
- `bookings` - Vehicle bookings
- `test_drive_fleet` - Test drive vehicles
- `test_drive_bookings` - Test drive bookings
- `service_bookings` - Service appointments

## 🔐 Security

- JWT-based authentication
- Role-based access control (RBAC)
- Account locking after failed login attempts
- Password encryption using BCrypt
- CORS configuration for frontend access

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📦 Building for Production

### Backend
```bash
cd backend
mvn clean package -DskipTests
```
The JAR file will be in `backend/target/dms-1.0.0-SNAPSHOT.jar`

### Frontend
```bash
cd frontend
npm run build
```
The build artifacts will be in `frontend/dist/`

## 🐳 Docker Deployment

Build and run with Docker Compose:
```bash
docker-compose up --build
```

## 📝 Recent Updates

### Latest Features (v1.0.0)
- ✅ Centralized Vehicle Model Management System
- ✅ Searchable vehicle model and customer dropdowns
- ✅ Card-based form layouts for all sales modules
- ✅ Enhanced user authentication with account locking
- ✅ Improved error handling and validation

### Bug Fixes
- ✅ Fixed form population issues in leads, bookings, and test drive modules
- ✅ Fixed search functionality across all modules
- ✅ Fixed vehicle dropdown not populating in booking edit mode
- ✅ Enhanced customer selector to handle async loading
- ✅ Fixed test drive fleet form to properly display brand and model

See [COMPLETE_BUG_FIXES.md](COMPLETE_BUG_FIXES.md) for detailed information.

## 📖 Documentation

- [Vehicle Model Centralization Guide](VEHICLE_MODEL_CENTRALIZATION.md)
- [Vehicle Model Dropdown Implementation](VEHICLE_MODEL_DROPDOWN_IMPLEMENTATION.md)
- [Bug Fixes Summary](BUG_FIXES_SUMMARY.md)
- [Complete Bug Fixes](COMPLETE_BUG_FIXES.md)
- [Final Implementation Summary](FINAL_IMPLEMENTATION_SUMMARY.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is proprietary software developed for Hyundai dealerships.

## 👥 Authors

- **Sanjay PK** - [GitHub](https://github.com/pksanjayy)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Angular team for the powerful frontend framework
- All contributors who helped improve this project

## 📞 Support

For support, email support@example.com or open an issue in the repository.

---

**Note**: This is a comprehensive dealer management system. Make sure to configure all environment variables and database connections before deploying to production.
