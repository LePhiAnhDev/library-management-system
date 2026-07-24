package com.library.domain.settings;

import com.library.domain.member.MembershipType;
import com.library.domain.settings.dto.LoanPolicyResponse;
import com.library.domain.settings.dto.LoanPolicyUpdateRequest;
import com.library.domain.settings.dto.SettingsResponse;
import com.library.domain.settings.dto.SettingsUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final LibrarySettingsRepository settingsRepository;
    private final LoanPolicyRepository loanPolicyRepository;

    @Override
    @Transactional(readOnly = true)
    public SettingsResponse getSettings() {
        return toResponse(getLibrarySettings());
    }

    @Override
    @Transactional(readOnly = true)
    public LibrarySettings getLibrarySettings() {
        return settingsRepository.findById(LibrarySettings.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Chưa cấu hình thư viện"));
    }

    @Override
    @Transactional(readOnly = true)
    public LoanPolicy getLoanPolicy(MembershipType membershipType) {
        return loanPolicyRepository.findById(membershipType)
                .orElseThrow(() -> new IllegalStateException(
                        "Chưa cấu hình chính sách mượn cho loại thẻ " + membershipType));
    }

    @Override
    @Transactional
    public SettingsResponse updateSettings(SettingsUpdateRequest request) {
        LibrarySettings settings = getLibrarySettings();
        settings.setLibraryName(request.libraryName());
        settings.setLibraryAddress(request.libraryAddress());
        settings.setOverdueFinePerDay(request.overdueFinePerDay());
        settings.setFineBlockThreshold(request.fineBlockThreshold());
        settings.setReservationHoldDays(request.reservationHoldDays());
        settings.setLostDefaultFee(request.lostDefaultFee());
        settings.setDamagedDefaultFee(request.damagedDefaultFee());
        settings.setUpdatedAt(Instant.now());
        return toResponse(settingsRepository.save(settings));
    }

    @Override
    @Transactional
    public LoanPolicyResponse updateLoanPolicy(MembershipType membershipType, LoanPolicyUpdateRequest request) {
        LoanPolicy policy = getLoanPolicy(membershipType);
        policy.setMaxBooks(request.maxBooks());
        policy.setLoanPeriodDays(request.loanPeriodDays());
        policy.setMaxRenewals(request.maxRenewals());
        return toPolicyResponse(loanPolicyRepository.save(policy));
    }

    private SettingsResponse toResponse(LibrarySettings settings) {
        List<LoanPolicyResponse> policies = loanPolicyRepository.findAll(Sort.by("membershipType")).stream()
                .map(this::toPolicyResponse)
                .toList();
        return new SettingsResponse(
                settings.getLibraryName(),
                settings.getLibraryAddress(),
                settings.getOverdueFinePerDay(),
                settings.getFineBlockThreshold(),
                settings.getReservationHoldDays(),
                settings.getLostDefaultFee(),
                settings.getDamagedDefaultFee(),
                policies,
                settings.getUpdatedAt());
    }

    private LoanPolicyResponse toPolicyResponse(LoanPolicy policy) {
        return new LoanPolicyResponse(
                policy.getMembershipType().name(),
                policy.getMaxBooks(),
                policy.getLoanPeriodDays(),
                policy.getMaxRenewals());
    }
}
