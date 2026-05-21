package com.campus.literature.config;

import com.campus.literature.entity.User;
import com.campus.literature.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化：将初始明文密码加密为 BCrypt 格式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        User admin = userMapper.selectByUsername("admin");
        if (admin != null && admin.getPasswordHash() != null && !admin.getPasswordHash().startsWith("$2a$")) {
            admin.setPasswordHash(passwordEncoder.encode(admin.getPasswordHash()));
            userMapper.updateById(admin);
            log.info("管理员账号密码已自动加密为 BCrypt 格式");
        }
    }
}
