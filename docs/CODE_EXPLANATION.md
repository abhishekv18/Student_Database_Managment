# Code Explanation

This document explains every Java file in the project, in the order the program uses them. Read it alongside the code.

**The big picture:**

```
Main  →  StudentManagementUI  →  InputValidator     (checks the form)
                              →  StudentDAO  →  DatabaseConnection  →  MySQL
                                     ↑
                                  Student  (carries one student's data between all of them)
```

| File | One-line job |
|---|---|
| `Main.java` | Starts the program |
| `Student.java` | Holds the data of one student |
| `DatabaseConnection.java` | Opens a connection to MySQL |
| `StudentDAO.java` | Runs all the SQL queries |
| `InputValidator.java` | Checks the text typed by the user |
| `StudentManagementUI.java` | Draws the window and reacts to button clicks |

---

## 1. `Main.java`: the starting point

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> startApplication());
}
```

**What it does:** `main` is the first method Java runs.

**Why `invokeLater`?** Swing is *not thread-safe*: all Swing components should be created and changed by one special thread, the **Event Dispatch Thread (EDT)**. `SwingUtilities.invokeLater(...)` says "please run this code on the EDT as soon as possible".

**What is `() -> startApplication()`?** It is a **lambda expression**, a short way of writing "a piece of code to run later". Here it means "when you are ready, call `startApplication()`".

```java
private static void startApplication() {
    StudentManagementUI window = new StudentManagementUI();
    window.setVisible(true);
    window.loadAllStudents();
}
```

1. `new StudentManagementUI()` creates the window object. Its constructor builds every panel, field and button.
2. `setVisible(true)` shows the window on the screen.
3. `loadAllStudents()` reads all students from MySQL and puts them in the table. If MySQL is not available, a friendly error message appears instead.

**Called by:** the Java Virtual Machine (JVM) when you click **Run**.

---

## 2. `Student.java`: the model class

### Classes and objects

A **class** is a blueprint. An **object** is one thing built from that blueprint.
`Student` is the class. `new Student(101, "Riya", "BCA", "riya@x.com", 88.5)` creates one object: one particular student.

### Fields and encapsulation

```java
private int rollNumber;
private String fullName;
private String course;
private String email;
private double marks;
```

These five **fields** match the five columns of the `students` table.

They are `private`, so no other class can change them directly. Other classes can only *read* them through **getters**. Hiding data like this and controlling access through methods is called **encapsulation**. Because there are no setters, a `Student` cannot be changed after it is created, which keeps the data safe and predictable.

### Constructor and the `this` keyword

```java
public Student(int rollNumber, String fullName, String course, String email, double marks) {
    this.rollNumber = rollNumber;
    ...
}
```

A **constructor** runs automatically when `new Student(...)` is called. It fills in the fields.

The parameter `rollNumber` has the same name as the field `rollNumber`. **`this.rollNumber`** means "the field of *this* object". The plain `rollNumber` means the parameter. So `this.rollNumber = rollNumber;` copies the parameter into the field.

### Getters

```java
public int getRollNumber() { return rollNumber; }
```

A getter simply returns a field's value. For example, `StudentDAO` calls `student.getFullName()` to put the name into the SQL query.

**Used by:** `StudentDAO` (to build SQL and to create students from database rows) and `StudentManagementUI` (to fill the table and the form).

---

## 3. `DatabaseConnection.java`: connecting to MySQL

### Why a separate class?

Every DAO method needs a connection. Putting the connection code in **one** place means the URL, username and password are read in only one place. If they change, nothing else changes.

### The settings file

```java
private static final String CONFIG_FILE = "db.properties";
```

`static final` makes this a **constant**: one value shared by the class that never changes.

The real password is stored in `db.properties`, not in the code. That file is ignored by Git (`.gitignore`), so the password is never uploaded. The committed file `db.properties.example` shows the format.

### `loadSettings()`

```java
Properties settings = new Properties();
try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
    settings.load(input);
} catch (IOException e) {
    throw new SQLException("Could not read db.properties ...");
}
```

- `Properties` is a built-in Java class that reads `key=value` lines from a file.
- `FileInputStream` opens the file for reading.
- **try-with-resources:** the part in brackets after `try` is a *resource*. Java **closes it automatically** when the block ends, even if an error happens.
- If the file is missing, an `IOException` occurs. We turn it into an `SQLException` with a clear message, so the UI only has to handle one kind of error for "the database could not be used".

### `getConnection()`

```java
String url = settings.getProperty("db.url");
String username = settings.getProperty("db.username");
String password = settings.getProperty("db.password", "");
...
return DriverManager.getConnection(url, username, password);
```

- `getProperty("db.url")` returns the value written after `db.url=` in the file.
- The `""` in `getProperty("db.password", "")` is a default value: an empty password if the line is missing.
- **`DriverManager.getConnection(...)`** asks the JDBC driver (MySQL Connector/J) to open a network connection to MySQL. It returns a **`Connection`** object.
- If MySQL is not running, or the password is wrong, it throws an **`SQLException`**.

The URL `jdbc:mysql://localhost:3306/student_management` means: use JDBC, with the MySQL driver, on this computer (`localhost`), on port `3306`, using the database `student_management`.

