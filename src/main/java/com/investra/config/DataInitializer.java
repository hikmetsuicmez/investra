package com.investra.config;

import com.investra.entity.*;
import com.investra.enums.*;
import com.investra.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public void run(String... args) throws Exception {

        portfolioItemRepository.deleteAll();
        tradeOrderRepository.deleteAll();
        transactionRepository.deleteAll();
        portfolioRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();
        userRepository.deleteAll();

        // Admin kullanıcısı
        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@investra.com")
                .role(Role.ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(admin);

        User trader = User.builder()
                .firstName("Trader")
                .lastName("Trade")
                .password(passwordEncoder.encode("trader123"))
                .email("trader@investra.com")
                .role(Role.TRADER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(trader);

        // Müşteri Oluşturma
        Client client = Client.builder()
                .user(admin)
                .fullName("John Doe")
                .nationalityNumber("12345678901")
                .taxId("9876543210")
                .blueCardNo("1234567890")
                .email("johndoe@gmail.com")
                .phone("555-1234")
                .isActive(true)
                .status(ClientStatus.ACTIVE)
                .clientType(ClientType.INDIVIDUAL)
                .createdAt(LocalDateTime.now())
                .build();

        clientRepository.save(client);

        // Müşteriye ait portföy oluşturma
        Portfolio portfolio = Portfolio.builder()
                .client(client)
                .createdAt(LocalDateTime.now())
                .build();

        portfolioRepository.save(portfolio);

        // Müşteriye takas hesabı oluşturma
        Account takasAccount = Account.builder()
                .client(client)
                .nickname("Ana Takas Hesabı")  // nickName -> nickname
                .iban("TR123456789012345678901234")
                .accountNumberAtBroker("TAKAS001")
                .brokerName("Örnek Menkul Değerler")
                .brokerCode("ORNEK001")
                .custodianName("Takasbank")
                .custodianCode("TKSBNK")
                .accountType(AccountType.SETTLEMENT)
                .balance(new BigDecimal("100000.00"))
                .availableBalance(new BigDecimal("100000.00"))  // Bu alan eksikti
                .isPrimarySettlement(true)
                .createdAt(LocalDateTime.now())
                .currency(Currency.TRY)
                .build();

        accountRepository.save(takasAccount);


    }
}
