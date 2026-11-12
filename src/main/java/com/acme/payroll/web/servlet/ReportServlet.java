package com.acme.payroll.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.payroll.dao.ReportDao;
import com.acme.payroll.model.DepartmentCost;
import com.acme.payroll.util.CsvWriter;
import com.acme.payroll.util.DateUtil;

/** Payroll cost by department for a date range (by pay date). */
public class ReportServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Date from = date(req, "from");
        Date to = date(req, "to");
        if (from == null) {
            from = DateUtil.parse(DateUtil.format(DateUtil.today()).substring(0, 4) + "-01-01");
        }
        if (to == null) {
            to = DateUtil.today();
        }
        List<DepartmentCost> rows = new ReportDao().costByDepartment(from, to);

        if ("/cost.csv".equals(action)) {
            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"payroll-cost-" + DateUtil.format(from)
                    + "_" + DateUtil.format(to) + ".csv\"");
            CsvWriter csv = new CsvWriter(resp.getWriter());
            csv.row("Department", "Headcount", "Gross", "Income tax", "Social", "Pension", "Net");
            for (DepartmentCost c : rows) {
                csv.row(c.getDepartmentName(), c.getHeadcount(), c.getGross(), c.getTax(), c.getSocial(),
                        c.getPension(), c.getNet());
            }
            csv.flush();
            return;
        }
        if (!"/".equals(action) && !"/cost".equals(action)) {
            throw new NotFoundException();
        }
        DepartmentCost total = new DepartmentCost();
        total.setDepartmentName("Total");
        total.setGross(BigDecimal.ZERO);
        total.setTax(BigDecimal.ZERO);
        total.setSocial(BigDecimal.ZERO);
        total.setPension(BigDecimal.ZERO);
        total.setNet(BigDecimal.ZERO);
        for (DepartmentCost c : rows) {
            total.setGross(total.getGross().add(c.getGross()));
            total.setTax(total.getTax().add(c.getTax()));
            total.setSocial(total.getSocial().add(c.getSocial()));
            total.setPension(total.getPension().add(c.getPension()));
            total.setNet(total.getNet().add(c.getNet()));
        }
        req.setAttribute("rows", rows);
        req.setAttribute("total", total);
        req.setAttribute("from", DateUtil.format(from));
        req.setAttribute("to", DateUtil.format(to));
        render("reports/cost", req, resp);
    }
}
