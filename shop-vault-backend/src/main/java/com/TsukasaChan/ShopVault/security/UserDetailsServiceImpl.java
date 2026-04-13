package com.TsukasaChan.ShopVault.security;

import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, identifier)
                .or()
                .eq(User::getEmail, identifier));

        if (user == null) {
            throw new UsernameNotFoundException("邮箱不存在");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new DisabledException("账户已被暂停");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
        }

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                authorities,
                user.getId()
        );
    }
}