**Called by:** every method in `StudentDAO`.

---

## 4. `StudentDAO.java`: all database operations

**DAO** means **Data Access Object**: a class whose only job is to talk to the database. The UI never writes SQL. It just calls methods like `studentDAO.addStudent(student)`. This *separation of responsibilities* makes each file easier to understand.

### CRUD

The four basic database operations are called **CRUD**:

| Letter | Meaning | SQL | DAO method |
|---|---|---|---|
| C | Create | `INSERT` | `addStudent()` |
| R | Read | `SELECT` | `getAllStudents()`, `getStudentByRollNumber()` |
| U | Update | `UPDATE` | `updateStudent()` |
| D | Delete | `DELETE` | `deleteStudent()` |

### Important JDBC objects

| Object | What it is |
|---|---|
| `Connection` | An open link to the MySQL database |
| `PreparedStatement` | A SQL query with `?` placeholders that are filled in safely |
| `ResultSet` | The rows returned by a `SELECT` query, read one row at a time |

### `addStudent(Student student)`

```java
String sql = "INSERT INTO students (roll_number, full_name, course, email, marks) VALUES (?, ?, ?, ?, ?)";

try (Connection connection = DatabaseConnection.getConnection();
     PreparedStatement statement = connection.prepareStatement(sql)) {

    statement.setInt(1, student.getRollNumber());
    statement.setString(2, student.getFullName());
    statement.setString(3, student.getCourse());
    statement.setString(4, student.getEmail());
    statement.setDouble(5, student.getMarks());

    int rowsInserted = statement.executeUpdate();
    return rowsInserted > 0;
}
```

Step by step:

1. **The SQL text** has five `?` placeholders instead of real values.
2. **try-with-resources** opens the `Connection` and the `PreparedStatement`, and **closes both automatically** at the end. This prevents "too many connections" problems.
3. **`setInt(1, ...)`, `setString(2, ...)`, …** fill the 1st, 2nd, … `?` with real values. JDBC sends the values *separately* from the SQL text, so a name like `Robert'); DROP TABLE students;--` is stored as plain text and can never run as SQL. This protection is against **SQL injection**.
4. **`executeUpdate()`** runs the `INSERT` and returns how many rows were changed (1 on success).
5. `return rowsInserted > 0;` returns `true` if a row was added.

If the roll number already exists, MySQL refuses because `roll_number` is the **PRIMARY KEY**. The driver throws an `SQLException` with error code **1062**, which the UI turns into "A student with this roll number already exists".

The method says `throws SQLException`, so it does **not** catch errors itself. It passes them to the caller (the UI), which decides what message to show.

### `getAllStudents()`

```java
String sql = "SELECT roll_number, full_name, course, email, marks FROM students ORDER BY roll_number";
ArrayList<Student> students = new ArrayList<>();

try (Connection connection = DatabaseConnection.getConnection();
     PreparedStatement statement = connection.prepareStatement(sql);
     ResultSet resultSet = statement.executeQuery()) {

    while (resultSet.next()) {
        students.add(createStudentFromRow(resultSet));
    }
}
return students;
```

