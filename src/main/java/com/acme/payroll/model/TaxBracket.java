package com.acme.payroll.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Progressive income tax bracket on monthly gross pay.
 */
public class TaxBracket implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
    private BigDecimal rate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(BigDecimal lowerBound) {
        this.lowerBound = lowerBound;
    }

    public BigDecimal getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(BigDecimal upperBound) {
        this.upperBound = upperBound;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getRatePercent() {
        return rate == null ? null : rate.movePointRight(2).stripTrailingZeros();
    }

    @Override
    public String toString() {
        return "TaxBracket[id=" + id + "]";
    }
}
