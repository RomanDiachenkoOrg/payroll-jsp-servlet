package com.acme.payroll.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/** Forces UTF-8 request decoding (Tomcat 6 used ISO-8859-1 for POST bodies). */
public class EncodingFilter implements Filter {

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig config) throws ServletException {
        String enc = config.getInitParameter("encoding");
        if (enc != null) {
            encoding = enc;
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        if (req.getCharacterEncoding() == null) {
            req.setCharacterEncoding(encoding);
        }
        resp.setCharacterEncoding(encoding);
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
        // nothing
    }
}
