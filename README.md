# 📈 StockTrader Pro — Stock Trading Platform

A full-stack stock trading simulation platform built with **Spring Boot**, **MongoDB Atlas**, and vanilla **HTML/CSS/JS**. Users can register, buy/sell stocks, track their portfolio performance, and view transaction history — all with simulated real-time price updates.

---

## Live Project Link - https://codealpha-stock-trading-platform.onrender.com

## 🚀 Features

- 📊 **Live Market Dashboard** — 10 stocks with prices that auto-update every 10 seconds
- 💰 **Buy & Sell Stocks** — with balance validation and instant feedback
- 💼 **Portfolio Tracker** — view current holdings, average buy price, and gain/loss per stock
- 📋 **Transaction History** — full log of all buy/sell operations
- 🧾 **P&L Summary** — total portfolio value, cash balance, and overall profit/loss
- 💾 **MongoDB Persistence** — all data stored in MongoDB Atlas
- 🔐 **User Accounts** — register and login by username, session persists via localStorage

---

## 🛠️ Tech Stack

| Layer      | Technology              |
|------------|-------------------------|
| Backend    | Java 17, Spring Boot 3.2 |
| Database   | MongoDB Atlas           |
| Frontend   | HTML, CSS, JavaScript   |
| Deployment | Render (Docker)         |

---

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/stocktrading/
│   │   ├── model/           # Stock, User, Transaction (OOP entities)
│   │   ├── repository/      # MongoDB repositories
│   │   ├── service/         # Business logic (trading, price simulation)
│   │   └── controller/      # REST API endpoints
│   └── resources/
│       ├── static/          # Frontend (index.html, CSS, JS)
│       └── application.properties
├── Dockerfile
├── render.yaml
└── pom.xml
```

---

## ⚙️ REST API Endpoints

### Stocks
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stocks` | Get all stocks |
| GET | `/api/stocks/{symbol}` | Get stock by symbol |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register new user |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users/username/{username}` | Get user by username |

### Trading
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/trade/buy` | Buy shares |
| POST | `/api/trade/sell` | Sell shares |
| GET | `/api/trade/portfolio/{userId}` | Get portfolio summary |
| GET | `/api/trade/transactions/{userId}` | Get transaction history |

---

## 🖥️ Running Locally

### Prerequisites
- Java 17+
- Maven 3.x
- MongoDB Atlas account

### Steps

1. **Clone the repository:**
```bash
git clone https://github.com/trynafind-sumanyu/CodeAlpha_Stock_Trading_Platform.git
cd CodeAlpha_Stock_Trading_Platform
```

2. **Configure MongoDB:**
   Edit `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=your_mongodb_connection_string
spring.data.mongodb.database=stock_trading
```

3. **Run the app:**
```bash
mvn spring-boot:run
```

4. **Open in browser:**
```
http://localhost:8080
```

---

## 🐳 Running with Docker

```bash
docker build -t stock-trading .
docker run -p 8080:8080 -e SPRING_DATA_MONGODB_URI=your_connection_string stock-trading
```

---

## 🌐 Deployment (Render)

1. Push code to GitHub
2. Create a new **Web Service** on [Render](https://render.com)
3. Select **Docker** as the runtime
4. Add environment variable:
    - `SPRING_DATA_MONGODB_URI` = your MongoDB Atlas connection string
5. Deploy!

---

## 📦 OOP Design

| Class | Responsibility |
|-------|---------------|
| `Stock` | Represents a stock with price history, high/low, sector |
| `User` | Manages balance, portfolio holdings, and avg buy prices |
| `Transaction` | Records every buy/sell with timestamp and amount |
| `StockService` | Seeds initial stocks, simulates price changes on schedule |
| `TradingService` | Handles buy/sell logic, portfolio P&L calculation |
| `UserService` | User creation and retrieval |

---

## 🎓 Built For

**CodeAlpha Internship — Task 2: Stock Trading Platform**

---

## 📸 Screenshots

> Register/Login → Browse live market → Buy stocks → Track portfolio performance → View history

---

## 👤 Author

**Sumanyu Rajput**  
GitHub: [@trynafind-sumanyu](https://github.com/trynafind-sumanyu)
