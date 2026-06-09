package com.app.obsession.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.app.obsession.order.application.command.CreateOrderCommand;
import com.app.obsession.order.application.port.OrderRepository;
import com.app.obsession.order.domain.Order;
import com.app.obsession.product.application.port.ProductRepository;
import com.app.obsession.product.application.port.ProductStockRepository;
import com.app.obsession.product.domain.Product;
import com.app.obsession.product.domain.ProductStatus;
import com.app.obsession.product.domain.ProductStock;
import com.app.obsession.product.exception.ProductErrorCode;
import com.app.obsession.product.exception.ProductException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class CreateOrderProcessorTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductStockRepository productStockRepository;

    private CreateOrderProcessor createOrderProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createOrderProcessor = new CreateOrderProcessor(
                orderRepository,
                productRepository,
                productStockRepository
        );
    }

    @Test
    @DisplayName("주문 생성 성공 시 재고를 예약하고 주문을 저장한다")
    void create_success() {
        Long memberId = 1L;
        Long productId = 10L;

        Product product = Product.create(
                2L,
                "테스트 상품",
                "테스트 상품 설명",
                BigDecimal.valueOf(10_000),
                ProductStatus.ON_SALE
        );
        ReflectionTestUtils.setField(product, "id", productId);

        ProductStock stock = ProductStock.create(productId, 10);

        CreateOrderCommand command = new CreateOrderCommand(
                memberId,
                List.of(new CreateOrderCommand.OrderLineCommand(productId, 2))
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productStockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(orderRepository.save(org.mockito.Mockito.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long result = createOrderProcessor.create(command);

        assertThat(result).isNull();
        assertThat(stock.getReservedQuantity()).isEqualTo(2);
        assertThat(stock.availableQuantity()).isEqualTo(8);
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 PRODUCT_NOT_FOUND 예외")
    void create_productNotFound() {
        Long productId = 10L;

        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderCommand.OrderLineCommand(productId, 1))
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createOrderProcessor.create(command))
                .isInstanceOf(ProductException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("판매 중이 아닌 상품이면 NOT_ON_SALE_PRODUCT 예외")
    void create_notOnSaleProduct() {
        Long productId = 10L;

        Product product = Product.create(
                2L,
                "테스트 상품",
                "테스트 상품 설명",
                BigDecimal.valueOf(10_000),
                ProductStatus.HIDDEN
        );

        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderCommand.OrderLineCommand(productId, 1))
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> createOrderProcessor.create(command))
                .isInstanceOf(ProductException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.NOT_ON_SALE_PRODUCT);
    }

    @Test
    @DisplayName("재고가 부족하면 INSUFFICIENT_STOCK 예외")
    void create_insufficientStock() {
        Long productId = 10L;

        Product product = Product.create(
                2L,
                "테스트 상품",
                "테스트 상품 설명",
                BigDecimal.valueOf(10_000),
                ProductStatus.ON_SALE
        );

        ProductStock stock = ProductStock.create(productId, 1);

        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderCommand.OrderLineCommand(productId, 2))
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productStockRepository.findByProductId(product.getId())).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> createOrderProcessor.create(command))
                .isInstanceOf(ProductException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.INSUFFICIENT_STOCK);
    }
}
