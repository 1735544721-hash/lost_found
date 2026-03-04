package org.example.springboot.util;

import org.example.springboot.exception.ServiceException;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 数据验证工具类
 * 提供各种业务数据的验证方法
 */
public class ValidationUtils {
    
    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    /**
     * 验证字符串不为空且长度在指定范围内
     */
    public static void validateStringLength(String value, String fieldName, int minLength, int maxLength) {
        if (!StringUtils.hasText(value)) {
            throw new ServiceException(fieldName + "不能为空");
        }
        
        if (value.trim().length() < minLength) {
            throw new ServiceException(fieldName + "长度不能少于" + minLength + "个字符");
        }
        
        if (value.trim().length() > maxLength) {
            throw new ServiceException(fieldName + "长度不能超过" + maxLength + "个字符");
        }
    }
    
    /**
     * 验证手机号格式
     */
    public static void validatePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new ServiceException("联系电话不能为空");
        }
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ServiceException("联系电话格式不正确");
        }
    }
    
    /**
     * 验证邮箱格式
     */
    public static void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ServiceException("邮箱不能为空");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ServiceException("邮箱格式不正确");
        }
    }
    
    /**
     * 验证时间不能是未来时间
     */
    public static void validateNotFutureTime(LocalDateTime time, String fieldName) {
        if (time == null) {
            throw new ServiceException(fieldName + "不能为空");
        }
        
        if (time.isAfter(LocalDateTime.now())) {
            throw new ServiceException(fieldName + "不能是未来时间");
        }
    }
    
    /**
     * 验证时间在合理范围内（不能太久远）
     */
    public static void validateReasonableTime(LocalDateTime time, String fieldName, int maxDaysAgo) {
        validateNotFutureTime(time, fieldName);
        
        LocalDateTime minTime = LocalDateTime.now().minusDays(maxDaysAgo);
        if (time.isBefore(minTime)) {
            throw new ServiceException(fieldName + "不能超过" + maxDaysAgo + "天前");
        }
    }
    
    /**
     * 验证ID不为空且大于0
     */
    public static void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ServiceException(fieldName + "无效");
        }
    }
    
    /**
     * 验证状态值在有效范围内
     */
    public static void validateStatus(Integer status, int minStatus, int maxStatus) {
        if (status == null) {
            throw new ServiceException("状态不能为空");
        }
        
        if (status < minStatus || status > maxStatus) {
            throw new ServiceException("状态值无效，必须在" + minStatus + "到" + maxStatus + "之间");
        }
    }
    
    /**
     * 验证图片路径格式
     */
    public static void validateImagePaths(String images) {
        if (StringUtils.hasText(images)) {
            String[] imagePaths = images.split(",");
            if (imagePaths.length > 5) {
                throw new ServiceException("最多只能上传5张图片");
            }
            
            for (String path : imagePaths) {
                if (!StringUtils.hasText(path.trim())) {
                    throw new ServiceException("图片路径不能为空");
                }
                // 可以添加更多图片路径格式验证
            }
        }
    }
    
    /**
     * 验证地点信息
     */
    public static void validateLocation(String location, String fieldName) {
        validateStringLength(location, fieldName, 2, 255);
        
        // 检查是否包含敏感词或无效字符
        if (location.contains("<script>") || location.contains("javascript:")) {
            throw new ServiceException(fieldName + "包含无效字符");
        }
    }
    
    /**
     * 验证描述内容
     */
    public static void validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            throw new ServiceException("描述不能为空");
        }
        
        if (description.trim().length() < 10) {
            throw new ServiceException("描述内容过于简单，请详细描述物品特征");
        }
        
        if (description.length() > 1000) {
            throw new ServiceException("描述内容不能超过1000个字符");
        }
        
        // 检查敏感词
        if (containsSensitiveWords(description)) {
            throw new ServiceException("描述内容包含敏感词汇");
        }
    }
    
    /**
     * 简单的敏感词检查
     */
    private static boolean containsSensitiveWords(String content) {
        String[] sensitiveWords = {"<script>", "javascript:", "eval(", "alert(", "document.cookie"};
        String lowerContent = content.toLowerCase();
        
        for (String word : sensitiveWords) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
