package com.agrofinance.event;
 
/** Published by LoanSchemeService when an admin adds a new scheme. */
public record SchemeCreatedEvent(String schemeName, String description) {
}
