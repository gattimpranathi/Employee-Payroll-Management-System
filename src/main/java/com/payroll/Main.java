
package com.payroll;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class Main extends Application {

    private static final String EMPLOYEE_FILE = "employees.csv";
    private static final String ATTENDANCE_FILE = "attendance.csv";
    private static final String PAYROLL_FILE = "payroll_history.csv";

    private Stage stage;

    private final String navy = "#17365D";
    private final String lightBlue = "#E8EEF7";
    private final String background = "#F3F6FA";
    private final String green = "#16834A";

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Employee Payroll System");
        showLogin();
        stage.show();
    }

    // ================= LOGIN =================

    private void showLogin() {

        Label title = heading("EMPLOYEE PAYROLL SYSTEM");
        Label subtitle = new Label("ADMIN LOGIN");

        TextField username = new TextField();
        username.setPromptText("Enter Username");
        username.setPrefHeight(42);

        PasswordField password = new PasswordField();
        password.setPromptText("Enter Password");
        password.setPrefHeight(42);

        Button login = createButton("LOGIN");
        login.setPrefWidth(300);

        login.setOnAction(e -> {
            if (username.getText().equals("admin")
                    && password.getText().equals("1234")) {
                showDashboard();
            } else {
                alert("Invalid username or password");
            }
        });

        VBox card = new VBox(18,
                title, subtitle, username, password, login);

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        card.setMaxWidth(390);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-border-color: #D5DFEA;" +
                "-fx-border-radius: 15;"
        );

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 750, 550));
    }

    // ================= MODERN DASHBOARD =================

    private void showDashboard() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7FB;");

        // LEFT SIDEBAR
        VBox sidebar = new VBox(17);
        sidebar.setPrefWidth(235);
        sidebar.setPadding(new Insets(30, 18, 25, 18));
        sidebar.setStyle("-fx-background-color: #142D4E;");

        Label logo = new Label("PAYROLL");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 25px; -fx-font-weight: bold;");
        Label logoSub = new Label("MANAGEMENT SYSTEM");
        logoSub.setStyle("-fx-text-fill: #AFC4DF; -fx-font-size: 10px;");
        VBox logoBox = new VBox(5, logo, logoSub);
        logoBox.setPadding(new Insets(5, 5, 25, 5));

        Label menuTitle = new Label("MAIN MENU");
        menuTitle.setStyle("-fx-text-fill: #91A9C7; -fx-font-size: 11px; -fx-font-weight: bold;");

        Button dashboardBtn = dashboardMenuButton("Dashboard");
        Button addBtn = dashboardMenuButton("Add Employee");
        Button viewBtn = dashboardMenuButton("View Employees");
        Button attendanceBtn = dashboardMenuButton("Attendance");
        Button salaryBtn = dashboardMenuButton("Calculate Salary");
        Button slipBtn = dashboardMenuButton("Salary Slips");
        Button historyBtn = dashboardMenuButton("Payroll History");

        dashboardBtn.setStyle("-fx-background-color: #285582; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 15 0 15;");
        addBtn.setOnAction(e -> showAddEmployee());
        viewBtn.setOnAction(e -> showViewEmployees());
        attendanceBtn.setOnAction(e -> showAttendance());
        salaryBtn.setOnAction(e -> showCalculateSalary());
        slipBtn.setOnAction(e -> showSalarySlip());
        historyBtn.setOnAction(e -> showPayrollHistory());

        Region sidebarSpace = new Region();
        VBox.setVgrow(sidebarSpace, Priority.ALWAYS);
        Button logoutBtn = dashboardMenuButton("Logout");
        logoutBtn.setStyle("-fx-background-color: #A83232; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 15 0 15;");
        logoutBtn.setOnAction(e -> showLogin());

        sidebar.getChildren().addAll(logoBox, menuTitle, dashboardBtn, addBtn,
                viewBtn, attendanceBtn, salaryBtn, slipBtn, historyBtn,
                sidebarSpace, logoutBtn);
        root.setLeft(sidebar);

        // HEADER
        Label welcome = new Label("Welcome, Admin!");
        welcome.setStyle("-fx-font-size: 27px; -fx-font-weight: bold; -fx-text-fill: #17365D;");
        Label description = new Label("Manage your employees and payroll activities");
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096;");
        VBox welcomeBox = new VBox(7, welcome, description);
        Label date = new Label(LocalDate.now().toString());
        date.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #285582; -fx-background-color: #E3ECF7; -fx-padding: 12 18 12 18; -fx-background-radius: 10;");
        HBox header = new HBox(welcomeBox, date);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        HBox.setHgrow(welcomeBox, Priority.ALWAYS);

        // OVERVIEW CARDS
        Label overview = new Label("Dashboard Overview");
        overview.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #17365D;");
        GridPane cards = new GridPane();
        cards.setHgap(18);
        cards.setVgap(18);
        // Read current values from the application's CSV files
        cards.add(dashboardDataCard("TOTAL EMPLOYEES",
                String.valueOf(getEmployeeCount()),
                "Employees registered in the system", "#E8F0FE", "#2457A6"), 0, 0);
        cards.add(dashboardDataCard("PRESENT TODAY",
                String.valueOf(getTodayAttendanceCount("Present")),
                "Attendance marked for " + LocalDate.now(), "#E5F7EF", "#16834A"), 1, 0);
        cards.add(dashboardDataCard("ABSENT TODAY",
                String.valueOf(getTodayAttendanceCount("Absent")),
                "Attendance marked for " + LocalDate.now(), "#FFF3DF", "#C47A16"), 0, 1);
        cards.add(dashboardDataCard("MONTHLY PAYROLL",
                money(getCurrentMonthPayroll()),
                "Net salary total for " + YearMonth.now(), "#F1EAFE", "#7545A5"), 1, 1);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        cards.getColumnConstraints().addAll(c1, c2);

        // QUICK ACTIONS
        Label quickTitle = new Label("Quick Actions");
        quickTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #17365D;");
        Button addAction = createButton("+  Add New Employee");
        Button attendanceAction = createButton("Manage Attendance");
        Button calculateAction = createButton("Calculate Payroll");
        addAction.setPrefWidth(205);
        attendanceAction.setPrefWidth(205);
        calculateAction.setPrefWidth(205);
        addAction.setOnAction(e -> showAddEmployee());
        attendanceAction.setOnAction(e -> showAttendance());
        calculateAction.setOnAction(e -> showCalculateSalary());
        HBox actions = new HBox(15, addAction, attendanceAction, calculateAction);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(25, header, overview, cards, quickTitle, actions);
        content.setPadding(new Insets(35));
        content.setStyle("-fx-background-color: #F4F7FB;");
        root.setCenter(content);

        Scene scene = new Scene(root, 1150, 750);
        stage.setScene(scene);
        stage.setTitle("Employee Payroll System - Admin Dashboard");
        stage.centerOnScreen();
    }

    // ================= LIVE DASHBOARD DATA =================

    // Counts employee rows in employees.csv (excluding the header).
    private int getEmployeeCount() {
        try {
            Path file = Path.of(EMPLOYEE_FILE);
            if (!Files.exists(file)) return 0;
            List<String> lines = Files.readAllLines(file);
            int count = 0;
            for (int i = 1; i < lines.size(); i++) {
                if (!lines.get(i).isBlank()) count++;
            }
            return count;
        } catch (IOException ex) {
            return 0;
        }
    }

    // Counts Present or Absent records for today's date only.
    private int getTodayAttendanceCount(String requiredStatus) {
        try {
            Path file = Path.of(ATTENDANCE_FILE);
            if (!Files.exists(file)) return 0;
            List<String> lines = Files.readAllLines(file);
            String today = LocalDate.now().toString();
            int count = 0;
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length >= 4
                        && parts[2].trim().equals(today)
                        && parts[3].trim().equalsIgnoreCase(requiredStatus)) {
                    count++;
                }
            }
            return count;
        } catch (IOException ex) {
            return 0;
        }
    }

    // Adds Net Salary (column 14 / index 13) for this month only.
    private double getCurrentMonthPayroll() {
        try {
            Path file = Path.of(PAYROLL_FILE);
            if (!Files.exists(file)) return 0.0;
            List<String> lines = Files.readAllLines(file);
            String currentMonth = YearMonth.now().toString();
            double total = 0.0;
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length >= 14 && parts[2].trim().equals(currentMonth)) {
                    total += Double.parseDouble(parts[13].trim());
                }
            }
            return total;
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private VBox dashboardDataCard(String title, String value,
            String description, String borderColor, String titleColor) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");
        Label number = new Label(value);
        number.setStyle("-fx-font-size: 27px; -fx-font-weight: bold; -fx-text-fill: #17365D;");
        Label details = new Label(description);
        details.setStyle("-fx-font-size: 11px; -fx-text-fill: #667085;");
        VBox card = new VBox(9, heading, number, details);
        card.setPadding(new Insets(20));
        card.setPrefHeight(135);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 13; -fx-border-color: "
                + borderColor + "; -fx-border-width: 0 0 0 5; -fx-border-radius: 13;"
                + "-fx-effect: dropshadow(gaussian, #DCE3ED, 8, 0, 0, 3);");
        return card;
    }

    private Button dashboardMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(195);
        button.setPrefHeight(43);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #E5EDF7; -fx-font-size: 13px; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 15 0 15;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #285582; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 15 0 15;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #E5EDF7; -fx-font-size: 13px; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 0 15 0 15;"));
        return button;
    }

    private VBox dashboardCard(String title, String description, String borderColor, String titleColor) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");
        Label details = new Label(description);
        details.setStyle("-fx-font-size: 12px; -fx-text-fill: #667085;");
        VBox card = new VBox(13, heading, details);
        card.setPadding(new Insets(22));
        card.setPrefHeight(115);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 13; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 5; -fx-border-radius: 13; -fx-effect: dropshadow(gaussian, #DCE3ED, 8, 0, 0, 3);");
        return card;
    }

    private Button createButton(String text) {

        Button button = new Button(text);
        button.setPrefWidth(320);
        button.setPrefHeight(43);

        button.setStyle(
                "-fx-background-color: " + navy + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 7;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #28558A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 7;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: " + navy + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 7;"
                )
        );

        return button;
    }

    // ================= ADD EMPLOYEE =================

    private void showAddEmployee() {

        Label title = heading("ADD EMPLOYEE");

        TextField id = new TextField();
        TextField name = new TextField();
        TextField department = new TextField();
        TextField designation = new TextField();
        TextField salary = new TextField();

        id.setPromptText("Employee ID");
        name.setPromptText("Employee Name");
        department.setPromptText("Department");
        designation.setPromptText("Designation");
        salary.setPromptText("Basic Salary");

        Button save = createButton("Save Employee");
        Button back = createButton("Back");

        save.setOnAction(e -> {
            try {
                if (id.getText().isBlank()
                        || name.getText().isBlank()
                        || department.getText().isBlank()
                        || designation.getText().isBlank()
                        || salary.getText().isBlank()) {
                    alert("Please fill all fields");
                    return;
                }

                double basic = Double.parseDouble(salary.getText());

                if (basic <= 0) {
                    alert("Salary must be greater than zero");
                    return;
                }

                if (findEmployee(id.getText().trim()) != null) {
                    alert("Employee ID already exists");
                    return;
                }

                boolean newFile = !Files.exists(Path.of(EMPLOYEE_FILE));

                try (BufferedWriter writer = Files.newBufferedWriter(
                        Path.of(EMPLOYEE_FILE),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {

                    if (newFile) {
                        writer.write(
                                "Employee ID,Name,Department,Designation,Basic Salary");
                        writer.newLine();
                    }

                    writer.write(
                            id.getText().trim() + ","
                            + name.getText().trim() + ","
                            + department.getText().trim() + ","
                            + designation.getText().trim() + ","
                            + basic
                    );
                    writer.newLine();
                }

                alert("Employee added successfully!");
                showDashboard();

            } catch (NumberFormatException ex) {
                alert("Enter a valid salary");
            } catch (IOException ex) {
                alert("Unable to save employee: " + ex.getMessage());
            }
        });

        back.setOnAction(e -> showDashboard());

        VBox form = new VBox(15,
                title, id, name, department, designation, salary, save, back);

        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color: white;");

        StackPane root = new StackPane(form);
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 750, 650));
    }

    // ================= VIEW EMPLOYEES =================

    private void showViewEmployees() {

        Label title = heading("EMPLOYEE DETAILS");
        TableView<Employee> table = new TableView<>();

        ObservableList<Employee> data =
                FXCollections.observableArrayList();

        TableColumn<Employee, String> idCol =
                new TableColumn<>("Employee ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Employee, String> nameCol =
                new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Employee, String> deptCol =
                new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));

        TableColumn<Employee, String> desCol =
                new TableColumn<>("Designation");
        desCol.setCellValueFactory(new PropertyValueFactory<>("designation"));

        TableColumn<Employee, Double> salaryCol =
                new TableColumn<>("Basic Salary");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));

        table.getColumns().addAll(
                idCol, nameCol, deptCol, desCol, salaryCol);

        try {
            if (Files.exists(Path.of(EMPLOYEE_FILE))) {
                List<String> lines =
                        Files.readAllLines(Path.of(EMPLOYEE_FILE));

                for (int i = 1; i < lines.size(); i++) {
                    String[] p = lines.get(i).split(",", -1);

                    if (p.length >= 5) {
                        data.add(new Employee(
                                p[0], p[1], p[2],
                                p[3], Double.parseDouble(p[4])));
                    }
                }
            }
        } catch (Exception ex) {
            alert("Error reading employees: " + ex.getMessage());
        }

        table.setItems(data);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        Button back = createButton("Back");
        back.setOnAction(e -> showDashboard());

        VBox root = new VBox(18, title, table, back);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 900, 600));
    }

    // ================= ATTENDANCE =================

    private void showAttendance() {

        Label title = heading("ATTENDANCE MANAGEMENT");

        TextField employeeId = new TextField();
        employeeId.setPromptText("Employee ID");

        DatePicker date = new DatePicker(LocalDate.now());

        ComboBox<String> status = new ComboBox<>();
        status.getItems().addAll("Present", "Absent");
        status.setValue("Present");
        status.setMaxWidth(Double.MAX_VALUE);

        Button save = createButton("Save Attendance");
        Button view = createButton("View Attendance Records");
        Button back = createButton("Back");

        save.setOnAction(e -> {
            try {
                Employee emp = findEmployee(employeeId.getText().trim());

                if (emp == null) {
                    alert("Employee ID not found");
                    return;
                }

                if (date.getValue() == null) {
                    alert("Please select a date");
                    return;
                }

                String selectedDate = date.getValue().toString();

                if (attendanceExists(emp.getId(), selectedDate)) {
                    alert("Attendance already entered for this date");
                    return;
                }

                boolean newFile =
                        !Files.exists(Path.of(ATTENDANCE_FILE));

                try (BufferedWriter writer = Files.newBufferedWriter(
                        Path.of(ATTENDANCE_FILE),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {

                    if (newFile) {
                        writer.write(
                                "Employee ID,Employee Name,Date,Status");
                        writer.newLine();
                    }

                    writer.write(
                            emp.getId() + ","
                            + emp.getName() + ","
                            + selectedDate + ","
                            + status.getValue());
                    writer.newLine();
                }

                alert("Attendance saved successfully!");

            } catch (IOException ex) {
                alert("Unable to save attendance: " + ex.getMessage());
            }
        });

        view.setOnAction(e -> showAttendanceRecords());
        back.setOnAction(e -> showDashboard());

        VBox form = new VBox(15,
                title, employeeId, date, status, save, view, back);

        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color: white;");

        StackPane root = new StackPane(form);
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 750, 600));
    }

    // ================= ATTENDANCE RECORDS =================

    private void showAttendanceRecords() {

        Label title = heading("ATTENDANCE RECORDS");
        TableView<Attendance> table = new TableView<>();

        ObservableList<Attendance> data =
                FXCollections.observableArrayList();

        TableColumn<Attendance, String> idCol =
                new TableColumn<>("Employee ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Attendance, String> nameCol =
                new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Attendance, String> dateCol =
                new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Attendance, String> statusCol =
                new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, nameCol, dateCol, statusCol);

        try {
            if (Files.exists(Path.of(ATTENDANCE_FILE))) {
                List<String> lines =
                        Files.readAllLines(Path.of(ATTENDANCE_FILE));

                for (int i = 1; i < lines.size(); i++) {
                    String[] p = lines.get(i).split(",", -1);

                    if (p.length >= 4) {
                        data.add(new Attendance(
                                p[0], p[1], p[2], p[3]));
                    }
                }
            }
        } catch (Exception ex) {
            alert("Unable to read attendance records");
        }

        table.setItems(data);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        Button back = createButton("Back");
        back.setOnAction(e -> showAttendance());

        VBox root = new VBox(15, title, table, back);
        root.setPadding(new Insets(25));

        stage.setScene(new Scene(root, 850, 600));
    }

    // ================= CALCULATE SALARY =================

    private void showCalculateSalary() {

        Label title = heading("CALCULATE SALARY");

        TextField employeeId = new TextField();
        employeeId.setPromptText("Employee ID");

        TextField month = new TextField();
        month.setPromptText("Month (YYYY-MM)");

        Button calculate = createButton("Calculate Salary");
        Button back = createButton("Back");

        calculate.setOnAction(e -> {
            try {
                String id = employeeId.getText().trim();
                String selectedMonth = month.getText().trim();

                YearMonth ym = YearMonth.parse(selectedMonth);
                Employee emp = findEmployee(id);

                if (emp == null) {
                    alert("Employee ID not found");
                    return;
                }

                if (payrollExists(id, selectedMonth)) {
                    alert("Salary for this employee and month is already saved.");
                    return;
                }

                Salary s = calculate(emp, ym);
                savePayroll(emp, s);
                showSalaryDetails(emp, s);

            } catch (Exception ex) {
                alert("Please enter a valid Employee ID and month (YYYY-MM)");
            }
        });

        back.setOnAction(e -> showDashboard());

        VBox form = new VBox(15,
                title, employeeId, month, calculate, back);

        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color: white;");

        StackPane root = new StackPane(form);
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 750, 550));
    }

    private Salary calculate(Employee emp, YearMonth month)
            throws IOException {

        int present = 0;
        int absent = 0;

        if (Files.exists(Path.of(ATTENDANCE_FILE))) {
            List<String> lines =
                    Files.readAllLines(Path.of(ATTENDANCE_FILE));

            for (int i = 1; i < lines.size(); i++) {
                String[] p = lines.get(i).split(",", -1);

                if (p.length >= 4 && p[0].equals(emp.getId())) {
                    LocalDate date = LocalDate.parse(p[2]);

                    if (YearMonth.from(date).equals(month)) {
                        if (p[3].equalsIgnoreCase("Present")) {
                            present++;
                        } else if (p[3].equalsIgnoreCase("Absent")) {
                            absent++;
                        }
                    }
                }
            }
        }

        double basic = emp.getSalary();
        double hra = basic * 0.20;
        double da = basic * 0.10;
        double gross = basic + hra + da;

        double pf = basic * 0.12;
        double professionalTax = 200;
        double lop = (basic / 30.0) * absent;

        double totalDeductions = pf + professionalTax + lop;
        double net = gross - totalDeductions;

        return new Salary(
                month.toString(), present, absent,
                basic, hra, da, gross,
                pf, professionalTax, lop,
                totalDeductions, net);
    }

    // ================= SAVE PAYROLL =================

    private void savePayroll(Employee emp, Salary s)
            throws IOException {

        boolean newFile = !Files.exists(Path.of(PAYROLL_FILE));

        try (BufferedWriter writer = Files.newBufferedWriter(
                Path.of(PAYROLL_FILE),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            if (newFile) {
                writer.write(
                        "Employee ID,Name,Month,Present,Absent,Basic,HRA,DA,Gross,PF,PT,LOP,Total Deductions,Net Salary");
                writer.newLine();
            }

            writer.write(
                    emp.getId() + ","
                    + emp.getName() + ","
                    + s.month + ","
                    + s.present + ","
                    + s.absent + ","
                    + s.basic + ","
                    + s.hra + ","
                    + s.da + ","
                    + s.gross + ","
                    + s.pf + ","
                    + s.pt + ","
                    + s.lop + ","
                    + s.totalDeductions + ","
                    + s.net);
            writer.newLine();
        }
    }

    // ================= SALARY RESULT =================

    private void showSalaryDetails(Employee emp, Salary s) {

        Label title = heading("SALARY CALCULATION RESULT");

        VBox details = new VBox(10,
                new Label("Employee ID: " + emp.getId()),
                new Label("Employee Name: " + emp.getName()),
                new Label("Salary Month: " + s.month),
                new Label("Present Days: " + s.present),
                new Label("Absent Days: " + s.absent),
                new Label("Basic Salary: " + money(s.basic)),
                new Label("HRA: " + money(s.hra)),
                new Label("DA: " + money(s.da)),
                new Label("Gross Salary: " + money(s.gross)),
                new Label("PF: " + money(s.pf)),
                new Label("Professional Tax: " + money(s.pt)),
                new Label("LOP Deduction: " + money(s.lop)),
                new Label("Total Deductions: " + money(s.totalDeductions)));

        Label netLabel = new Label("NET SALARY: " + money(s.net));
        netLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        netLabel.setStyle("-fx-text-fill: " + green + ";");

        Button viewSlip = createButton("View Salary Slip");
        Button back = createButton("Dashboard");

        viewSlip.setOnAction(e -> displaySalarySlip(emp, s));
        back.setOnAction(e -> showDashboard());

        VBox root = new VBox(
                15, title, details, netLabel, viewSlip, back);

        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 750, 700));
    }

    // ================= GENERATE SALARY SLIP =================

    private void showSalarySlip() {

        Label title = heading("GENERATE SALARY SLIP");

        TextField employeeId = new TextField();
        employeeId.setPromptText("Enter Employee ID");

        TextField month = new TextField();
        month.setPromptText("Enter Month (YYYY-MM)");

        Button generate = createButton("Generate Salary Slip");
        Button back = createButton("Back");

        generate.setOnAction(e -> {
            try {
                String id = employeeId.getText().trim();
                YearMonth ym = YearMonth.parse(month.getText().trim());

                Employee emp = findEmployee(id);

                if (emp == null) {
                    alert("Employee ID not found");
                    return;
                }

                Salary salary = findSavedSalary(id, ym.toString());

                if (salary == null) {
                    alert("Salary record not found. First calculate salary for this month.");
                    return;
                }

                displaySalarySlip(emp, salary);

            } catch (Exception ex) {
                alert("Please enter valid Employee ID and month (YYYY-MM)");
            }
        });

        back.setOnAction(e -> showDashboard());

        VBox form = new VBox(
                15, title, employeeId, month, generate, back);

        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color: white;");

        StackPane root = new StackPane(form);
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 750, 550));
    }

    // ================= FIND SAVED SALARY =================

    private Salary findSavedSalary(String id, String month)
            throws IOException {

        if (!Files.exists(Path.of(PAYROLL_FILE))) {
            return null;
        }

        List<String> lines = Files.readAllLines(Path.of(PAYROLL_FILE));

        for (int i = lines.size() - 1; i >= 1; i--) {
            String[] p = lines.get(i).split(",", -1);

            if (p.length >= 14
                    && p[0].equals(id)
                    && p[2].equals(month)) {

                return new Salary(
                        p[2],
                        Integer.parseInt(p[3]),
                        Integer.parseInt(p[4]),
                        Double.parseDouble(p[5]),
                        Double.parseDouble(p[6]),
                        Double.parseDouble(p[7]),
                        Double.parseDouble(p[8]),
                        Double.parseDouble(p[9]),
                        Double.parseDouble(p[10]),
                        Double.parseDouble(p[11]),
                        Double.parseDouble(p[12]),
                        Double.parseDouble(p[13]));
            }
        }

        return null;
    }

    // ================= SALARY SLIP DISPLAY =================

    private void displaySalarySlip(Employee emp, Salary s) {

        Label company = new Label("EMPLOYEE PAYROLL SYSTEM");
        company.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        company.setStyle("-fx-text-fill: " + navy + ";");

        Label subtitle = new Label(
                "MONTHLY EMPLOYEE SALARY STATEMENT");

        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label slipTitle = new Label("SALARY SLIP - " + s.month);
        slipTitle.setFont(Font.font("Arial", FontWeight.BOLD, 19));
        slipTitle.setStyle("-fx-text-fill: " + navy + ";");

        VBox header = new VBox(7, company, subtitle, slipTitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));

        GridPane info = new GridPane();
        info.setHgap(20);
        info.setVgap(12);
        info.setPadding(new Insets(18));
        info.setStyle(
                "-fx-background-color: #F4F7FB;" +
                "-fx-border-color: #B8C7D9;" +
                "-fx-border-width: 1;");

        addSlipInfo(info, 0, 0, "Employee ID", emp.getId());
        addSlipInfo(info, 0, 1, "Employee Name", emp.getName());
        addSlipInfo(info, 1, 0, "Department", emp.getDepartment());
        addSlipInfo(info, 1, 1, "Designation", emp.getDesignation());
        addSlipInfo(info, 2, 0, "Pay Period", s.month);
        addSlipInfo(info, 2, 1, "Pay Date", LocalDate.now().toString());
        addSlipInfo(info, 3, 0, "Present Days", String.valueOf(s.present));
        addSlipInfo(info, 3, 1, "Absent / LOP Days", String.valueOf(s.absent));
        addSlipInfo(info, 4, 0, "Total Recorded Days",
                String.valueOf(s.present + s.absent));

        VBox earningsBox = createSalaryTable(
                "EARNINGS",
                new String[][] {
                    {"Basic Salary", money(s.basic)},
                    {"Dearness Allowance", money(s.da)},
                    {"House Rent Allowance", money(s.hra)},
                    {"Leave Travel Allowance", money(0)},
                    {"Gross Earnings", money(s.gross)}
                });

        VBox deductionsBox = createSalaryTable(
                "DEDUCTIONS",
                new String[][] {
                    {"Employee Provident Fund", money(s.pf)},
                    {"Professional Tax", money(s.pt)},
                    {"Tax Deducted at Source", money(0)},
                    {"Medical Insurance", money(0)},
                    {"Loss of Pay", money(s.lop)},
                    {"Total Deductions", money(s.totalDeductions)}
                });

        HBox tables = new HBox(22, earningsBox, deductionsBox);
        tables.setAlignment(Pos.TOP_CENTER);

        Label netTitle = new Label("TOTAL NET PAYABLE");
        netTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        netTitle.setStyle("-fx-text-fill: " + navy + ";");

        Label calculation = new Label(
                "Gross Earnings - Total Deductions");

        Label netAmount = new Label(money(s.net));
        netAmount.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        netAmount.setStyle("-fx-text-fill: " + green + ";");

        VBox netBox = new VBox(8, netTitle, calculation, netAmount);
        netBox.setAlignment(Pos.CENTER);
        netBox.setPadding(new Insets(20));
        netBox.setMaxWidth(Double.MAX_VALUE);
        netBox.setStyle(
                "-fx-background-color: #EAF5F0;" +
                "-fx-border-color: #A7CDB8;" +
                "-fx-border-width: 1;");

        Label note = new Label(
                "This is a system-generated salary slip.");
        note.setStyle("-fx-text-fill: #777777;");

        VBox slip = new VBox(
                18, header, new Separator(), info,
                tables, netBox, note);

        slip.setAlignment(Pos.CENTER);
        slip.setPadding(new Insets(25));
        slip.setPrefWidth(1000);
        slip.setMinWidth(1000);
        slip.setStyle("-fx-background-color: white;");

        ScrollPane scroll = new ScrollPane(slip);
        scroll.setFitToHeight(false);
        scroll.setFitToWidth(false);
        scroll.setPannable(true);

        Button download = createButton("Download Salary Slip (PDF)");
        Button print = createButton("Print Salary Slip");
        Button back = createButton("Back to Dashboard");

        download.setOnAction(e -> downloadSalarySlip(emp, s));
        print.setOnAction(e -> printSalarySlip(slip));
        back.setOnAction(e -> showDashboard());

        HBox buttons = new HBox(12, download, print, back);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));

        VBox root = new VBox(10, scroll, buttons);
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 1100, 850));
    }

    // ================= DOWNLOAD SALARY SLIP PDF =================

    private void downloadSalarySlip(Employee emp, Salary s) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Salary Slip as PDF");
        fileChooser.setInitialFileName(
                "Salary_Slip_" + emp.getId() + "_" + s.month + ".pdf");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content =
                    new PDPageContentStream(document, page)) {

                float pageWidth = page.getMediaBox().getWidth();

                PDType1Font regular =
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                PDType1Font bold =
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                // COMPANY HEADER

                pdfText(content, bold, 20,
                        "EMPLOYEE PAYROLL SYSTEM", 50, 45);

                pdfText(content, regular, 11,
                        "MONTHLY EMPLOYEE SALARY STATEMENT", 50, 65);

                pdfText(content, bold, 15,
                        "SALARY SLIP - " + s.month, 50, 95);

                pdfLine(content, 50, 108, pageWidth - 50, 108);

                // EMPLOYEE INFORMATION

                pdfText(content, bold, 13,
                        "EMPLOYEE INFORMATION", 50, 135);

                pdfText(content, regular, 11,
                        "Employee ID: " + emp.getId(), 50, 160);

                pdfText(content, regular, 11,
                        "Employee Name: " + emp.getName(), 310, 160);

                pdfText(content, regular, 11,
                        "Department: " + emp.getDepartment(), 50, 185);

                pdfText(content, regular, 11,
                        "Designation: " + emp.getDesignation(), 310, 185);

                pdfText(content, regular, 11,
                        "Pay Period: " + s.month, 50, 210);

                pdfText(content, regular, 11,
                        "Pay Date: " + LocalDate.now(), 310, 210);

                pdfText(content, regular, 11,
                        "Present Days: " + s.present, 50, 235);

                pdfText(content, regular, 11,
                        "Absent / LOP Days: " + s.absent, 310, 235);

                pdfText(content, regular, 11,
                        "Total Recorded Days: " + (s.present + s.absent),
                        50, 260);

                pdfLine(content, 50, 280, pageWidth - 50, 280);

                // EARNINGS TABLE

                String[][] earnings = {
                    {"Basic Salary", money(s.basic)},
                    {"Dearness Allowance", money(s.da)},
                    {"House Rent Allowance", money(s.hra)},
                    {"Leave Travel Allowance", money(0)},
                    {"Gross Earnings", money(s.gross)}
                };

                // DEDUCTIONS TABLE

                String[][] deductions = {
                    {"Employee Provident Fund", money(s.pf)},
                    {"Professional Tax", money(s.pt)},
                    {"Tax Deducted at Source", money(0)},
                    {"Medical Insurance", money(0)},
                    {"Loss of Pay", money(s.lop)},
                    {"Total Deductions", money(s.totalDeductions)}
                };

                drawPdfTable(content, bold, regular,
                        50, 310, 245, "EARNINGS", earnings);

                drawPdfTable(content, bold, regular,
                        315, 310, 230, "DEDUCTIONS", deductions);

                // NET PAYABLE

                pdfLine(content, 50, 540, pageWidth - 50, 540);

                pdfText(content, bold, 15,
                        "TOTAL NET PAYABLE", 50, 575);

                pdfText(content, regular, 11,
                        "Gross Earnings - Total Deductions", 50, 600);

                pdfText(content, bold, 22,
                        money(s.net), 50, 635);

                pdfLine(content, 50, 670, pageWidth - 50, 670);

                pdfText(content, regular, 10,
                        "This is a system-generated salary slip.", 50, 695);

                pdfText(content, regular, 9,
                        "Employee Payroll Management System", 50, 720);
            }

            document.save(file);

            alert("Salary slip downloaded successfully!\n\nSaved at:\n"
                    + file.getAbsolutePath());

        } catch (Exception ex) {
            alert("Unable to generate PDF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ================= PDF TABLE =================

    private void drawPdfTable(
            PDPageContentStream content,
            PDType1Font bold,
            PDType1Font regular,
            float x,
            float y,
            float width,
            String title,
            String[][] rows) throws IOException {

        pdfText(content, bold, 13, title, x, y);

        float headerY = y + 25;
        float rowHeight = 27;

        pdfLine(content, x, headerY, x + width, headerY);

        pdfText(content, bold, 9,
                "Description", x + 5, headerY + 16);

        pdfText(content, bold, 9,
                "Amount", x + width - 65, headerY + 16);

        pdfLine(content, x, headerY + 23,
                x + width, headerY + 23);

        for (int i = 0; i < rows.length; i++) {

            float rowY = headerY + 23 + (i * rowHeight);
            boolean total = i == rows.length - 1;

            pdfText(content, total ? bold : regular, 9,
                    rows[i][0], x + 5, rowY + 17);

            pdfText(content, total ? bold : regular, 9,
                    rows[i][1], x + width - 75, rowY + 17);

            pdfLine(content, x, rowY + rowHeight,
                    x + width, rowY + rowHeight);
        }
    }

    // ================= PDF TEXT HELPER =================

    private void pdfText(
            PDPageContentStream content,
            PDType1Font font,
            int fontSize,
            String text,
            float x,
            float topY) throws IOException {

        String safeText = safePdfText(text);

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, 842 - topY);
        content.showText(safeText);
        content.endText();
    }

    private String safePdfText(String text) {

        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (c >= 32 && c <= 255) {
                result.append(c);
            } else {
                result.append('?');
            }
        }

        return result.toString();
    }

    // ================= PDF LINE HELPER =================

    private void pdfLine(
            PDPageContentStream content,
            float x1,
            float y1,
            float x2,
            float y2) throws IOException {

        content.moveTo(x1, 842 - y1);
        content.lineTo(x2, 842 - y2);
        content.stroke();
    }

    // ================= SALARY TABLE IN GUI =================

    private VBox createSalaryTable(String title, String[][] rows) {

        Label heading = new Label(title);
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        heading.setStyle("-fx-text-fill: " + navy + ";");

        GridPane table = new GridPane();
        table.setPrefWidth(470);
        table.setMinWidth(440);

        ColumnConstraints description = new ColumnConstraints();
        description.setPercentWidth(65);
        description.setMinWidth(230);
        description.setHgrow(Priority.ALWAYS);

        ColumnConstraints amount = new ColumnConstraints();
        amount.setPercentWidth(35);
        amount.setMinWidth(130);
        amount.setHgrow(Priority.ALWAYS);

        table.getColumnConstraints().addAll(description, amount);

        addSalaryCell(table, "Description", 0, 0, true, false);
        addSalaryCell(table, "Amount (Rs.)", 1, 0, true, true);

        for (int i = 0; i < rows.length; i++) {
            boolean totalRow =
                    rows[i][0].equals("Gross Earnings")
                    || rows[i][0].equals("Total Deductions");

            addSalaryCell(table, rows[i][0],
                    0, i + 1, totalRow, false);

            addSalaryCell(table, rows[i][1],
                    1, i + 1, totalRow, true);
        }

        VBox box = new VBox(12, heading, table);
        box.setPrefWidth(470);
        box.setMinWidth(440);

        return box;
    }

    private void addSalaryCell(
            GridPane table,
            String value,
            int column,
            int row,
            boolean bold,
            boolean rightAlign) {

        Label label = new Label(value);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(42);
        label.setPadding(new Insets(10));

        label.setAlignment(
                rightAlign ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        label.setFont(Font.font(
                "Arial",
                bold ? FontWeight.BOLD : FontWeight.NORMAL,
                13));

        String cellBackground =
                row == 0 ? lightBlue :
                (bold ? "#F0F3F8" : "white");

        label.setStyle(
                "-fx-background-color: " + cellBackground + ";" +
                "-fx-border-color: #C8D3E0;" +
                "-fx-border-width: 0.7;" +
                "-fx-text-fill: #24364B;");

        GridPane.setHgrow(label, Priority.ALWAYS);
        table.add(label, column, row);
    }

    // ================= EMPLOYEE INFORMATION CELLS =================

    private void addSlipInfo(
            GridPane grid,
            int row,
            int column,
            String title,
            String value) {

        Label key = new Label(title + ":");
        key.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        key.setStyle("-fx-text-fill: #34495E;");

        Label data = new Label(value);
        data.setFont(Font.font("Arial", 13));
        data.setStyle("-fx-text-fill: #222222;");

        VBox item = new VBox(5, key, data);
        item.setMinWidth(250);
        item.setPrefWidth(400);

        grid.add(item, column, row);
    }

    // ================= PRINT SALARY SLIP =================

    private void printSalarySlip(VBox slip) {

        PrinterJob job = PrinterJob.createPrinterJob();

        if (job == null) {
            alert("No printer is available on this computer.");
            return;
        }

        boolean proceed = job.showPrintDialog(stage);

        if (proceed) {
            boolean success = job.printPage(slip);

            if (success) {
                job.endJob();
                alert("Salary slip sent to printer.");
            } else {
                job.cancelJob();
                alert("Unable to print salary slip.");
            }
        }
    }

    // ================= PAYROLL HISTORY =================

    private void showPayrollHistory() {

        Label title = heading("PAYROLL HISTORY");
        TableView<PayrollRecord> table = new TableView<>();

        ObservableList<PayrollRecord> data =
                FXCollections.observableArrayList();

        String[] names = {
                "Employee ID", "Name", "Month",
                "Basic", "Gross", "Deductions", "Net Salary"
        };

        String[] properties = {
                "id", "name", "month",
                "basic", "gross", "deductions", "net"
        };

        for (int i = 0; i < names.length; i++) {
            TableColumn<PayrollRecord, String> col =
                    new TableColumn<>(names[i]);

            col.setCellValueFactory(
                    new PropertyValueFactory<>(properties[i]));

            table.getColumns().add(col);
        }

        try {
            if (Files.exists(Path.of(PAYROLL_FILE))) {
                List<String> lines =
                        Files.readAllLines(Path.of(PAYROLL_FILE));

                for (int i = 1; i < lines.size(); i++) {
                    String[] p = lines.get(i).split(",", -1);

                    if (p.length >= 14) {
                        data.add(new PayrollRecord(
                                p[0], p[1], p[2],
                                p[5], p[8], p[12], p[13]));
                    }
                }
            }
        } catch (Exception ex) {
            alert("Unable to read payroll history");
        }

        table.setItems(data);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        Button back = createButton("Back");
        back.setOnAction(e -> showDashboard());

        VBox root = new VBox(15, title, table, back);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: " + background + ";");

        stage.setScene(new Scene(root, 950, 600));
    }

    // ================= FIND EMPLOYEE =================

    private Employee findEmployee(String id) throws IOException {

        if (!Files.exists(Path.of(EMPLOYEE_FILE))) {
            return null;
        }

        List<String> lines =
                Files.readAllLines(Path.of(EMPLOYEE_FILE));

        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",", -1);

            if (p.length >= 5 && p[0].equals(id)) {
                return new Employee(
                        p[0], p[1], p[2],
                        p[3], Double.parseDouble(p[4]));
            }
        }

        return null;
    }

    // ================= ATTENDANCE EXISTS =================

    private boolean attendanceExists(String id, String date)
            throws IOException {

        if (!Files.exists(Path.of(ATTENDANCE_FILE))) {
            return false;
        }

        List<String> lines =
                Files.readAllLines(Path.of(ATTENDANCE_FILE));

        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",", -1);

            if (p.length >= 4
                    && p[0].equals(id)
                    && p[2].equals(date)) {
                return true;
            }
        }

        return false;
    }

    // ================= PAYROLL EXISTS =================

    private boolean payrollExists(String id, String month)
            throws IOException {

        if (!Files.exists(Path.of(PAYROLL_FILE))) {
            return false;
        }

        List<String> lines =
                Files.readAllLines(Path.of(PAYROLL_FILE));

        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",", -1);

            if (p.length >= 14
                    && p[0].equals(id)
                    && p[2].equals(month)) {
                return true;
            }
        }

        return false;
    }

    // ================= COMMON METHODS =================

    private Label heading(String text) {

        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 23));
        label.setStyle("-fx-text-fill: " + navy + ";");

        return label;
    }

    private String money(double amount) {
        return String.format(Locale.US, "Rs. %.2f", amount);
    }

    private void alert(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Employee Payroll System");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================= EMPLOYEE CLASS =================

    public static class Employee {

        private final String id;
        private final String name;
        private final String department;
        private final String designation;
        private final double salary;

        public Employee(String id, String name, String department,
                        String designation, double salary) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.designation = designation;
            this.salary = salary;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDepartment() { return department; }
        public String getDesignation() { return designation; }
        public double getSalary() { return salary; }
    }

    // ================= ATTENDANCE CLASS =================

    public static class Attendance {

        private final String id;
        private final String name;
        private final String date;
        private final String status;

        public Attendance(String id, String name,
                          String date, String status) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.status = status;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
    }

    // ================= SALARY CLASS =================

    public static class Salary {

        String month;
        int present;
        int absent;

        double basic;
        double hra;
        double da;
        double gross;

        double pf;
        double pt;
        double lop;

        double totalDeductions;
        double net;

        public Salary(String month, int present, int absent,
                      double basic, double hra, double da,
                      double gross, double pf, double pt,
                      double lop, double totalDeductions, double net) {

            this.month = month;
            this.present = present;
            this.absent = absent;
            this.basic = basic;
            this.hra = hra;
            this.da = da;
            this.gross = gross;
            this.pf = pf;
            this.pt = pt;
            this.lop = lop;
            this.totalDeductions = totalDeductions;
            this.net = net;
        }
    }

    // ================= PAYROLL RECORD CLASS =================

    public static class PayrollRecord {

        private final String id;
        private final String name;
        private final String month;
        private final String basic;
        private final String gross;
        private final String deductions;
        private final String net;

        public PayrollRecord(String id, String name, String month,
                             String basic, String gross,
                             String deductions, String net) {
            this.id = id;
            this.name = name;
            this.month = month;
            this.basic = basic;
            this.gross = gross;
            this.deductions = deductions;
            this.net = net;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getMonth() { return month; }
        public String getBasic() { return basic; }
        public String getGross() { return gross; }
        public String getDeductions() { return deductions; }
        public String getNet() { return net; }
    }

    // ================= MAIN =================

    public static void main(String[] args) {
        launch(args);
    }
}
