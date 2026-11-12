# Acme Payroll (web)

Internal payroll and HR web application for Acme Corporation: employees, departments,
weekly timesheets, monthly pay runs, payslips and payroll cost reports.

It is a classic Java EE web application: servlets acting as front controllers, JSP views,
plain JDBC DAOs and an embedded H2 database. No framework.

## Tech stack

| Layer        | Technology                                                        |
|--------------|-------------------------------------------------------------------|
| Language     | Java 8 (source/target 1.8)                                        |
| Web          | Servlet 3.0, JSP 2.2, JSTL 1.2, `web.xml` configuration           |
| Front end    | jQuery 1.12, jQuery UI 1.12 (datepicker, dialogs, tabs), plain CSS |
| Persistence  | JDBC with a small built-in connection pool, H2 2.2 (file mode)    |
| Logging      | SLF4J + reload4j (log4j 1.2 API, `log4j.properties`)              |
| Build        | Maven 3, WAR packaging, Jetty 9.4 Maven plugin for local running  |
| Tests        | JUnit 4                                                           |

## Prerequisites

- JDK 8 or newer (built and tested with JDK 8 and JDK 21)
- Maven 3.6+
- Internet access on first build (Maven dependencies; jQuery is loaded from code.jquery.com)

## Running

```bash
mvn jetty:run
```

Open http://localhost:8080/ and sign in with one of the demo accounts.

On the first start the application creates the H2 database in `./data/payroll.mv.db`
and loads the demo data (departments, ~25 employees, salary history, timesheets, two paid
pay runs and one draft pay run). Delete the `data/` directory to start over.

To deploy to another servlet container (Tomcat 7+, Jetty 9+) build the WAR with
`mvn package` and deploy `target/payroll.war`. The database location can be changed with
`-Dpayroll.db.url=jdbc:h2:file:/path/to/payroll`.

## Demo accounts

All demo accounts use the password `Passw0rd!`.

| Email                | Role     | Can do                                                                 |
|----------------------|----------|------------------------------------------------------------------------|
| `admin@acme.test`    | Admin    | Everything                                                             |
| `manager@acme.test`  | Manager  | Employee list, approve/reject Engineering timesheets, reports          |
| `employee@acme.test` | Employee | Own profile and payslips, enter and submit own timesheets              |

## Screens and routes

| Route                                   | Screen                                                     | Roles            |
|-----------------------------------------|------------------------------------------------------------|------------------|
| `/login`, `/logout`                     | Sign in / sign out                                         | all              |
| `/dashboard`                            | Headcount, last payroll, pending timesheets, next pay date, cost by department | all |
| `/employees/`                           | Employee list: search, department/status filter, sorting, paging | admin, manager |
| `/employees/new`, `/employees/edit?id=` | Employee form with validation                              | admin            |
| `/employees/view?id=`                   | Profile, salary history (add raise), payslips; deactivate  | admin, manager   |
| `/profile`                              | Own employee record                                        | employee         |
| `/timesheets/`                          | Timesheet list with status filter                          | all (scoped)     |
| `/timesheets/new`, `/timesheets/edit?id=` | Weekly timesheet (Mon-Sun, 0-24 h per day), save / submit | all              |
| `/timesheets/view?id=`                  | Timesheet detail, approve / reject with reason             | admin, manager   |
| `/payruns/`                             | Pay run list, create pay run for a period                  | admin            |
| `/payruns/view?id=`                     | Calculate, review lines, approve, mark paid, CSV export    | admin            |
| `/payslips/view?id=`                    | Payslip breakdown, print view, CSV download                | owner, admin     |
| `/departments/`                         | Department CRUD                                            | admin            |
| `/reports/cost`                         | Payroll cost by department for a period, CSV export        | admin, manager   |
| `/admin/taxes`                          | Tax brackets, social/pension rates, deduction types        | admin            |
| `/admin/users`                          | User accounts (enable/disable)                             | admin            |
| `/admin/audit`                          | Audit log with filter                                      | admin            |

## Payroll rules

- Salaried employees: gross = annual salary / 12.
- Hourly employees: gross = rate x regular hours + rate x 1.5 x overtime hours, where overtime
  is the time above 40 hours in a week. Only approved timesheets whose week starts in the pay
  period are paid.
- Income tax is progressive on the monthly gross (defaults: 0% up to 1,000; 15% up to 4,000;
  25% above). Brackets are editable under Settings.
- Social contribution 5% of gross; pension 3% of gross for employees in the pension plan.
- Recurring deductions (health insurance, union dues) are subtracted as "other deductions".
- Amounts are rounded to cents, half-up. Currency is USD.
- Pay run status flow: Draft -> Calculated -> Approved -> Paid. Paid runs are locked.

## Project structure

```
src/main/java/com/acme/payroll/
    dao/         JDBC DAOs (BaseDao has the query/update helpers)
    listener/    AppContextListener - connection pool and first-start database setup
    model/       Plain JavaBeans and enums
    service/     Business rules: payroll calculation, pay run workflow, validation
    util/        Connection pool, SQL script runner, dates, money, CSV, paging
    web/filter/  EncodingFilter, AuthFilter (login + role checks)
    web/servlet/ Controllers, one per area, dispatching on the path info
src/main/resources/
    db/schema.sql, db/seed.sql   database schema and demo data
    db.properties, log4j.properties
src/main/webapp/
    WEB-INF/web.xml              servlet, filter and error page configuration
    WEB-INF/jsp/                 JSP views (include/header.jsp and footer.jsp for layout)
    css/, js/                    styles and jQuery page scripts
src/test/java/                   JUnit tests
```

## Tests

```bash
mvn test
```

The unit tests cover the payroll calculation, employee validation and utilities. The DAO tests
load the schema and demo data into an in-memory H2 database and run the DAOs and pay run
workflow against it. `mvn verify` additionally builds the WAR. CI runs `mvn -B verify` on
every push (see `.github/workflows/ci.yml`).
