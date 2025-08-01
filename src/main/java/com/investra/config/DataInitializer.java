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
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Admin kullanıcı oluşturma

        portfolioItemRepository.deleteAll();
        portfolioRepository.deleteAll();
        stockRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder()
                .email("admin@investra.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(admin);

        // Mock müşteri oluşturma
        Client client = Client.builder()
                .fullName("Ahmet Yılmaz")
                .tckn("12345678901")
                .vergiNo("1234567890")
                .email("ahmet.yilmaz@email.com")
                .phone("5551234567")
                .status(ClientStatus.ACTIVE)
                .clientType("BIREYSEL")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        client = clientRepository.save(client);

        // Müşteriye ait portföy oluşturma
        Portfolio portfolio = Portfolio.builder()
                .client(client)
                .createdAt(LocalDateTime.now())
                .build();
        portfolio = portfolioRepository.save(portfolio);

        // Müşteriye takas hesabı oluşturma
        Account takasAccount = Account.builder()
                .client(client)
                .accountNumber("TKS123456")
                .currency(Currency.TRY)
                .balance(new BigDecimal("100000.00"))
                .accountType(AccountType.TAKAS)
                .isPrimaryTakas(true)
                .createdAt(LocalDateTime.now())
                .build();
        accountRepository.save(takasAccount);

        // Mock hisse senetleri oluşturma (güncellenmiş gruplarla)
        Stock stock1 = Stock.builder()
                .symbol("GARAN")
                .name("Garanti Bankası")
                .group(StockGroup.FINANCE)    // Finans grubu - Büyük banka
                .currentPrice(new BigDecimal("27.50"))
                .isActive(true)
                .source(StockSource.BIST)
                .createdAt(LocalDateTime.now())
                .build();
        stock1 = stockRepository.save(stock1);

        Stock stock2 = Stock.builder()
                .symbol("THYAO")
                .name("Türk Hava Yolları")
                .group(StockGroup.TECHNOLOGY)  // Teknoloji grubu - Büyük şirket
                .currentPrice(new BigDecimal("35.75"))
                .isActive(true)
                .source(StockSource.BIST)
                .createdAt(LocalDateTime.now())
                .build();
        stock2 = stockRepository.save(stock2);

        Stock stock3 = Stock.builder()
                .symbol("YKBNK")
                .name("Yapı Kredi Bankası")
                .group(StockGroup.FINANCE)    // Finans grubu
                .currentPrice(new BigDecimal("12.85"))
                .isActive(true)
                .source(StockSource.BIST)
                .createdAt(LocalDateTime.now())
                .build();
        stock3 = stockRepository.save(stock3);

        // Müşterinin portföyüne hisse senetleri ekleme
        PortfolioItem item1 = PortfolioItem.builder()
                .portfolio(portfolio)
                .account(takasAccount)
                .stock(stock1)
                .quantity(1000)
                .avgPrice(new BigDecimal("25.30"))
                .lastUpdated(LocalDateTime.now())
                .build();
        portfolioItemRepository.save(item1);

        PortfolioItem item2 = PortfolioItem.builder()
                .portfolio(portfolio)
                .account(takasAccount)
                .stock(stock2)
                .quantity(500)
                .avgPrice(new BigDecimal("33.40"))
                .lastUpdated(LocalDateTime.now())
                .build();
        portfolioItemRepository.save(item2);
    }
}
