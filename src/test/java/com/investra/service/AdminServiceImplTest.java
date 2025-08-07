package com.investra.service;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.entity.User;
import com.investra.enums.Role;
import com.investra.repository.UserRepository;
import com.investra.service.impl.AdminServiceImpl;
import com.investra.utils.EmployeeNumberGenerator;
import com.investra.utils.PasswordGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmployeeNumberGenerator employeeNumberGenerator;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
@Test
public void createUser_ShouldReturnSuccess_WhenValidRequest() {
    CreateUserRequest request = new CreateUserRequest();
    request.setEmail("test@example.com");
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setNationalityNumber("12345678901");
    request.setPhoneNumber("05551234567");
    request.setRole(Role.ADMIN);

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(userRepository.findByNationalityNumber(request.getNationalityNumber())).thenReturn(Optional.empty());
    when(employeeNumberGenerator.generateNext()).thenReturn("EMP123");
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(emailTemplateService.processTemplate(anyString(), anyMap())).thenReturn("Welcome Email Content");

    Response<CreateUserResponse> response = adminService.createUser(request);
    System.out.println("EmployeeNumber: " + response.getData().getEmployeeNumber());
    CreateUserResponse data = response.getData();
    System.out.println("response.data = " + data);
    System.out.println("response.data.employeeNumber = " + data.getEmployeeNumber());

    assertNotNull(response);
    assertEquals(201, response.getStatusCode());
    assertEquals("Kullanıcı başarıyla eklendi", response.getMessage());
    assertNotNull(response.getData());
    assertEquals("EMP123", response.getData().getEmployeeNumber());
    verify(userRepository).save(any(User.class));
}

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