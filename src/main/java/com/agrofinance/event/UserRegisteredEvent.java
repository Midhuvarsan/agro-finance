package com.agrofinance.event;
 
/** Published by AuthService after a successful registration. */
public record UserRegisteredEvent(Long userId, String email) {
}
