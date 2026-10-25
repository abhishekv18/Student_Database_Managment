# Viva and Interview Questions

All answers describe **this project's actual code**.

---

## A. Project Overview

**1. What is your project about?**
It is a desktop application that manages student records. Using a Java Swing window, the user can add, view, search (by roll number), update and delete students. The data is stored permanently in a MySQL database through JDBC.

**2. What details are stored for each student?**
Roll number (`int`, primary key), full name (`String`), course (`String`), email (`String`, optional) and marks (`double`, 0–100). They are stored in the `students` table of the `student_management` database.

**3. How many Java classes are there, and what does each do?**
Six:
- `Main` starts the app.
- `Student` holds one student's data.
- `DatabaseConnection` opens a MySQL connection.
- `StudentDAO` contains all the SQL.
- `InputValidator` checks the form input.
- `StudentManagementUI` builds the window and handles button clicks.

**4. Why did you split the code into several classes instead of one file?**
Each class has **one responsibility**. The UI doesn't contain SQL, and the DAO doesn't contain Swing code. This makes the code easier to read, test and change. For example, the database code could change without touching the window.

## B. Technology Choices

**5. Why Java?**
It is object-oriented, platform-independent, taught widely in colleges, and includes Swing for desktop GUIs and JDBC for databases in the standard library.

**6. Why Swing and not JavaFX?**
Swing is part of the standard JDK, so nothing extra has to be installed. It provides everything this project needs: `JFrame`, `JTable`, `JTextField`, `JButton` and `JOptionPane`.

**7. Why MySQL?**
It is free, popular and reliable, and it stores data permanently. Records survive after the program closes, unlike an `ArrayList` in memory.

**8. What is MySQL Connector/J and why is it needed?**
It is MySQL's **JDBC driver**. JDBC is only a set of standard interfaces. The driver is the library that actually knows how to talk to a MySQL server. Maven downloads it from the dependency in `pom.xml`.

**9. What is Maven and what does `pom.xml` do here?**
Maven is a build tool. `pom.xml` names the project, sets Java version 17, and lists the dependencies (Connector/J, plus JUnit for tests) so they are downloaded automatically instead of being added by hand.

## C. Java OOP Concepts

**10. Which OOP concepts does the project use?**
- **Classes and objects:** `Student`, `new StudentDAO()`
- **Encapsulation:** private fields with getters in `Student`
- **Inheritance:** `StudentManagementUI extends JFrame`
- **Method overriding:** `isCellEditable()` in the table model
- **Constructors:** in `Student` and `StudentManagementUI`

**11. What is encapsulation, and where is it used?**
Encapsulation means keeping data private and accessing it only through methods. In `Student`, all five fields are `private`, and other classes read them with getters such as `getRollNumber()`. There are no setters, so a `Student` cannot be changed after creation.

**12. What does the `this` keyword do in the `Student` constructor?**
In `this.rollNumber = rollNumber;`, `this.rollNumber` is the object's field and `rollNumber` is the constructor parameter. `this` tells them apart when they have the same name.

**13. Why are the methods in `InputValidator` static?**
They don't store any data. They only check the text they are given. Static methods can be called directly on the class, for example `InputValidator.isValidMarks("85")`, without creating an object.

**14. Why does `StudentManagementUI` extend `JFrame`?**
So that it *is* a window and inherits methods like `setTitle()`, `setSize()` and `setVisible()`.

## D. Swing and Event Handling

**15. What is an event listener?**
Code that runs when something happens, such as a button click. Each button gets one, for example `addButton.addActionListener(e -> addStudent());`.

**16. What does `e -> addStudent()` mean?**
It is a **lambda expression**, a short way of writing an `ActionListener` whose `actionPerformed(ActionEvent e)` method calls `addStudent()`. `e` is the event object.

**17. How does selecting a table row fill the form?**
A `ListSelectionListener` on the table's selection model calls `fillFormFromSelectedRow()`. That method gets the row index with `getSelectedRow()`, takes the matching `Student` from the `displayedStudents` list, copies its values into the text fields and locks the roll number field.

**18. Why is `SwingUtilities.invokeLater()` used in `Main`?**
Swing is not thread-safe. All UI work should happen on the **Event Dispatch Thread**. `invokeLater()` schedules the window creation on that thread.

**19. Which layout managers did you use and why?**
- `BorderLayout` for the main areas (header, form, table, status bar)
- `GridBagLayout` to align the form's labels and fields
- `GridLayout` to make the four buttons the same size
- `FlowLayout` for the search bar

Layout managers resize sensibly and avoid hard-coded pixel positions.

**20. Why can't the user type directly in the table?**
The `DefaultTableModel` overrides `isCellEditable()` to return `false`. All edits go through the form, where they are validated before reaching the database.

## E. JDBC and MySQL

**21. What are the main JDBC steps in this project?**
1. Get a `Connection` with `DriverManager.getConnection(url, user, password)`.
2. Create a `PreparedStatement` with `?` placeholders.
3. Set the values with `setInt`, `setString` and `setDouble`.
4. Run it with `executeQuery()` (SELECT) or `executeUpdate()` (INSERT/UPDATE/DELETE).
5. Read the `ResultSet` if there is one.
6. Close everything. Try-with-resources does this automatically.

