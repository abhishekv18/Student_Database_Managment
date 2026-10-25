# Flow Diagrams

The diagrams use **Mermaid**. They render automatically on GitHub, and in VS Code with the *Markdown Preview Mermaid Support* extension (open the file and press `Ctrl+Shift+V`). A plain-text version follows each diagram, for printing or drawing on a board.

---

## 1. Overall Application Architecture

```mermaid
flowchart TD
    User([User]) --> UI["StudentManagementUI<br/>(Swing window, button listeners)"]
    Main["Main<br/>(starts the app)"] --> UI
    UI --> Validator["InputValidator<br/>(checks form input)"]
    UI --> DAO["StudentDAO<br/>(all SQL queries)"]
    DAO --> DBC["DatabaseConnection<br/>(reads db.properties)"]
    DBC --> JDBC["JDBC + MySQL Connector/J"]
    JDBC --> MySQL[("MySQL<br/>student_management.students")]
    Student["Student<br/>(data object)"] -.carries data between.- UI
    Student -.- DAO
```

```
            ┌────────┐
            │  Main  │  starts the program
            └───┬────┘
                ▼
 User ──► ┌──────────────────────┐ ──► ┌────────────────┐
          │ StudentManagementUI  │     │ InputValidator │  (checks input)
          │ (window + buttons)   │     └────────────────┘
          └──────────┬───────────┘
                     ▼   Student objects travel between these layers
          ┌──────────────────────┐
          │      StudentDAO      │  (INSERT, SELECT, UPDATE, DELETE)
          └──────────┬───────────┘
                     ▼
          ┌──────────────────────┐
          │  DatabaseConnection  │  (reads db.properties)
          └──────────┬───────────┘
                     ▼
          ┌──────────────────────┐
          │ JDBC (Connector/J)   │
          └──────────┬───────────┘
                     ▼
          ┌──────────────────────┐
          │ MySQL: students table│
          └──────────────────────┘
```

---

## 2. Add Student Flow

```mermaid
flowchart TD
    A[User fills form and clicks Add Student] --> B["addStudent() runs"]
    B --> C{Is a table row selected?}
    C -- Yes --> C1[Warning: click Clear first] --> Z[End]
    C -- No --> D{"isFormValid()"}
    D -- No --> D1[Show warning, e.g. marks must be 0-100] --> Z
    D -- Yes --> E["readStudentFromForm() creates Student"]
    E --> F["studentDAO.addStudent(student)"]
    F --> G["DatabaseConnection.getConnection()"]
    G --> H["PreparedStatement: INSERT INTO students ... VALUES (?, ?, ?, ?, ?)"]
    H --> I{MySQL result}
    I -- 1 row inserted --> J["clearForm() + reload table + success message"] --> Z
    I -- SQLException --> K["showDatabaseError()<br/>e.g. 1062 = duplicate roll number"] --> Z
```

```
Click "Add Student"
   │
   ├─ row selected? ── yes ──► "Click Clear first"            (stop)
   │
   ├─ form valid?  ─── no ───► show warning                   (stop)
   │
   ▼
Student object ──► StudentDAO.addStudent()
   ──► DatabaseConnection.getConnection()
   ──► INSERT INTO students (...) VALUES (?, ?, ?, ?, ?)
   ──► MySQL
          ├─ success ──► clear form, reload table, "Student added successfully."
          └─ error   ──► friendly message (duplicate / not running / ...)
```

---

## 3. Search Student Flow

```mermaid
flowchart TD
    A[User types roll number and clicks Search or presses Enter] --> B["searchStudent() runs"]
    B --> C{Search box empty or not a positive number?}
    C -- Yes --> C1[Show warning] --> Z[End]
    C -- No --> D["studentDAO.getStudentByRollNumber(roll)"]
    D --> E["SELECT ... FROM students WHERE roll_number = ?"]
    E --> F{Row found?}
    F -- Yes --> G[Show only that student in the table]
    F -- No --> H[Message: No student found with roll number X]
    E -- SQLException --> I["showDatabaseError()"]
    G --> Z
    H --> Z
    I --> Z
    G -.-> S["Show All button: clear search box, loadAllStudents()"]
```

```
Click "Search"
   ├─ invalid input ──► warning                               (stop)
   ▼
StudentDAO.getStudentByRollNumber(roll)
   ──► SELECT ... WHERE roll_number = ?
          ├─ found     ──► table shows that one student
          ├─ not found ──► "No student found with roll number X"
          └─ error     ──► friendly database message

Click "Show All" ──► clear search box ──► reload every student
```

---

## 4. Update Student Flow

```mermaid
flowchart TD
    A[User clicks a row in the table] --> B["fillFormFromSelectedRow()"]
    B --> B1[Form filled, roll number locked, selectedRollNumber set]
    B1 --> C[User edits details and clicks Update Student]
    C --> D{"selectedRollNumber == -1?"}
    D -- Yes --> D1[Warning: select a student first] --> Z[End]
    D -- No --> E{"isFormValid()"}
    E -- No --> E1[Show warning] --> Z
    E -- Yes --> F["studentDAO.updateStudent(student)"]
    F --> G["UPDATE students SET full_name=?, course=?, email=?, marks=? WHERE roll_number=?"]
    G --> H{Rows updated > 0?}
    H -- Yes --> I["clearForm() + reload + success message"] --> Z
    H -- No --> J[Error: student no longer exists] --> Z
    G -- SQLException --> K["showDatabaseError()"] --> Z
```

```
Click a table row ──► form filled, Roll Number locked
   ▼
Edit fields ──► click "Update Student"
   ├─ nothing selected ──► warning                            (stop)
   ├─ form invalid     ──► warning                            (stop)
   ▼
StudentDAO.updateStudent(student)
   ──► UPDATE students SET ... WHERE roll_number = ?
          ├─ 1 row updated ──► clear form, reload, "Student updated successfully."
          ├─ 0 rows        ──► "No student found ... may have been deleted"
          └─ error         ──► friendly database message
```

---

## 5. Delete Student Flow

```mermaid
flowchart TD
    A[User clicks a row, then Delete Student] --> B{"selectedRollNumber == -1?"}
    B -- Yes --> B1[Warning: select a student first] --> Z[End]
    B -- No --> C["Confirm dialog: Are you sure?"]
    C -- No --> C1[Status: Delete cancelled. Nothing deleted] --> Z
    C -- Yes --> D["studentDAO.deleteStudent(roll)"]
    D --> E["DELETE FROM students WHERE roll_number = ?"]
    E --> F{Rows deleted > 0?}
    F -- Yes --> G["clearForm() + reload + success message"] --> Z
    F -- No --> H[Error: already deleted, reload table] --> Z
    E -- SQLException --> I["showDatabaseError()"] --> Z
```

```
Select row ──► click "Delete Student"
   ├─ nothing selected ──► warning                            (stop)
   ▼
"Are you sure?"  ── No ──► "Delete cancelled." (nothing deleted)
   │ Yes
   ▼
StudentDAO.deleteStudent(roll)
   ──► DELETE FROM students WHERE roll_number = ?
          ├─ deleted ──► clear form, reload, "Student deleted successfully."
          └─ error   ──► friendly database message
```
