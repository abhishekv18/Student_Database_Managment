# Student Database Management System

A simple desktop application, written in **Java Swing** and connected to **MySQL** through **JDBC**, for managing student records. You can **add, view, search, update and delete** students from a clean graphical window.

It is a beginner-level college project. The code is deliberately kept simple so that every line can be read, understood and explained in a viva.

---

## 1. Features

| Feature | What it does |
|---|---|
| **Add Student** | Validates the form and inserts a new student into MySQL. Duplicate roll numbers are rejected with a clear message. |
| **View Students** | Shows every student in a table when the app starts, and refreshes the table after every change. |
| **Search Student** | Finds a student by roll number. **Show All** brings the full list back. |
| **Update Student** | Click a row, edit the details in the form, then click **Update Student**. The roll number cannot be changed. |
| **Delete Student** | Click a row, then click **Delete Student**. The app asks for confirmation before deleting. |
| **Clear** | Empties the form, removes the selection and shows the full list again. |
| **Validation** | Checks for empty required fields, invalid roll numbers, marks outside 0–100, badly formatted emails and text that is too long. |
| **Friendly errors** | Explains problems such as "MySQL is not running" or "database does not exist" in plain English. |

## 2. Technology Stack

| Technology | Used for |
|---|---|
| **Java 17** | Application logic and object-oriented programming |
| **Java Swing** | The graphical user interface (window, form, table, buttons) |
| **JDBC** | Connecting Java to MySQL and running SQL queries |
| **MySQL 8** | Storing student records permanently |
| **MySQL Connector/J 8.4** | The JDBC driver for MySQL (downloaded automatically by Maven) |
| **Maven** (`pom.xml`) | Downloads Connector/J and JUnit automatically |
| **JUnit 5** | A few simple tests for the validation rules and the Student class |
| **VS Code** | Development environment |

## 3. Project Folder Structure

```
Student_Managment/
│
├── src/
│   ├── main/java/studentmanagement/
│   │   ├── Main.java                  Starts the application
│   │   ├── Student.java               One student: fields, constructor, getters
│   │   ├── DatabaseConnection.java    Reads db.properties and opens a MySQL connection
│   │   ├── StudentDAO.java            All SQL: add, view, search, update, delete
│   │   ├── InputValidator.java        Checks the text typed into the form
│   │   └── StudentManagementUI.java   The Swing window and the button actions
│   │
│   └── test/java/studentmanagement/
│       ├── InputValidatorTest.java    JUnit tests for the validation rules
│       └── StudentTest.java           JUnit test for the Student class
│
├── docs/
│   ├── CODE_EXPLANATION.md            Line-by-line style explanation of every file
│   ├── INTERVIEW_QUESTIONS.md         Viva questions with answers
│   └── FLOW_DIAGRAM.md                Architecture and flow diagrams
│
├── database.sql                       Creates the database and the table
├── db.properties.example              Example database settings (copy it to db.properties)
├── pom.xml                            Maven settings and dependencies
├── README.md                          This file
└── .gitignore                         Files Git should ignore (including db.properties)
```

> `InputValidator.java` is one small extra file beyond the five main classes. It keeps the validation rules out of the UI so they can be tested with JUnit without a database.

## 4. Prerequisites

You need:

1. **JDK 17 or newer** (Java Development Kit)
2. **MySQL Server 8** (MySQL Workbench is optional but helpful)
3. **VS Code** with the **Extension Pack for Java**
4. An internet connection the first time you open the project (so Maven can download Connector/J)

You do **not** need to install the Maven command-line tool. VS Code's Java extension reads `pom.xml` and downloads the libraries by itself.

## 5. Installing and Checking the JDK

1. Download a JDK 17 (or newer), for example from <https://adoptium.net> or the Microsoft Build of OpenJDK.
2. Install it with the default options.
3. Open a **new** terminal (Command Prompt or PowerShell) and run:

```bash
java -version
```

```bash
javac -version
```

Both commands should print version 17 or higher. If you see "not recognized", add the JDK's `bin` folder to your `PATH`, or reinstall and tick "Set JAVA_HOME / add to PATH".

## 6. Setting Up VS Code for Java