- **`executeQuery()`** is used for `SELECT`. It returns a **`ResultSet`**.
- A `ResultSet` works like a cursor that starts *before* the first row. **`resultSet.next()`** moves to the next row and returns `false` when there are no more rows, so the `while` loop visits every row.
- **`ArrayList<Student>`** is a list that can grow, holding `Student` objects. `<Student>` means "this list may only contain Students".
- If the table is empty, the loop never runs and an **empty list** is returned (not `null`).

### `getStudentByRollNumber(int rollNumber)`

```java
String sql = "SELECT ... FROM students WHERE roll_number = ?";
statement.setInt(1, rollNumber);
try (ResultSet resultSet = statement.executeQuery()) {
    if (resultSet.next()) {
        return createStudentFromRow(resultSet);
    }
}
return null;
```

Because `roll_number` is the primary key, there can be at most one matching row. So an `if` is used instead of a `while`. If no row is found, the method returns **`null`**, meaning "no student".

### `updateStudent(Student student)`

```java
UPDATE students SET full_name = ?, course = ?, email = ?, marks = ? WHERE roll_number = ?
```

- Only name, course, email and marks are changed. The **roll number is used in `WHERE`** to choose which row to update, so it never changes.
- Note the order: the roll number is the **5th** `?`, so it uses `setInt(5, ...)`.
- `executeUpdate()` returns 0 if no student had that roll number (for example, if someone deleted it), so the method returns `false`.

### `deleteStudent(int rollNumber)`

```java
DELETE FROM students WHERE roll_number = ?
```

Deletes the one matching row. It returns `true` if a row was deleted.

### `createStudentFromRow(ResultSet resultSet)`, a private helper

```java
int rollNumber = resultSet.getInt("roll_number");
String fullName = resultSet.getString("full_name");
...
return new Student(rollNumber, fullName, course, email, marks);
```

This reads the current row's columns **by name** and builds a `Student` object. Both `getAllStudents()` and `getStudentByRollNumber()` use it, so this code is not written twice. It is `private` because only `StudentDAO` needs it.

### `executeQuery()` versus `executeUpdate()`

| Method | Used for | Returns |
|---|---|---|
| `executeQuery()` | `SELECT` (reading) | a `ResultSet` with the rows |
| `executeUpdate()` | `INSERT`, `UPDATE`, `DELETE` (changing) | an `int`: the number of rows changed |

---

## 5. `InputValidator.java`: checking user input

All methods are **`static`**, so they are called on the class itself, for example `InputValidator.isValidMarks("85")`, without creating an object. That makes sense because they don't store any data.

