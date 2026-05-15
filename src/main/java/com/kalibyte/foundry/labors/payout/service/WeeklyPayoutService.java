package com.kalibyte.foundry.labors.payout.service;

import com.kalibyte.foundry.labors.payout.dto.DisbursePayoutRequest;
import com.kalibyte.foundry.labors.payout.dto.WeeklyPayoutRequest;
import com.kalibyte.foundry.labors.payout.dto.WeeklyPayoutResponse;

import java.util.List;

public interface WeeklyPayoutService {
    WeeklyPayoutResponse generateWeeklyPayout(WeeklyPayoutRequest request);
    WeeklyPayoutResponse disbursePayout(Long payoutId, DisbursePayoutRequest request);
    List<WeeklyPayoutResponse> getPayoutsByLaborer(Long laborerId);
}
