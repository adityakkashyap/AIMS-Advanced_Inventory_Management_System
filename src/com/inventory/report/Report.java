package com.inventory.report;

import java.util.Map;

public interface Report {
    String generateReport(Map<String, Object> data);
}
