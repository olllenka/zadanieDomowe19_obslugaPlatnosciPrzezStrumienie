package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PaymentService {

    private PaymentRepository paymentRepository;
    private DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    List<Payment> findPaymentsSortedByDateDesc() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .sorted((pay1, pay2) -> -pay1.getPaymentDate().compareTo(pay2.getPaymentDate()))
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForCurrentMonth() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay -> pay.getPaymentDate().getYear() == dateTimeProvider.yearMonthNow().getYear())
                .filter(pay -> pay.getPaymentDate().getMonthValue() == dateTimeProvider.yearMonthNow().getMonthValue())
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay -> pay.getPaymentDate().getYear() == yearMonth.getYear())
                .filter(pay -> pay.getPaymentDate().getMonthValue() == yearMonth.getMonthValue())
                .collect(Collectors.toList());
    }

    List<Payment> findPaymentsForGivenLastDays(int days) {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay -> pay.getPaymentDate().plusDays(days).isAfter(dateTimeProvider.zonedDateTimeNow()))
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithOnePaymentItem() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay -> pay.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    Set<String> findProductsSoldInCurrentMonth() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay -> pay.getPaymentDate().getYear() == dateTimeProvider.yearMonthNow().getYear())
                .filter(pay -> pay.getPaymentDate().getMonthValue() == dateTimeProvider.yearMonthNow().getMonthValue())
                .flatMap(new Function<Payment, Stream<PaymentItem>>() {
                    @Override
                    public Stream<PaymentItem> apply(Payment payment) {
                        return payment.getPaymentItems().stream();
                    }
                })
                .map(item -> item.getName())
                .collect(Collectors.toSet());
    }

    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay -> pay.getPaymentDate().getYear() == yearMonth.getYear())
                .filter(pay -> pay.getPaymentDate().getMonthValue() == yearMonth.getMonthValue())
                .flatMap(new Function<Payment, Stream<PaymentItem>>() {
                    @Override
                    public Stream<PaymentItem> apply(Payment payment) {
                        return payment.getPaymentItems().stream();
                    }
                })
                .map(item -> item.getFinalPrice())
                .reduce(BigDecimal.valueOf(0), new BinaryOperator<BigDecimal>() {
                    @Override
                    public BigDecimal apply(BigDecimal itemPrice, BigDecimal itemPrice2) {
                        return itemPrice.add(itemPrice2);
                    }
                });
    }

    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay -> pay.getPaymentDate().getYear() == yearMonth.getYear())
                .filter(pay -> pay.getPaymentDate().getMonthValue() == yearMonth.getMonthValue())
                .flatMap(new Function<Payment, Stream<PaymentItem>>() {
                    @Override
                    public Stream<PaymentItem> apply(Payment payment) {
                        return payment.getPaymentItems().stream();
                    }
                })
                .map(item -> item.getRegularPrice().subtract(item.getFinalPrice()))
                .reduce(BigDecimal.valueOf(0), new BinaryOperator<BigDecimal>() {
                    @Override
                    public BigDecimal apply(BigDecimal itemDiscount, BigDecimal itemDiscount2) {
                        return itemDiscount.add(itemDiscount2);
                    }
                });
    }

    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay -> pay.getUser().getEmail().equalsIgnoreCase(userEmail))
                .flatMap(new Function<Payment, Stream<PaymentItem>>() {
                    @Override
                    public Stream<PaymentItem> apply(Payment payment) {
                        return payment.getPaymentItems().stream();
                    }
                })
                .collect(Collectors.toList());
    }

    Set<Payment> findPaymentsWithValueOver(int value) {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(pay ->{
                    List<PaymentItem> itemsList = pay.getPaymentItems();
                    BigDecimal valueSum = BigDecimal.ZERO;
                    for (PaymentItem paymentItem : itemsList) {
                        valueSum=valueSum.add(paymentItem.getFinalPrice());
                    }
                    return valueSum.doubleValue()>value;
                })
                .collect(Collectors.toSet());
    }
}
