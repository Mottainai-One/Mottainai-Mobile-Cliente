package com.mottainai.cliente.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mottainai.cliente.network.dto.LoyaltyAccount;
import com.mottainai.cliente.network.dto.LoyaltyTransaction;
import com.mottainai.cliente.repository.LoadState;
import com.mottainai.cliente.repository.LoyaltyRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LoyaltyViewModel extends AndroidViewModel {
    private final LoyaltyRepository repository;
    private final MutableLiveData<LoadState<LoyaltyAccount>> account = new MutableLiveData<>();
    private final MutableLiveData<LoadState<List<LoyaltyTransaction>>> transactions = new MutableLiveData<>();
    private int historyRequest;

    public LoyaltyViewModel(@NonNull Application application) {
        super(application);
        repository = new LoyaltyRepository(application);
    }

    public LiveData<LoadState<LoyaltyAccount>> account() { return account; }
    public LiveData<LoadState<List<LoyaltyTransaction>>> transactions() { return transactions; }

    public void loadAccount(String id) {
        account.setValue(LoadState.loading());
        repository.account(id, account::setValue);
    }

    public void loadRecentTransactions(String id) {
        int request = ++historyRequest;
        transactions.setValue(LoadState.loading());
        List<LoyaltyTransaction> collected = new ArrayList<>();
        int[] remaining = {6};
        LoadState<?>[] firstError = {null};
        LocalDate month = LocalDate.now().withDayOfMonth(1).minusMonths(5);
        for (int i = 0; i < 6; i++) {
            repository.transactions(id, month.plusMonths(i), state -> {
                if (request != historyRequest) return;
                if (state.status == LoadState.Status.SUCCESS) {
                    collected.addAll(state.data);
                } else if (firstError[0] == null) {
                    firstError[0] = state;
                }
                if (--remaining[0] == 0) {
                    if (firstError[0] != null) {
                        transactions.setValue(LoadState.error(firstError[0].message,
                                firstError[0].httpStatus));
                    } else {
                        collected.sort(Comparator.comparing(
                                (LoyaltyTransaction item) -> item.createdAt == null ? "" : item.createdAt)
                                .reversed());
                        transactions.setValue(LoadState.success(collected));
                    }
                }
            });
        }
    }
}
