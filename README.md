# SkillSwap – Student Skill Exchange Platform

A **Java Swing desktop application** that connects students who want to teach and learn from each other. Register, list the skills you can teach and the ones you want to learn, get matched with peers, and exchange learning requests — all backed by **Firebase** for authentication and real-time cloud storage.

---

## ✨ Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | **User Registration & Login** | Email / password auth via Firebase Authentication (REST API). |
| 2 | **Student Profile** | Name, email, and college stored in Firebase Realtime Database. |
| 3 | **Skills to Teach** | Add / remove skills you are ready to teach. |
| 4 | **Skills to Learn** | Add / remove skills you want to pick up. |
| 5 | **Skill Matching** | Automatic matching — find students whose teach-list overlaps your learn-list. |
| 6 | **Learning Requests** | Send, accept, or reject skill-exchange requests. |
| 7 | **Dashboard** | At-a-glance view of your profile, matches, and request status. |
| 8 | **Dark Theme** | Modern, professional dark UI powered by [FlatLaf](https://www.formdev.com/flatlaf/). |

---

## 🏗️ Architecture

```
src/main/java/com/skillswap/
├── SkillSwapApp.java            # Entry point
├── config/
│   └── FirebaseConfig.java      # Firebase initialisation (reads .env)
├── model/
│   ├── User.java                # Student domain model
│   └── SkillRequest.java        # Learning-request domain model
├── service/                     # Business logic & Firebase CRUD
├── controller/                  # Mediates between views and services
├── view/
│   ├── MainFrame.java           # Top-level window (CardLayout router)
│   ├── components/              # Reusable styled Swing widgets
│   └── panels/                  # Screen-level panels (login, dashboard…)
└── util/
    ├── UIConstants.java          # Colour palette, fonts, dimensions
    └── ValidationUtil.java       # Input validation helpers
```

The project follows **MVC principles**:

* **Model** – `User`, `SkillRequest` (POJOs / domain objects).
* **View** – Swing panels & reusable components.
* **Controller** – Orchestrates user actions, calls services, updates views.
* **Service** – Firebase CRUD, authentication, and matching logic.

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| GUI | Java Swing + FlatLaf dark theme |
| Auth | Firebase Authentication (REST API) |
| Database | Firebase Realtime Database |
| Build | Apache Maven |
| Secrets | dotenv-java (`.env` file) |

---

## 🚀 Getting Started

### Prerequisites

* **Java 21+** (`java --version`)
* **Apache Maven 3.9+** (`mvn --version`)
* A **Firebase project** with:
  * Authentication → Email / Password sign-in enabled
  * Realtime Database created

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/skillswap.git
cd skillswap
```

### 2. Configure Firebase credentials

1. In the Firebase Console → **Project Settings → Service accounts → Generate new private key**.
2. Save the downloaded JSON file as `firebase-service-account.json` in the project root.
3. Copy the example env file and fill in your values:

```bash
cp .env.example .env
```

```env
FIREBASE_SERVICE_ACCOUNT_PATH=./firebase-service-account.json
FIREBASE_DATABASE_URL=https://<project-id>.firebaseio.com
FIREBASE_API_KEY=<your-web-api-key>
```

> ⚠️ **Never commit `.env` or `firebase-service-account.json`** — they are git-ignored by default.

### 3. Build & run

```bash
# Compile and package
mvn clean package

# Run the application
java -jar target/skillswap-1.0.0.jar
```

Or run directly during development:

```bash
mvn compile exec:java -Dexec.mainClass="com.skillswap.SkillSwapApp"
```

---

## 📂 Firebase Database Structure

```
skillswap-db/
├── users/
│   └── {uid}/
│       ├── uid: string
│       ├── name: string
│       ├── email: string
│       ├── college: string
│       ├── skillsToTeach: [string]
│       └── skillsToLearn: [string]
└── requests/
    └── {requestId}/
        ├── requestId: string
        ├── senderUid: string
        ├── senderName: string
        ├── receiverUid: string
        ├── receiverName: string
        ├── skill: string
        ├── status: "PENDING" | "ACCEPTED" | "REJECTED"
        └── timestamp: long
```

---

## 📦 Key Dependencies

| Dependency | Purpose |
|-----------|---------|
| `firebase-admin` | Server-side Firebase SDK (Auth + RTDB) |
| `flatlaf` | Modern flat dark look-and-feel for Swing |
| `gson` | JSON serialisation / deserialisation |
| `dotenv-java` | Load `.env` variables without hardcoding |
| `slf4j-simple` | Logging backend required by Firebase SDK |

---

## 🤝 Contributing

1. Fork the repo.
2. Create a feature branch: `git checkout -b feature/amazing-feature`.
3. Commit your changes: `git commit -m "Add amazing feature"`.
4. Push to the branch: `git push origin feature/amazing-feature`.
5. Open a Pull Request.

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
