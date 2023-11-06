package hu.webuni.booking.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.webuni.bonus.api.BonusApi;
import hu.webuni.booking.dto.PurchaseData;
import hu.webuni.booking.dto.TicketData;
import hu.webuni.currency.api.CurrencyApi;
import hu.webuni.flights.api.FlightsApi;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Value("${booking.bonus}")
    double bonusRate;
    
    @Autowired
    BonusApi bonusApi;
    @Autowired
    CurrencyApi currencyApi;
    @Autowired
    FlightsApi flightsApi;

    @PostMapping("/ticket")
    public PurchaseData buyTicket(@RequestBody TicketData ticketData) {
    	
    	System.out.println(bonusApi.getPoints(ticketData.getUser()));
    	System.out.println(currencyApi.getRate("USD", "HUF"));
    	System.out.println(flightsApi.searchFlight(ticketData.getFrom(), ticketData.getTo()));
    	
        return null;
    }
}
