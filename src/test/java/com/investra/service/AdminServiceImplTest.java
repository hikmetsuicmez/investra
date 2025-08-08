package com.investra.service;

import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.repository.UserRepository;
import com.investra.service.impl.AdminServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    public void updateUser_ShouldReturn404_WhenUserNotFound() {
        String employeeNumber = "EMP999";
        UpdateUserRequest request = new UpdateUserRequest();

        when(userRepository.findByEmployeeNumber(employeeNumber)).thenReturn(Optional.empty());

        Response<UpdateUserResponse> response = adminService.updateUser(employeeNumber, request);

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getMessage().contains("Güncellenecek kullanıcı bulunamadı"));
    }

    @Test
    public void deleteUser_ShouldReturn404_WhenUserNotFound() {
        String employeeNumber = "EMP404";
        when(userRepository.findByEmployeeNumber(employeeNumber)).thenReturn(Optional.empty());

        Response<Void> response = adminService.deleteUser(employeeNumber);

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getMessage().contains("Kullanıcı bulunamadı"));
    }
}