package com.company.permit.framework.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OnlineUserCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public void add(Long userId) {
        stringRedisTemplate.opsForZSet().add(CacheKeys.ONLINE_USERS, String.valueOf(userId), System.currentTimeMillis());
    }

    public void remove(Long userId) {
        stringRedisTemplate.opsForZSet().remove(CacheKeys.ONLINE_USERS, String.valueOf(userId));
    }

    public long size() {
        Long size = stringRedisTemplate.opsForZSet().zCard(CacheKeys.ONLINE_USERS);
        return size == null ? 0 : size;
    }

    public List<Long> page(long offset, long count) {
        Set<String> ids = stringRedisTemplate.opsForZSet().reverseRange(CacheKeys.ONLINE_USERS, offset, offset + count - 1);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>();
        for (String id : ids) {
            result.add(Long.valueOf(id));
        }
        return result;
    }
}
