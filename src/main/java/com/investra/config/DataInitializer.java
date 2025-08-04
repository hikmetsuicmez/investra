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
    private final StockRepository stockRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    public void run(String... args) throws Exception {

        portfolioItemRepository.deleteAll();
        tradeOrderRepository.deleteAll();
        portfolioRepository.deleteAll();
        accountRepository.deleteAll();
        stockRepository.deleteAll();
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

        // Müşteri Oluşturma
        Client client = Client.builder()
                .user(admin)
                .fullName("John Doe")
                .nationalityNumber("12345678901")
                .taxId("9876543210")
                .blueCardNo("1234567890")
                .email("johndoe@gmail.com")
                .phone("555-1234")
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
                .accountNumber("TR12345678901234567890")
                .accountType(AccountType.SETTLEMENT)
                .balance(new BigDecimal("100000.00"))
                .createdAt(LocalDateTime.now())
                .currency(Currency.TRY)
                .build();

        accountRepository.save(takasAccount);

        // hisse senedi oluşturma
        Stock stock1 = Stock.builder()
                .name("ABC Teknoloji")
                .symbol("ABCTK")
                .code("ABCTK")  // Hisse kodu
                .sector("Teknoloji")  // Sektör bilgisi
                .exchangeCode("BIST")  // Borsa kodu
                .group(StockGroup.TECHNOLOGY)
                .price(new BigDecimal("150.00"))  // currentPrice -> price olarak değişti
                .isActive(true)
                .source(StockSource.BIST)
                .createdAt(LocalDateTime.now())
                .build();

        Stock stock2 = Stock.builder()
                .name("Garanti Bankası")
                .symbol("GARAN")
                .code("GARAN")  // Hisse kodu
                .sector("Bankacılık")  // Sektör bilgisi
                .exchangeCode("BIST")  // Borsa kodu
                .group(StockGroup.FINANCE)
                .price(new BigDecimal("25.30"))  // currentPrice -> price olarak değişti
                .isActive(true)
                .source(StockSource.BIST)
                .createdAt(LocalDateTime.now())
                .build();

        stockRepository.save(stock1);
        stockRepository.save(stock2);

        PortfolioItem item1 = PortfolioItem.builder()
                .portfolio(portfolio)
                .account(takasAccount)
                .stock(stock2)
                .quantity(100)
                .avgPrice(new BigDecimal("23.50"))
                .lastUpdated(LocalDateTime.now())
                .build();

        PortfolioItem item2 = PortfolioItem.builder()
                .portfolio(portfolio)
                .account(takasAccount)
                .stock(stock1)
                .quantity(100)
                .avgPrice(new BigDecimal("23.50"))
                .lastUpdated(LocalDateTime.now())
                .build();

        portfolioItemRepository.save(item1);
        portfolioItemRepository.save(item2);

    }
}
