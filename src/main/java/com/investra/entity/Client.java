package com.investra.entity;

import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;
import com.investra.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "vergi_no", unique = true)
    private String vergiNo;

    @Column(name = "tckn", unique = true)
    private String tckn;

    @Column(name = "passport_no", unique = true)
    private String passportNo;

    @Column(name = "blue_card_no", unique = true)
    private String blueCardNo;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "nationality_type") // true: TC, false: Yabancı
    private Boolean nationalityType;

    @Column(name = "email")
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "education_status") // VARCHAR/TEXT
    private String educationStatus;

    @Column(name = "monthly_income")
    private String monthlyIncome;

    @Column(name = "estimated_transaction_volume")
    private String estimatedTransactionVolume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClientStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "client_type")
    @Enumerated(EnumType.STRING)
    private ClientType clientType;

    @Column(name = "company_type")
    private String companyType;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Portfolio> portfolios = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeOrder> tradeOrders = new ArrayList<>();

}
