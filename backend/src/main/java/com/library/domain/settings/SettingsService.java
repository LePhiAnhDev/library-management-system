package com.library.domain.settings;

import com.library.domain.member.MembershipType;
import com.library.domain.settings.dto.LoanPolicyResponse;
import com.library.domain.settings.dto.LoanPolicyUpdateRequest;
import com.library.domain.settings.dto.SettingsResponse;
import com.library.domain.settings.dto.SettingsUpdateRequest;

public interface SettingsService {

    SettingsResponse getSettings();

    SettingsResponse updateSettings(SettingsUpdateRequest request);

    LoanPolicyResponse updateLoanPolicy(MembershipType membershipType, LoanPolicyUpdateRequest request);

    /**
     * Library configuration entity for internal use by other services (fine rates, thresholds).
     */
    LibrarySettings getLibrarySettings();

    /**
     * Loan policy for a membership type for internal use (limits, period, renewals).
     */
    LoanPolicy getLoanPolicy(MembershipType membershipType);
}
