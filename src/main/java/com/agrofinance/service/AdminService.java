package com.agrofinance.service;
 
import com.agrofinance.dto.AdminUserResponse;
import com.agrofinance.dto.DashboardResponse;
import com.agrofinance.dto.OfficerAdminResponse;
import com.agrofinance.dto.PageResponse;
import com.agrofinance.entity.UserStatus;
import org.springframework.data.domain.Pageable;
 
import java.util.List;
 
public interface AdminService {
 
    PageResponse<AdminUserResponse> listUsers(Pageable pageable);
 
    AdminUserResponse updateUserStatus(Long adminUserId, Long targetUserId, UserStatus status);
 
    List<OfficerAdminResponse> listOfficers();
 
    DashboardResponse dashboard();
 
}
 


























