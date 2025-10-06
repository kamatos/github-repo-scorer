package com.gerard.githubreposcorer.service;

import com.gerard.githubreposcorer.scoring.model.ScoringContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoringService {

    public BigDecimal calculateScore(ScoringContext context) {
       return null;
    }
}