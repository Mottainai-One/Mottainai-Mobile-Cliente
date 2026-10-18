package com.mottainai.cliente.repository;

/** Interprets the profile contract before allowing entry to authenticated screens. */
public enum ProfileSyncStatus {
    READY, PROFILE_REQUIRED, INVALID_RESPONSE, ERROR;

    public static ProfileSyncStatus from(int httpStatus, Integer customerId) {
        if (httpStatus == 404) return PROFILE_REQUIRED;
        if (httpStatus < 200 || httpStatus >= 300) return ERROR;
        return customerId != null && customerId > 0 ? READY : INVALID_RESPONSE;
    }
}