**22. What is the difference between `executeQuery()` and `executeUpdate()`?**
`executeQuery()` is for `SELECT` and returns a `ResultSet` of rows. `executeUpdate()` is for `INSERT`, `UPDATE` and `DELETE`, and returns an `int`: the number of rows changed. The DAO uses that number to return `true` or `false`.

**23. What is a `ResultSet` and how do you read it?**
It holds the rows returned by a `SELECT`. `resultSet.next()` moves to the next row and returns `false` when there are no more. Inside the loop, `getInt("roll_number")`, `getString("full_name")` and so on read the column values. `createStudentFromRow()` turns each row into a `Student`.

**24. Why `PreparedStatement` instead of `Statement`?**
The values are sent separately from the SQL text, which **prevents SQL injection**: input like `' OR '1'='1` is treated as plain data. It also handles quotes in names (like O'Brien) correctly, and keeps the code cleaner than joining strings.

**25. Where are the database URL, username and password stored?**
In `db.properties`, which `DatabaseConnection` reads using `java.util.Properties`. The file is in `.gitignore`, so the password is never committed. `db.properties.example` shows the format.

**26. Does the application create the database?**
No. The user runs `database.sql` once in MySQL. This avoids the app accidentally creating or deleting databases.

## F. SQL and CRUD

**27. What is CRUD? Map it to your methods.**
Create, Read, Update, Delete:
- Create → `addStudent()` (`INSERT`)
- Read → `getAllStudents()` and `getStudentByRollNumber()` (`SELECT`)
- Update → `updateStudent()` (`UPDATE`)
- Delete → `deleteStudent()` (`DELETE`)

**28. What is a primary key, and why is `roll_number` the primary key?**
A primary key uniquely identifies each row. It cannot be duplicated or NULL. Each student has a unique roll number, so MySQL itself rejects duplicates.

**29. How are duplicate roll numbers handled?**
MySQL rejects the `INSERT` and the driver throws an `SQLException` with error code **1062**. `showDatabaseError()` detects this code and shows "A student with this roll number already exists."

**30. Write the SQL used to update a student.**
`UPDATE students SET full_name = ?, course = ?, email = ?, marks = ? WHERE roll_number = ?`
The roll number appears only in `WHERE`, so it identifies the row and is never changed.

**31. Why does `getAllStudents()` use `ORDER BY roll_number`?**
So the table always shows students in a predictable order, sorted by roll number.

## G. Exception Handling and Validation

**32. How are database errors handled?**
DAO methods declare `throws SQLException` and don't catch errors themselves. The UI methods wrap each call in `try-catch`. In the `catch`, `showDatabaseError()` prints technical details to the terminal and shows a friendly dialog based on the error code. A success message is only shown if the operation really succeeded.

**33. What is try-with-resources, and why is it used?**
`try (Connection c = ...; PreparedStatement s = ...) { ... }` closes `c` and `s` automatically when the block ends, even if an exception occurs. This prevents connection leaks without writing `finally` blocks.

**34. What validation is done before saving?**
In `isFormValid()`:
- Roll number, name, course and marks must not be empty.
- The roll number must be a positive integer.
- Marks must be 0–100.
- The email, if given, must look like `a@b.c`.
- Name and course can be at most 100 characters, email at most 150 (matching the `VARCHAR` sizes).

**35. How does `isValidRollNumber()` detect non-numeric input?**
It calls `Integer.parseInt()` inside `try`. If the text is not a whole number, a `NumberFormatException` is thrown, caught, and the method returns `false`.

**36. What happens if MySQL isn't running when the app starts?**
`DriverManager.getConnection()` throws an `SQLException` with SQL state `08S01`. `loadAllStudents()` catches it, leaves the table empty and shows "Cannot connect to MySQL. Make sure the MySQL server is running…".

**37. How does the app stop you from updating or deleting without selecting a student?**
`selectedRollNumber` is `-1` when nothing is selected. `updateStudent()` and `deleteStudent()` check this first and show "Please select a student from the table first."

**38. What happens if the user clicks No in the delete confirmation?**
`showConfirmDialog()` returns something other than `YES_OPTION`, so the method shows "Delete cancelled." and returns without calling the DAO.

## H. Testing

**39. How did you test the project?**
JUnit 5 tests (`InputValidatorTest`, `StudentTest`) check the validation rules and the `Student` class without a database. The CRUD features are tested manually with MySQL running: add, add a duplicate, search existing and missing roll numbers, update, delete (Yes and No), and invalid input. Connection failures are tested by stopping MySQL or using a wrong password.

## I. Security and Improvements

**40. What security measures does the project have?**
- `PreparedStatement` everywhere against SQL injection
- Credentials kept out of the code and out of Git
- Input validation before saving
- Error messages that never show the password

**41. What are the limitations of the project?**
- No login system
- Search by roll number only
- Database calls run on the UI thread
- A new connection is opened for each operation
- The marks range is enforced in Java but not by a database constraint
- The password is stored as plain text in a local file

**42. How could the project be improved?**
- Add a login screen.
- Search by name or course.
- Add table sorting and filtering (`TableRowSorter`).
- Run queries in the background with `SwingWorker`.
- Use a connection pool.
- Add a `CHECK (marks BETWEEN 0 AND 100)` constraint in MySQL.
- Export the records to CSV or PDF.
- Add more automated tests that use a test database.
