package com.investra.entity;

import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;
import com.investra.enums.EstimatedTransactionVolume;
import com.investra.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ortak Alanlar
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false)
    private ClientType clientType; // BIREYSEL veya KURUMSAL

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClientStatus status;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // bireysel müşteri
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "nationality_type") // true: TC, false: Yabancı
    private Boolean nationalityType;

    @Column(name = "tax_id", unique = true)
    private String taxId; // T.C. kimlik no

    @Column(name = "passport_no", unique = true)
    private String passportNo;

    @Column(name = "blue_card_no", unique = true)
    private String blueCardNo;

    @Column(name = "nationality_number", unique = true)
    private String nationalityNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "profession")
    private String profession;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "education_status")
    private String educationStatus;

    @Column(name = "monthly_income")
    private BigDecimal monthlyIncome;

    @Column(name = "estimated_transaction_volume")
    @Enumerated(EnumType.STRING)
    private EstimatedTransactionVolume estimatedTransactionVolume;


    // kurumsal müşteri
    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tax_number", unique = true)
    private String taxNumber;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    @Column(name = "company_type")
    private String companyType;

    @Column(name = "sector")
    private String sector;

    @Column(name = "monthly_revenue")
    private BigDecimal monthlyRevenue;

    // İLİŞKİLER
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Portfolio> portfolios = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeOrder> tradeOrders = new ArrayList<>();
}
