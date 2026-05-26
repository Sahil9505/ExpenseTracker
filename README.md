# Premium Fintech Expense Tracker

This project is a modern, responsive, and fully functional full-stack Expense Tracker built for academic presentations and personal finance management. It features a premium "Dark Mode" SaaS-style dashboard.

## Tech Stack

**Backend**:
- Spring Boot 3.2.x
- MySQL Database
- Spring Security (JWT Authentication)
- Spring Data JPA (Hibernate)

**Frontend**:
- React 18 (Vite)
- Tailwind CSS
- React Router DOM
- Recharts (Data Visualization)
- Axios (API Client)

---

## Features Implemented

1. **Authentication System**
   - User Registration & Login
   - Secure JWT token-based authentication
   - Protected routing on the frontend
2. **Dashboard Overview**
   - Real-time statistics (Total Expenses, Monthly, Average Per Day)
   - Dynamic charts using Recharts (Pie Chart & Bar Chart)
   - Budget progress bars
3. **Expense Management**
   - Full CRUD: Add, Edit, Delete, and View expenses
   - Backend search and filtering capabilities
   - Pagination and sorting
4. **Categories, Budgets, & Bills**
   - Custom categories tracking
   - Monthly budget allocation (Backend APIs ready)
   - Upcoming bills reminders (Backend APIs ready)
5. **Modern UI/UX**
   - Premium deep-dark blue color palette
   - Glassmorphism effects and modern borders
   - Completely responsive layout (Mobile Sidebar Drawer)

---

## Setup Instructions

### 1. Database Setup
Ensure you have MySQL installed and running. Create the database:
```sql
CREATE DATABASE expense_tracker;
```

### 2. Backend Setup
Navigate to the backend directory:
```bash
cd backend
```
Update `src/main/resources/application.properties` with your MySQL credentials:
```properties
spring.datasource.username=root
spring.datasource.password=yourpassword
```
Run the Spring Boot server:
```bash
mvn spring-boot:run
```
*(Note: Because of `spring.jpa.hibernate.ddl-auto=update`, all necessary tables (`users`, `expenses`, `categories`, `budgets`, `bills`) will be created automatically).*

### 3. Frontend Setup
Navigate to the frontend directory:
```bash
cd frontend
```
Install dependencies:
```bash
npm install
```
Start the Vite development server:
```bash
npm run dev
```

The application will be accessible at `http://localhost:5173`.

---

## Step-by-Step Implementation Guide (For Viva/Interview)

1. **Architecture Overview**: The app uses a classic client-server model. The Spring Boot backend acts as a RESTful API serving JSON data. The React frontend consumes these APIs.
2. **Security**: We used Spring Security with a custom `JwtAuthFilter`. When a user logs in, the backend verifies credentials and issues a JWT. The React app stores this JWT in `localStorage` and sends it in the `Authorization: Bearer <token>` header for all subsequent requests via an Axios Interceptor.
3. **Database Relationships**: The `User` entity has a One-to-Many relationship with `Expense`, `Budget`, `ExpenseCategory`, and `Bill`. This ensures data isolation (users only see their own data).
4. **Data Visualization**: We utilized `Recharts`. The `ExpenseService` calculates aggregated data (e.g., category-wise totals using custom JPQL queries in the repository) and passes it to the frontend, which injects the data directly into the `<PieChart>` and `<BarChart>` components.
5. **UI Framework**: Tailwind CSS was heavily utilized using a custom slate and indigo palette to achieve the modern, dark "Fintech" look without writing heavy custom CSS files.
