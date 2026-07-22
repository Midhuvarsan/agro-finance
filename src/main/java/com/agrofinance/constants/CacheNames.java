package com.agrofinance.constants;
 
/**
 * Cache name constants — the constants/ package's first real use.
 * @Cacheable("laonSchemes") (typo) would silently create a separate,
 * never-evicted cache; CacheNames.LOAN_SCHEMES cannot be misspelled
 * without a compile error.
 */
public final class CacheNames {
 
    public static final String LOAN_SCHEMES = "loanSchemes";
    public static final String DASHBOARD = "dashboard";
    public static final String WEATHER = "weather";

    private CacheNames() {
    }
 
}