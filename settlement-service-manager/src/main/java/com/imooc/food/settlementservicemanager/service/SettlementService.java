package com.imooc.food.settlementservicemanager.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class SettlementService {

    Random rand = new Random(25);

    public Integer settlement(Integer accountId, BigDecimal amount) {
        return rand.nextInt(1000000000);
    }
}