1. Install VS Code from <https://code.visualstudio.com>.
2. Open the **Extensions** view (`Ctrl+Shift+X`).
3. Search for **Extension Pack for Java** (by Microsoft) and click **Install**. It includes:
   - Language Support for Java (by Red Hat)
   - Debugger for Java
   - Test Runner for Java
   - Maven for Java
   - Project Manager for Java
4. Restart VS Code.

## 7. Installing and Starting MySQL

1. Download **MySQL Installer** (Windows) or MySQL Community Server from <https://dev.mysql.com/downloads/>.
2. Choose **MySQL Server** (and optionally **MySQL Workbench**).
3. During setup, choose a **root password** and remember it.
4. Make sure the server is running:
   - **Windows:** press `Win + R`, type `services.msc`, find **MySQL80**, and check that the Status is **Running**. If it isn't, right-click it and choose **Start**.
   - Or, in an administrator terminal: `net start MySQL80`
   - **macOS / Linux:** `sudo systemctl start mysql` or use the MySQL preference pane.

Check that you can log in:

```bash
mysql -u root -p
```

Type your password when asked. Type `exit` to leave.

> If `mysql` is "not recognized" on Windows, use the full path, for example
> `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p`, or use MySQL Workbench instead.

## 8. Creating the Database (run `database.sql`)

The application **does not** create the database by itself. You do it once, manually.

**Option A: terminal.** Open a terminal **in the project folder** and run:

```bash
mysql -u root -p < database.sql
```

**Option B: MySQL Workbench**

1. Open MySQL Workbench and connect to your local server.
2. Go to **File → Open SQL Script…** and choose `database.sql`.
3. Click the **lightning bolt** (Execute) button.

**Check that it worked:**

```sql
USE student_management;
SHOW TABLES;
DESCRIBE students;
```

You should see the `students` table with the columns `roll_number`, `full_name`, `course`, `email` and `marks`.

> Running the script a second time gives "database exists". That is fine: the database is already created.
> To start completely fresh, run `DROP DATABASE student_management;` yourself first. **This deletes all records.**

## 9. Configuring Your Database Credentials

Your MySQL password is **never** written in the Java code. It is read from a file called `db.properties`.

1. In the project folder, make a copy of `db.properties.example` and name the copy **`db.properties`**.
2. Open `db.properties` and enter your own details:

```properties
db.url=jdbc:mysql://localhost:3306/student_management
db.username=root
db.password=your_real_mysql_password
```

3. Save the file.

Notes:

- `db.properties` must be in the **project's root folder** (next to `pom.xml`), because the program looks for it in the folder it is started from.
- `db.properties` is listed in `.gitignore`, so Git will not commit your password.
- If your MySQL server uses a different port, change `3306` in `db.url`.

## 10. Running the Application in VS Code

1. Open VS Code → **File → Open Folder…** → choose the project folder (the one that contains `pom.xml`).
2. Wait for the Java extension to finish loading. The status bar shows "Java: Ready" or a 👍. The first time, Maven downloads MySQL Connector/J, which can take a minute.
3. Open `src/main/java/studentmanagement/Main.java`.
4. Click **Run** above the `main` method (or press `F5`).
5. The **Student Database Management System** window opens and loads the students from MySQL.

Another way: open the **Run and Debug** panel (`Ctrl+Shift+D`), choose **Run Student Management System** (defined in `.vscode/launch.json`) and click the green ▶.

> **Don't use the "Run Code" ▶ button from the *Code Runner* extension.** It compiles only the open file, so you get errors like `cannot find symbol: class StudentManagementUI`. Use the **Run** link above `main`, F5, or the Run and Debug panel instead.

**Running the tests:** open `InputValidatorTest.java` and click **Run Test** above the class, or use the **Testing** (flask) icon in the left sidebar.

**Optional (Maven command line).** If you have installed Maven yourself:

```bash
mvn test
```

```bash
mvn compile exec:java
```

## 11. How to Use Each Feature

