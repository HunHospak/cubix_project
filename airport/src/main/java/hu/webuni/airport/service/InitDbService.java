package hu.webuni.airport.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.webuni.airport.aspect.LogCall;
import hu.webuni.airport.model.Address;
import hu.webuni.airport.model.Airport;
import hu.webuni.airport.model.AirportUser;
import hu.webuni.airport.model.Flight;
import hu.webuni.airport.repository.AddressRepository;
import hu.webuni.airport.repository.AirportRepository;
import hu.webuni.airport.repository.FlightRepository;
import hu.webuni.airport.repository.ImageRepository;
import hu.webuni.airport.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InitDbService {

	private final AirportRepository airportRepository;
	private final FlightService flightService;
	private final AddressRepository addressRepository;
	private final FlightRepository flightRepository;
	private final JdbcTemplate jdbcTemplate;
	private final ImageRepository imageRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	@LogCall
	public void deleteDb() {
	
		flightRepository.deleteAll();
		airportRepository.deleteAll();
		addressRepository.deleteAll();
	}

	@Transactional
	public void deleteAudTables() {
		jdbcTemplate.update("DELETE FROM address_aud");
		jdbcTemplate.update("DELETE FROM airport_aud");
		jdbcTemplate.update("DELETE FROM flight_aud");
	}
	
	@Transactional
	public void deleteAuditData() {
		jdbcTemplate.update("DELETE FROM address_aud");
		jdbcTemplate.update("DELETE FROM airport_aud");
		jdbcTemplate.update("DELETE FROM flight_aud");
	}
	
	
	@Transactional
	public void addInitData() {
		
		Address address1 = addressRepository.save(Address.builder().city("Budapest").build());
		Address address2 = addressRepository.save(Address.builder().city("Los Angeles").build());
		Address address3 = addressRepository.save(Address.builder().city("New York").build());
		Address address4 = addressRepository.save(Address.builder().city("London").build());
		
		Airport airport1 = airportRepository.save(new Airport("airport1", "BUD"));
		airport1.setAddress(address1);
		Airport airport2 = airportRepository.save(new Airport("airport2", "LAX"));
		airport2.setAddress(address2);
		Airport airport3 = airportRepository.save(new Airport("airport3", "JFK"));
		airport3.setAddress(address3);
		Airport airport4 = airportRepository.save(new Airport("airport4", "LGW"));
		airport4.setAddress(address4);
		
		flightService.save(new Flight(0, "ABC123", LocalDateTime.of(2022, 6, 10, 10, 10), airport1, airport2, null));
		flightService.save(new Flight(0, "ABC456", LocalDateTime.of(2022, 6, 10, 12, 10), airport2, airport3, null));
		flightService.save(new Flight(0, "DEF234", LocalDateTime.of(2022, 6, 12, 14, 10), airport2, airport4, null));
		flightService.save(new Flight(0, "GHI345", LocalDateTime.of(2022, 6, 13, 16, 10), airport4, airport1, null));
	
		createUsersIfNeeded();
	}
	
	@Transactional
	public void createUsersIfNeeded() {
		if(!userRepository.existsById("admin")) {
			userRepository.save(new AirportUser("admin", passwordEncoder.encode("pass"), Set.of("admin", "user")));
		}
		
		if(!userRepository.existsById("user")) {
			userRepository.save(new AirportUser("user", passwordEncoder.encode("pass"), Set.of("user")));
		}
	}
}
