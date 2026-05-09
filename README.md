# 📚 Inkwell Bookstore — Full-Stack E-Commerce Platform

Inkwell is a sophisticated online bookstore platform featuring a robust Spring Boot backend and a dynamic, responsive frontend. It provides a seamless shopping experience with real-time book searching, category filtering, user authentication, and a synchronized shopping cart system.

---

## 🚀 Features

### **Frontend (Inkwell UI)**
- **Modern Design**: Premium aesthetics with smooth micro-animations and a responsive layout.
- **Dynamic Theming**: Support for both **Dark** and **Light** modes.
- **Book Discovery**: 
  - Real-time search functionality.
  - Category-based filtering (Fiction, Sci-Fi, Mystery, etc.).
  - Featured and Trending book carousels.
- **Shopping Experience**:
  - Persistent Shopping Cart (synchronized with the backend for logged-in users).
  - Quick "Buy Now" and "Add to Cart" actions.
  - Wishlist functionality (LocalStorage based).
- **User Authentication**: Secure Login and Registration flows using JWT.
- **Detailed Book Pages**: Rich information including ratings, descriptions, and related books.

### **Backend (Inkwell API)**
- **RESTful API**: Clean and documented endpoints for all operations.
- **Security**: Stateless JWT-based authentication and authorization.
- **Data Persistence**: Spring Data JPA with an H2 in-memory database (pre-seeded with 120 books).
- **Swagger/OpenAPI**: Interactive API documentation for easy testing.
- **Seeded Content**: Automatic data seeding on startup to ensure a ready-to-use catalog.

---

## 🛠️ Tech Stack

### **Frontend**
- **Core**: Vanilla HTML5, CSS3, JavaScript (ES6+).
- **Dev Server**: Vite with TanStack Start (TypeScript).
- **Icons**: Lucide React / SVG.
- **Styling**: Modern CSS with CSS Variables for theming.

### **Backend**
- **Framework**: Spring Boot 3.2.5 (Java 17).
- **Security**: Spring Security & JJWT.
- **Database**: H2 (In-memory).
- **Documentation**: SpringDoc OpenAPI (Swagger UI).
- **Build Tool**: Maven.

---

## 📁 Project Structure

```text
Book Store/
├── bookstore-backend/          # Spring Boot Application
│   ├── src/main/java/          # Java source code
│   ├── src/main/resources/     # Application config & properties
│   └── pom.xml                 # Maven dependencies
├── crimson-pages-main/         # Frontend Project
│   ├── public/bookstore/       # Core Frontend (HTML/JS/CSS)
│   ├── src/                    # Vite/TanStack Start Wrapper
│   └── package.json            # Node.js dependencies
└── README.md                   # Project documentation
```

---

## ⚡ Quick Start

### **1. Prerequisites**
- **Java 17** or higher.
- **Node.js** (v18+) and **npm**.
- **Maven** (or use the provided binary in the backend).

### **2. Run the Backend**
```bash
cd bookstore-backend
# If mvn is in your PATH:
mvn spring-boot:run
# Or use the local maven binary:
.\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
The API will be available at `http://localhost:8080`.
The Swagger UI can be accessed at `http://localhost:8080/swagger-ui.html`.

### **3. Run the Frontend**
```bash
cd crimson-pages-main
npm install
npm run dev
```
The application will be available at `http://localhost:8081` (redirects to the bookstore UI).

---

## 📝 API Endpoints Summary

- **Auth**: `/api/auth/login`, `/api/auth/register`
- **Books**: `/api/books` (GET, POST), `/api/books/{id}` (GET)
- **Cart**: `/api/cart` (GET, POST, DELETE)
- **Orders**: `/api/orders/checkout` (POST)
- **Reviews**: `/api/reviews` (POST)

---

## 🤝 Contributing

1. Fork the project.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

Developed with ❤️ for the Book Lovers community.