| Task | Steps |
|---|---|
| **Add a student** | Type Roll Number, Full Name, Course, Email (optional) and Marks → click **Add Student**. |
| **View all students** | The table fills automatically when the app starts. Click **Show All** at any time to refresh it. |
| **Search** | Type a roll number in **Search by Roll No** → click **Search** (or press Enter). Click **Show All** to see everyone again. |
| **Update** | Click a row in the table → its details appear in the form (the Roll Number is locked) → change the details → click **Update Student**. |
| **Delete** | Click a row → click **Delete Student** → click **Yes** to confirm (**No** cancels). |
| **Clear** | Click **Clear** to empty the form, unlock the Roll Number field and show all students. |

Validation rules:

- Roll Number, Full Name, Course and Marks are **required**. Email is optional.
- Roll Number must be a whole number greater than 0.
- Marks must be a number from 0 to 100 (decimals like 72.5 are allowed).
- Email, if given, must look like `name@example.com`.
- Full Name and Course: at most 100 characters. Email: at most 150 characters.

## 12. Common Errors and Solutions

| Message / problem | Cause | Solution |
|---|---|---|
| "Could not read db.properties…" | The settings file does not exist, or it is not in the folder the app was started from | Copy `db.properties.example` to `db.properties` in the project root folder. Open the project root folder (not `src`) in VS Code. |
| "Cannot connect to MySQL…" | The MySQL server is not running, or the port in `db.url` is wrong | Start the **MySQL80** service. Check `db.url` (default port 3306). |
| "MySQL rejected the username or password" | Wrong `db.username` or `db.password` | Fix the values in `db.properties`. Test them with `mysql -u root -p`. |
| "The database 'student_management' does not exist" | `database.sql` was not run | Run `database.sql` (section 8). |
| "The table 'students' does not exist" | The database exists but the table was never created | Run the `CREATE TABLE` part of `database.sql`. |
| "A student with this roll number already exists" | Roll number is the primary key and must be unique | Use a different roll number. |
| "Public Key Retrieval is not allowed" (in the terminal) | A MySQL 8 authentication setting on some installations | Add `?allowPublicKeyRetrieval=true&useSSL=false` to the end of `db.url` (local development only). |
| `ClassNotFoundException` / "No suitable driver found" | Connector/J was not downloaded | Make sure the folder with `pom.xml` is open in VS Code. Run **Java: Clean Java Language Server Workspace** from the Command Palette (`Ctrl+Shift+P`). |
| Red errors everywhere in VS Code | The Java extension is still loading, or no JDK was found | Wait for "Java: Ready". Check `java -version`. Install the Extension Pack for Java. |

Technical details of every database error are also printed in the VS Code **terminal / debug console**. Your password is never printed.

## 13. How the Program Works (Execution Flow)

Example: **Add Student**

```
User fills in the form and clicks "Add Student"
  → Swing calls the button's action listener → addStudent() in StudentManagementUI
  → isFormValid() checks the fields using InputValidator
  → readStudentFromForm() creates a Student object
  → studentDAO.addStudent(student) is called
  → StudentDAO gets a Connection from DatabaseConnection.getConnection()
  → A PreparedStatement runs: INSERT INTO students (...) VALUES (?, ?, ?, ?, ?)
  → MySQL saves the row (or reports an error such as a duplicate roll number)
  → Java gets the result, or catches the SQLException
  → The UI shows a success / error message and reloads the table
```

Search, Update and Delete follow the same pattern: **UI → validation → StudentDAO → DatabaseConnection → JDBC → MySQL → back to the UI**.

See `docs/FLOW_DIAGRAM.md` for diagrams and `docs/CODE_EXPLANATION.md` for a detailed walkthrough.

## 14. Limitations of This Beginner Version

- **Single user, local database.** Built for one person on one computer.
- **No login system.** Anyone who can open the app can change the records.
- **Database work runs on the UI thread.** This is fine for a small local database. In a bigger app, slow queries would freeze the window for a moment. They could later be moved to a background thread with `SwingWorker`.
- **A new connection for every operation.** Simple and safe, but larger apps reuse connections with a *connection pool*.
- **Search by roll number only.** No searching by name or course, and no sorting or paging.
- **Simple email check.** It checks the basic shape only (`something@something.something`).
- **The 0–100 marks rule is checked only in Java.** MySQL itself would accept other values if inserted directly with SQL.
- **The password is stored in a plain text file** (`db.properties`). This is acceptable for local development but not for production.
