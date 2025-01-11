package com.web.book.version.service;

import org.springframework.stereotype.Service;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 30;
    
    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(BLOCK_DURATION_MINUTES, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Integer>() {
                @Override
                public Integer load(String key) {
                    return 0;
                }
            });
    }

    public void loginFailed(String ip) {
        try {
            int attempts = attemptsCache.get(ip);
            attemptsCache.put(ip, attempts + 1);
        } catch (ExecutionException e) {
            attemptsCache.put(ip, 1);
        }
    }

    public boolean isBlocked(String ip) {
        try {
            return attemptsCache.get(ip) >= MAX_ATTEMPTS;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public void loginSucceeded(String ip) {
        attemptsCache.invalidate(ip);
    }
}