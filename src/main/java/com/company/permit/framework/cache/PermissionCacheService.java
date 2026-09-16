package com.company.permit.framework.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public void savePermissions(Long userId, Collection<String> perms) {
        String key = CacheKeys.permission(userId);
        stringRedisTemplate.delete(key);
        if (perms != null && !perms.isEmpty()) {
            stringRedisTemplate.opsForSet().add(key, perms.toArray(new String[0]));
        } else {
            stringRedisTemplate.opsForSet().add(key, "__empty__");
        }
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }

    public java.util.Set<String> getPermissions(Long userId) {
        String key = CacheKeys.permission(userId);
        Boolean exists = stringRedisTemplate.hasKey(key);
        if (!Boolean.TRUE.equals(exists)) {
            return null;
        }
        java.util.Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (members == null) {
            return java.util.Collections.emptySet();
        }
        members.remove("__empty__");
        return members;
    }

    public void evict(Long userId) {
        stringRedisTemplate.delete(CacheKeys.permission(userId));
        stringRedisTemplate.delete(CacheKeys.dataScope(userId));
    }

    public void evictPermissions(Long userId) {
        stringRedisTemplate.delete(CacheKeys.permission(userId));
    }
}
