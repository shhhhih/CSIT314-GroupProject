package com.team.idp.dto;

import java.math.BigDecimal;

public record DesignerIdpRequest(String title, String description,
                                 BigDecimal budget, String propertyType,
                                 String projectStatus, Long categoryId) { }
