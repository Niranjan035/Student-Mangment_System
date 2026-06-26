# Student Management System
> Java Swing + JDBC + MySQL — No Spring Boot, No Maven, No Gradle

---

## Project Structure

```
StudentManagementSystem/
├── lib/
│   └── mysql-connector-j-9.x.x.jar   ← Download and place here
├── src/
│   ├── Main/
│   │   └── Main.java                  ← Application entry point
│   ├── AppGUI/
│   │   └── AppGUI.java                ← Full GUI + CRUD logic
│   ├── dbConnect/
│   │   └── DBConnect.java             ← MySQL connection factory
│   └── Table/
│       └── Table.java                 ← JTable setup and data loading
└── README.md
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 11 or higher |
| MySQL Server | 5.7 or higher |
| MySQL Connector/J | 8.x or 9.x |
| VS Code | Any recent version |

---

## Step 1 — Set Up the Database

Open MySQL Workbench or the MySQL command line and run:

```sql
CREATE DATABASE IF NOT EXISTS studentdata;

USE studentdata;

CREATE TABLE students (
    Student_ID VARCHAR(10),
    first_name VARCHAR(50),
    last_name  VARCHAR(50),
    major      VARCHAR(50),
    Phone      VARCHAR(15),
    GPA        DECIMAL(3,1),
    DOB        DATE
);
```

---

## Step 2 — Download MySQL Connector/J

1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Download the **Platform Independent** ZIP
3. Extract it and copy `mysql-connector-j-x.x.x.jar` into the `lib/` folder

---

## Step 3 — Update Database Credentials

Open `src/dbConnect/DBConnect.java` and update:

```java
private static final String USER     = "root";      // Your MySQL username
private static final String PASSWORD = "root";      // Your MySQL password
```

---

## Step 4 — Compile

Open a terminal in the `StudentManagementSystem/` root folder.

**On Windows:**
```cmd
javac -cp "lib\mysql-connector-j-9.x.x.jar" -d out -sourcepath src src\Main\Main.java src\AppGUI\AppGUI.java src\dbConnect\DBConnect.java src\Table\Table.java
```

**On macOS / Linux:**
```bash
javac -cp "lib/mysql-connector-j-9.x.x.jar" -d out -sourcepath src \
  src/Main/Main.java src/AppGUI/AppGUI.java \
  src/dbConnect/DBConnect.java src/Table/Table.java
```

> Replace `mysql-connector-j-9.x.x.jar` with your actual JAR filename.

---

## Step 5 — Run

**On Windows:**
```cmd
java -cp "out;lib\mysql-connector-j-9.x.x.jar" Main.Main
```

**On macOS / Linux:**
```bash
java -cp "out:lib/mysql-connector-j-9.x.x.jar" Main.Main
```

---

## Features

| Feature | Description |
|---------|-------------|
| **Add** | Insert a new student record |
| **Update** | Modify an existing student by ID |
| **Delete** | Remove a student (with confirmation) |
| **Search** | Find by ID or partial name match |
| **Clear** | Reset all form fields |
| **Refresh** | Reload the full table from MySQL |
| **Row Click** | Click any row to auto-fill the form |

---

## Validation Rules

| Field | Rule |
|-------|------|
| All fields | Cannot be empty |
| Student ID | Maximum 10 characters |
| Phone | Digits only (no dashes or spaces) |
| GPA | Numeric value between 0.0 and 4.0 |
| DOB | Must be in `YYYY-MM-DD` format |

---

## Troubleshooting

**"MySQL JDBC Driver not found"**
→ The JAR is missing from `lib/` or your classpath `-cp` flag is wrong.

**"Access denied for user"**
→ Wrong username/password in `DBConnect.java`.

**"Unknown database 'studentdata'"**
→ Run the `CREATE DATABASE studentdata;` SQL above first.

**"Connection refused"**
→ MySQL server is not running. Start it from MySQL Workbench or your system services.
