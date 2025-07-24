package me.ronygomes.ums.api.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Repository
public class RegistrationNumberRepository {

    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy");

    private static final String INCREMENT_QUERY = """
            MERGE INTO registration_number_bounds AS rnb
            USING (
            	SELECT id FROM departments WHERE code = ?
            ) AS params(dept_id)
            ON (rnb.department_id = params.dept_id AND rnb.year = ?)
            WHEN matched THEN
              UPDATE SET last_used_number = last_used_number + 1
            WHEN NOT matched THEN
              INSERT (year, department_id, last_used_number)
              VALUES (?, params.dept_id, 1)
            """;

    private static final String SELECT_QUERY = """
            SELECT year || '-' || d.code || '-' || LPAD(last_used_number::text, 4, '0') AS reg_number
            FROM registration_number_bounds rnb
            JOIN departments d ON d.id = rnb.department_id
            WHERE d.code = ? AND rnb.year = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public RegistrationNumberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getNextId(Date registrationDate, String departmentCode) {
        int year = Integer.parseInt(FORMATTER.format(registrationDate));
        jdbcTemplate.update(INCREMENT_QUERY, departmentCode, year, year);
        return jdbcTemplate.queryForObject(SELECT_QUERY, String.class, departmentCode, year);
    }
}
