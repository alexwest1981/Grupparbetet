package Wigell.Sushi.API.service;

import Wigell.Sushi.API.entity.Booking;
import Wigell.Sushi.API.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final CurrencyService currencyService;

    public BookingService(BookingRepository bookingRepository, CurrencyService currencyService) {
        this.bookingRepository = bookingRepository;
        this.currencyService = currencyService;
    }

    public Booking createBooking(Booking booking) {
        // 1. Beräkna priset i JPY via externa tjänsten
        double priceJpy = currencyService.convertSekToJpy(booking.getTotalPriceSek());
        booking.setTotalPriceJpy(priceJpy);

        // 2. Spara i databasen
        Booking savedBooking = bookingRepository.save(booking);

        // 3. Logga till fil [cite: 121]
        logger.info("New booking created for customer: {}. Total: {} SEK / {} JPY",
                savedBooking.getCustomer().getUsername(),
                savedBooking.getTotalPriceSek(),
                savedBooking.getTotalPriceJpy());

        return savedBooking;
    }
}