package com.example.springboot.controller;

import com.example.springboot.common.AuthContext;
import com.example.springboot.common.BaseController;
import com.example.springboot.common.Result;
import com.example.springboot.dto.request.AdminRequest;
import com.example.springboot.dto.response.AdminView;
import com.example.springboot.entity.Admin;
import com.example.springboot.service.AdminService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
public class AdminController extends BaseController<Admin, AdminRequest, AdminView> {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        super(adminService);
        this.adminService = adminService;
    }

    /** 通用 DELETE 会绕过「不能删自己 / 最后一个管理员」守卫，这里显式接住 */
    @Override
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        adminService.deleteGuarded(id, AuthContext.userId());
        return Result.success();
    }

    @Override
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Integer> ids) {
        adminService.deleteBatchGuarded(ids, AuthContext.userId());
        return Result.success();
    }

    @Override
    protected Admin toEntity(AdminRequest request) {
        Admin admin = new Admin();
        admin.setId(request.getId());
        admin.setUsername(request.getUsername());
        admin.setPassword(request.getPassword());
        admin.setRole(request.getRole());
        admin.setName(request.getName());
        admin.setNewPassword(request.getNewPassword());
        admin.setToken(request.getToken());
        admin.setAvatar(request.getAvatar());
        admin.setPhone(request.getPhone());
        return admin;
    }

    @Override
    protected AdminView toView(Admin admin) {
        AdminView view = new AdminView();
        view.setId(admin.getId());
        view.setUsername(admin.getUsername());
        view.setRole(admin.getRole());
        view.setName(admin.getName());
        view.setAvatar(admin.getAvatar());
        view.setPhone(admin.getPhone());
        return view;
    }
}
