package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;

import org.example.springboot.entity.User;
import org.example.springboot.DTO.UserPasswordUpdateDTO;
import org.example.springboot.enumClass.AccountStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    

    
    @Value("${user.defaultPassword}")
    private String DEFAULT_PWD;

    @Resource
    private PasswordEncoder bCryptPasswordEncoder;

    public User getByEmail(String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        return user;
    }

    public User login(User user) {
        User dbUser = getByUsername(user.getUsername());
        // 用户存在性检查已经在 getByUsername 中处理

        // 验证密码
        if (!bCryptPasswordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }

        // 完善的账户状态检查
        AccountStatus accountStatus = AccountStatus.fromValue(dbUser.getStatus());
        if (!accountStatus.canLogin()) {
            throw new ServiceException(accountStatus.getDescription() + ": " + accountStatus.getTransitionAdvice());
        }

        // 生成token
        String token = JwtTokenUtils.genToken(String.valueOf(dbUser.getId()), dbUser.getPassword());
        dbUser.setToken(token);

        // 记录登录时间（可选）
        // updateLastLoginTime(dbUser.getId());

        return dbUser;
    }

    public List<User> getUserByRole(String roleCode) {
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getRoleCode, roleCode)
        );
        if (users.isEmpty()) {
            throw new ServiceException("未找到该角色的用户");
        }
        return users;
    }

    public void createUser(User user) {
        // 检查用户名是否存在
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            ) != null) {
            throw new ServiceException("用户名已存在");
        }
        
        // 检查邮箱是否被使用
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail())
            ) != null) {
            throw new ServiceException("邮箱已被使用");
        }


        user.setPassword(StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : DEFAULT_PWD);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        
        if (userMapper.insert(user) <= 0) {
            throw new ServiceException("用户创建失败");
        }
    }

    /**
     * 更新用户状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status, String reason) {
        User user = getUserById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 验证状态值
        AccountStatus newStatus = AccountStatus.fromValue(status);
        AccountStatus currentStatus = AccountStatus.fromValue(user.getStatus());

        // 检查状态转换是否合法
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new ServiceException("不允许从 " + currentStatus.getDescription() + " 转换到 " + newStatus.getDescription());
        }

        user.setStatus(status);
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("状态更新失败");
        }

        // 记录状态变更日志（可选）
        // logStatusChange(userId, currentStatus, newStatus, reason);
    }

    /**
     * 检查用户状态转换是否合法
     */
    private boolean isValidStatusTransition(AccountStatus from, AccountStatus to) {
        switch (from) {
            case PENDING_REVIEW:
                return to == AccountStatus.ACTIVE || to == AccountStatus.REVIEW_FAILED;
            case REVIEW_FAILED:
                return to == AccountStatus.PENDING_REVIEW || to == AccountStatus.DISABLED;
            case ACTIVE:
                return to == AccountStatus.DISABLED || to == AccountStatus.LOCKED || to == AccountStatus.EXPIRED;
            case DISABLED:
                return to == AccountStatus.ACTIVE;
            case LOCKED:
                return to == AccountStatus.ACTIVE || to == AccountStatus.DISABLED;
            case EXPIRED:
                return to == AccountStatus.ACTIVE;
            default:
                return false;
        }
    }

    /**
     * 检查用户是否可以执行操作
     */
    public void checkUserCanOperate(User user) {
        if (user == null) {
            throw new ServiceException("用户未登录");
        }

        AccountStatus status = AccountStatus.fromValue(user.getStatus());
        if (!status.canOperate()) {
            throw new ServiceException("账户状态异常，无法执行操作: " + status.getTransitionAdvice());
        }
    }

    public void updateUser(Long id, User user) {
        // 检查用户是否存在
        if (userMapper.selectById(id) == null) {
            throw new ServiceException("要更新的用户不存在");
        }
        
        // 检查新用户名是否与其他用户重复
        if (user.getUsername() != null) {
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            );
            if (existUser != null && !existUser.getId().equals(id)) {
                throw new ServiceException("新用户名已被使用");
            }
        }
        
        user.setId(id);
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("用户更新失败");
        }
    }

    public User getByUsername(String username) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    public void deleteBatch(List<Integer> ids) {
        if (userMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }

    public List<User> getUserList() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        if (users.isEmpty()) {
            throw new ServiceException("未找到任何用户");
        }
        return users;
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    public Page<User> getUsersByPage(String username,  String name, String roleCode, Integer currentPage, Integer size) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }

        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(User::getName, name);
        }
        if (StringUtils.isNotBlank(roleCode)) {
            queryWrapper.eq(User::getRoleCode, roleCode);
        }
        
        return userMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    public void updatePassword(Long id, UserPasswordUpdateDTO update) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证旧密码
        if (!bCryptPasswordEncoder.matches(update.getOldPassword(), user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        
        // 更新新密码
        user.setPassword(bCryptPasswordEncoder.encode(update.getNewPassword()));
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码修改失败");
        }
    }

    public void forgetPassword(String email, String newPassword) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码重置失败");
        }
    }

    public void deleteUserById(Long id) {
        if (userMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
}