| Method | Returns `true` when… |
|---|---|
| `isEmpty(text)` | the text is `null`, `""` or only spaces |
| `isTooLong(text, max)` | the text has more than `max` characters |
| `isValidRollNumber(text)` | the text is a whole number greater than 0 |
| `isValidMarks(text)` | the text is a number from 0 to 100 |
| `isValidEmail(email)` | the email is empty (it's optional), or looks like `a@b.c` |

### How the number checks work

```java
try {
    int rollNumber = Integer.parseInt(text.trim());
    return rollNumber > 0;
} catch (NumberFormatException e) {
    return false;
}
```

`Integer.parseInt("101")` converts text to the number 101. If the text isn't a whole number (`"abc"`, `"10.5"`, or a number too big for an `int`), Java throws a **`NumberFormatException`**. We **catch** it and return `false`. `Double.parseDouble` works the same way for marks.

`trim()` removes spaces at the start and end, so `" 101 "` is accepted.

### Why a separate file?

These rules don't need the window or the database, so they can be tested easily with **JUnit** (`InputValidatorTest.java`).

**Called by:** `StudentManagementUI.isFormValid()` and `StudentManagementUI.searchStudent()`.

---

## 6. `StudentManagementUI.java`: the window and the button actions

```java
public class StudentManagementUI extends JFrame {
```

**`extends JFrame`** means our class *is a* window. It **inherits** all of `JFrame`'s abilities (`setTitle`, `setSize`, `setVisible`, …). This is **inheritance**.

The file is divided into four parts.

### Part 1: Building the interface

**Constants.** Colors (`NAVY`, `RED`, …), fonts and maximum text lengths are `private static final` constants at the top, so the look can be changed in one place.

**Fields.** The text fields, buttons, table and status label are stored as fields, so every method in the class can use them.

```java
private ArrayList<Student> displayedStudents = new ArrayList<>();
private int selectedRollNumber = -1;
private StudentDAO studentDAO = new StudentDAO();
```

- `displayedStudents`: the students currently in the table, in the same order as the rows.
- `selectedRollNumber`: which student is selected. **`-1` means "none"** (real roll numbers are always greater than 0).
- `studentDAO`: the object used for all database work.

**The constructor** sets the title, size, minimum size and close behaviour, then builds the layout:

```
┌──────────────────────── Header (NORTH) ────────────────────────┐
├───────── Form (WEST) ─────────┬──────── Table (CENTER) ────────┤
├──────────────────────── Status bar (SOUTH) ────────────────────┤
```

**Layout managers** decide where components go, so we never need to type pixel positions:

| Layout | Used for |
|---|---|
| `BorderLayout` | The main areas: NORTH, SOUTH, WEST, CENTER |
| `GridBagLayout` | The form: labels in one column, text fields in another, lined up neatly |
| `GridLayout(2, 2, 10, 10)` | The four main buttons, all the same size, with 10px gaps |
| `FlowLayout` | The search bar, with items placed side by side |

When the window is resized, the table (CENTER) grows and the form keeps its width.

**Methods that build parts of the window:**

- `createHeaderPanel()`: the navy title bar.
- `createFormPanel()`: the "Student Details" card. It calls `addFormRow()` five times, once per field.
- `addFormRow(panel, row, labelText, field)`: puts a label in column 0 and a text field in column 1 using `GridBagConstraints` (`gridx` = column, `gridy` = row, `weightx = 1.0` + `fill = HORIZONTAL` makes the field stretch).
- `createTablePanel()`: the search bar and the table.
- `createStatusPanel()`: the message bar at the bottom.
- `createTextField()`, `createButton()`, `createSectionTitle()`, `createCardBorder()`: small helpers so every field and button looks the same, without repeating the styling code.

**The table model:**

```java
tableModel = new DefaultTableModel(columnNames, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
};
```

A `JTable` displays the data, and a **`DefaultTableModel`** stores the rows. `0` means "start with zero rows". The `{ ... }` block creates a small *anonymous subclass* that **overrides** `isCellEditable()` to return `false`, so users can't type into the table cells. Changes go through the form, where they are validated.

The table is placed inside a **`JScrollPane`**, which adds scroll bars when there are many rows.

### Part 2: Connecting buttons to methods (event handling)

```java
addButton.addActionListener(e -> addStudent());
```

- Clicking a button creates an **event**. An **`ActionListener`** is code that runs when that event happens.
- `e -> addStudent()` is a **lambda**, a short form of writing a full `ActionListener` class. `e` is the event object (we don't need it). The arrow means "when clicked, run `addStudent()`".
- Without a lambda, the same thing would look like this:

```java
addButton.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        addStudent();
    }
});
```

All six buttons are connected this way. Pressing **Enter** in the search box is connected to `searchStudent()` too.

```java
studentTable.getSelectionModel().addListSelectionListener(e -> fillFormFromSelectedRow());
```

This listener runs whenever the selected row changes. It copies the selected student into the form.

### Part 3: What each button does

#### `addStudent()`: Add Student button

1. If a student is selected (`selectedRollNumber != -1`), tell the user to click Clear first, because they are probably editing, not adding.
2. `isFormValid()`: stop if anything is wrong.
3. `readStudentFromForm()`: build a `Student` object from the text fields.
4. `studentDAO.addStudent(student)` inserts it into MySQL.
5. On success: `clearForm()` (which reloads the table), then a success status and dialog.
6. On `SQLException`: `showDatabaseError(...)` shows a friendly message. **No success message is shown.**

#### `updateStudent()`: Update Student button

1. If nothing is selected, show "Please select a student from the table first."
2. Validate the form and build the `Student`. The roll number field is **locked** (`setEditable(false)`) while a student is selected, so it still holds the original roll number.
3. `studentDAO.updateStudent(student)`. If it returns `false`, the student no longer exists.
4. Success → clear the form, reload, show a message. Error → friendly message.

#### `deleteStudent()`: Delete Student button

1. If nothing is selected, show a warning.
2. `JOptionPane.showConfirmDialog(...)` asks *"Are you sure…?"* with **Yes / No** buttons.
3. If the answer is not `YES_OPTION`, it shows "Delete cancelled." and **does nothing else**.
4. Otherwise `studentDAO.deleteStudent(rollNumber)`, then clear and reload.

#### `searchStudent()`: Search button (and Enter key)

1. Check that the search box is not empty and contains a valid roll number.
2. `studentDAO.getStudentByRollNumber(rollNumber)`.
3. `null` → "No student found with roll number …".
4. Otherwise, put that one student into a new `ArrayList` and show it with `showStudentsInTable()`.

#### `showAllStudents()`: Show All button

Empties the search box and calls `loadAllStudents()`.

#### `clearForm()`: Clear button

Empties every text field, unlocks the roll number field, sets `selectedRollNumber = -1`, clears the table selection and reloads all students. It is also called after every successful add, update and delete.

### Part 4: Helper methods

#### `loadAllStudents()`

Calls `studentDAO.getAllStudents()` and shows the result. If the list is empty, the status bar says *"No student records found…"*. It is `public` because `Main` calls it at start-up.

#### `showStudentsInTable(ArrayList<Student> students)`

```java
tableModel.setRowCount(0);
displayedStudents = students;
for (Student student : students) {
    Object[] row = { student.getRollNumber(), student.getFullName(), ... };
    tableModel.addRow(row);
}
```

`setRowCount(0)` removes the old rows. The **for-each loop** adds one table row per student. `Object[]` is used because one row holds different types (int, String, double).

#### `fillFormFromSelectedRow()`

```java
int selectedRow = studentTable.getSelectedRow();
if (selectedRow == -1) { return; }
Student student = displayedStudents.get(selectedRow);
```

`getSelectedRow()` returns `-1` when no row is selected (for example, while the table is being refreshed), so the method stops. Otherwise it takes the matching `Student` from `displayedStudents`, which uses the same order as the rows, copies the details into the text fields and **locks the roll number field**.

#### `isFormValid()`

Reads and trims every field, then checks in this order: required fields → roll number → marks → email → length. On the first problem it shows a warning and returns `false`. If everything is fine it returns `true`.

#### `readStudentFromForm()`

Converts the text into the right types: `Integer.parseInt` for the roll number and `Double.parseDouble` for marks. It then creates a `Student`. It is only called **after** `isFormValid()`, so the conversion can't fail.

#### `showDatabaseError(String action, SQLException e)`

1. Prints the technical details (message, error code, SQL state) to the terminal with `System.err.println`, for the developer. The password is never part of these messages.
2. Chooses a friendly message from the **MySQL error code**:

| Code / state | Meaning | Message shown |
|---|---|---|
| 1062 | Duplicate primary key | "A student with this roll number already exists." |
| 1045 | Access denied | "MySQL rejected the username or password…" |
| 1049 | Unknown database | "The database 'student_management' does not exist…" |
| 1146 | Table doesn't exist | "The table 'students' does not exist…" |
| SQL state `08…` | Connection failure | "Cannot connect to MySQL. Make sure the MySQL server is running…" |
| anything else | Other SQL error | "Database error: " + the driver's message |

#### `setStatus`, `showInfo`, `showWarning`, `showError`

Small wrappers around `statusLabel.setText(...)` and `JOptionPane.showMessageDialog(...)`, so every message looks consistent and the code stays short.

---

## 7. Key Concepts Summary

| Concept | Where it is used |
|---|---|
| **Class & object** | `Student` class; `new Student(...)` objects; `new StudentDAO()` |
| **Constructor** | `Student(...)` fills fields; `StudentManagementUI()` builds the window |
| **Encapsulation** | `private` fields in `Student` with public getters |
| **Getters** | `getRollNumber()`, `getFullName()`, … |
| **`this` keyword** | `this.rollNumber = rollNumber;` in the `Student` constructor |
| **Inheritance** | `StudentManagementUI extends JFrame` |
| **Method overriding** | `isCellEditable()` in the table model |
| **Static methods** | `InputValidator` methods, `DatabaseConnection.getConnection()` |
| **ArrayList** | `ArrayList<Student>` returned by `getAllStudents()` |
| **Event listener / lambda** | `addButton.addActionListener(e -> addStudent())` |
| **JDBC `Connection`** | `DatabaseConnection.getConnection()` |
| **`PreparedStatement`** | Every query in `StudentDAO`, with `?` placeholders |
| **`ResultSet`** | Reading rows in `getAllStudents()` and `getStudentByRollNumber()` |
| **try-with-resources** | Every DAO method and `loadSettings()`, which close resources automatically |
| **try-catch** | The UI catches `SQLException`; the validator catches `NumberFormatException` |
| **Primary key** | `roll_number INT PRIMARY KEY` keeps roll numbers unique |

---

## 8. Complete Add Student Flow (Step by Step)

Suppose the user types **101, Riya Sharma, BCA, riya@example.com, 88.5** and clicks **Add Student**.

1. **Swing event.** The click creates an `ActionEvent`. The listener registered in `connectButtonsToActions()` runs `addStudent()`.
2. **Selection check.** `selectedRollNumber` is `-1`, so we continue.
3. **Validation.** `isFormValid()` reads the five text fields:
   - No required field is empty → OK
   - `InputValidator.isValidRollNumber("101")` → `true`
   - `InputValidator.isValidMarks("88.5")` → `true`
   - `InputValidator.isValidEmail("riya@example.com")` → `true`
   - The lengths are fine → returns `true`.
4. **Create the object.** `readStudentFromForm()` returns `new Student(101, "Riya Sharma", "BCA", "riya@example.com", 88.5)`.
5. **Call the DAO.** `studentDAO.addStudent(student)`.
6. **Get a connection.** `DatabaseConnection.getConnection()` reads `db.properties` and calls `DriverManager.getConnection(url, user, password)`.
7. **Prepare the SQL.** `connection.prepareStatement("INSERT INTO students (...) VALUES (?, ?, ?, ?, ?)")`. The five `?` are filled with `setInt` / `setString` / `setDouble`.
8. **MySQL runs it.** `executeUpdate()` sends the query. MySQL checks the primary key, saves the row and replies "1 row affected".
9. **Back in Java.** `rowsInserted` is 1, so the method returns `true`. try-with-resources **closes** the statement and the connection.
10. **Update the UI.** `clearForm()` empties the fields and calls `loadAllStudents()`, which runs a `SELECT` and refreshes the table. The new student now appears. The status bar and a dialog say "Student added successfully."

**If roll number 101 already exists:** at step 8 MySQL rejects the row. At step 9 an `SQLException` (code 1062) is thrown, skips the rest of `addStudent()`'s `try` block and lands in `catch`. `showDatabaseError()` shows *"A student with this roll number already exists."* The form is **not** cleared, so the user can fix the number.

**If MySQL is not running:** at step 6 `DriverManager.getConnection` throws an `SQLException` with SQL state `08S01`. The user sees *"Cannot connect to MySQL…"*.

---

## 9. Why Database Work Runs on the UI Thread

Every query runs directly inside the button's click handler, on the Event Dispatch Thread. For a small local database this takes only a few milliseconds and keeps the code very simple.

In a larger application, a slow query would freeze the window until it finished. The standard fix is Swing's `SwingWorker` class, which runs the query in the background and updates the table when it's done. That is a possible future improvement.
