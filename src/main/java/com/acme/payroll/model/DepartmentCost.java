package com.acme.payroll.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Payroll cost aggregated per department (report row).
 */
public class DepartmentCost implements Serializable {

    private static final long serialVersionUID = 1L;

    private String departmentName;
    private int headcount;
    private BigDecimal gross;
    private BigDecimal tax;
    private BigDecimal social;
    private BigDecimal pension;
    private BigDecimal net;

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getHeadcount() {
        return headcount;
    }

    public void setHeadcount(int headcount) {
        this.headcount = headcount;
    }

    public BigDecimal getGross() {
        return gross;
    }

    public void setGross(BigDecimal gross) {
        this.gross = gross;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getSocial() {
        return social;
    }

    public void setSocial(BigDecimal social) {
        this.social = social;
    }

    public BigDecimal getPension() {
        return pension;
    }

    public void setPension(BigDecimal pension) {
        this.pension = pension;
    }

    public BigDecimal getNet() {
        return net;
    }

    public void setNet(BigDecimal net) {
        this.net = net;
    }

    @Override
    public String toString() {
        return "DepartmentCost[id=" + departmentName + "]";
    }
}
