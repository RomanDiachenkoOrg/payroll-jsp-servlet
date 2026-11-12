package com.acme.payroll.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.acme.payroll.model.Department;
import com.acme.payroll.model.Employee;
import com.acme.payroll.model.PayRun;
import com.acme.payroll.model.PayRunStatus;
import com.acme.payroll.model.Payslip;
import com.acme.payroll.model.TimesheetStatus;
import com.acme.payroll.model.User;
import com.acme.payroll.service.AuthService;
import com.acme.payroll.service.BusinessException;
import com.acme.payroll.service.DepartmentService;
import com.acme.payroll.service.PayRunService;
import com.acme.payroll.util.DateUtil;
import com.acme.payroll.util.Page;
import com.acme.payroll.util.ScriptRunner;
import com.acme.payroll.util.SimpleConnectionPool;

/** Runs the DAOs and services against an in-memory H2 database loaded with the demo data. */
public class DaoIntegrationTest {

    private static int counter;

    private SimpleConnectionPool ds;

    @Before
    public void setUp() throws Exception {
        ds = new SimpleConnectionPool("jdbc:h2:mem:test" + (++counter) + ";DB_CLOSE_DELAY=-1", "sa", "", 4);
        Connection con = ds.getConnection();
        try {
            new ScriptRunner(con).run("/db/schema.sql");
            new ScriptRunner(con).run("/db/seed.sql");
        } finally {
            con.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        Connection con = ds.getConnection();
        try {
            con.createStatement().execute("SHUTDOWN");
        } finally {
            con.close();
        }
        ds.shutdown();
    }

    @Test
    public void seedDataIsLoaded() {
        assertEquals(5, new DepartmentDao(ds).findAll().size());
        assertEquals(22, new EmployeeDao(ds).countActive());
        assertEquals(3, new PayRunDao(ds).findAll().size());
        assertTrue(new TimesheetDao(ds).countByStatus(TimesheetStatus.SUBMITTED) > 0);
    }

    @Test
    public void searchFiltersByDepartmentAndText() {
        EmployeeFilter f = new EmployeeFilter();
        f.setDepartmentId(1L);
        Page<Employee> page = new EmployeeDao(ds).search(f);
        assertEquals(7, page.getTotalItems());
        for (Employee e : page.getItems()) {
            assertEquals("Engineering", e.getDepartmentName());
        }

        f = new EmployeeFilter();
        f.setQuery("garcia");
        page = new EmployeeDao(ds).search(f);
        assertEquals(1, page.getTotalItems());
        assertEquals("Mason", page.getItems().get(0).getFirstName());
    }

    @Test
    public void inactiveFilter() {
        EmployeeFilter f = new EmployeeFilter();
        f.setStatus(EmployeeFilter.STATUS_INACTIVE);
        Page<Employee> page = new EmployeeDao(ds).search(f);
        assertEquals(3, page.getTotalItems());
        for (Employee e : page.getItems()) {
            assertFalse(e.isActive());
        }
    }

    @Test
    public void authenticatesDemoUsers() {
        AuthService auth = new AuthService(new UserDao(ds), new AuditLogDao(ds));
        User admin = auth.authenticate("admin@acme.test", "Passw0rd!");
        assertNotNull(admin);
        assertTrue(admin.isAdmin());
        assertNull(auth.authenticate("admin@acme.test", "wrong"));
        assertNull(auth.authenticate("nobody@acme.test", "Passw0rd!"));
    }

    @Test
    public void cannotDeleteDepartmentWithActiveEmployees() {
        DepartmentService service = new DepartmentService(new DepartmentDao(ds), new AuditLogDao(ds));
        try {
            service.delete(1L, "admin@acme.test");
            fail("expected BusinessException");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("active employees"));
        }
        Department d = new Department();
        d.setCode("LEG");
        d.setName("Legal");
        assertTrue(service.save(d, "admin@acme.test").isEmpty());
        service.delete(d.getId(), "admin@acme.test");
        assertNull(new DepartmentDao(ds).findById(d.getId()));
    }

    @Test
    public void calculateAndApproveDraftPayRun() {
        PayRunService service = new PayRunService(ds);
        int count = service.calculate(3L, "admin@acme.test");
        assertTrue(count >= 16);
        PayRun run = new PayRunDao(ds).findById(3L);
        assertEquals(PayRunStatus.CALCULATED, run.getStatus());

        List<Payslip> slips = new PayslipDao(ds).findByPayRun(3L);
        assertEquals(count, slips.size());
        for (Payslip p : slips) {
            assertEquals(p.getNet(), p.getGross().subtract(p.getTotalDeductions()));
        }

        service.approve(3L, "admin@acme.test");
        assertEquals(PayRunStatus.APPROVED, new PayRunDao(ds).findById(3L).getStatus());
        service.markPaid(3L, "admin@acme.test");
        assertEquals(PayRunStatus.PAID, new PayRunDao(ds).findById(3L).getStatus());
    }

    @Test
    public void paidPayRunIsLocked() {
        PayRunService service = new PayRunService(ds);
        try {
            service.calculate(1L, "admin@acme.test");
            fail("expected BusinessException");
        } catch (BusinessException expected) {
            // ok
        }
        try {
            service.delete(1L, "admin@acme.test");
            fail("expected BusinessException");
        } catch (BusinessException expected) {
            // ok
        }
    }

    @Test
    public void overlappingPayRunIsRejected() {
        PayRunService service = new PayRunService(ds);
        try {
            service.create(DateUtil.parse("2026-10-15"), DateUtil.parse("2026-11-14"),
                    DateUtil.parse("2026-11-14"), "admin@acme.test");
            fail("expected BusinessException");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("overlapping"));
        }
    }
}
