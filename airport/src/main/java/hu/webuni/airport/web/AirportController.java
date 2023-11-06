package hu.webuni.airport.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import hu.webuni.airport.api.AirportControllerApi;
import hu.webuni.airport.api.model.AirportDto;
import hu.webuni.airport.api.model.HistoryDataAirportDto;
import hu.webuni.airport.mapper.AirportMapper;
import hu.webuni.airport.mapper.HistoryDataMapper;
import hu.webuni.airport.model.Airport;
import hu.webuni.airport.model.HistoryData;
import hu.webuni.airport.model.Image;
import hu.webuni.airport.repository.AirportRepository;
import hu.webuni.airport.service.AirportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AirportController implements AirportControllerApi {
	
	private final NativeWebRequest nativeWebRequest;
	private final AirportService airportService;
	private final AirportRepository airportRepository;
	
	private final AirportMapper airportMapper;
	private final HistoryDataMapper historyDataMapper;
	private final PageableHandlerMethodArgumentResolver pageableResolver;
	
	@Override
	public Optional<NativeWebRequest> getRequest() {
		return Optional.of(nativeWebRequest);
	}

	@Override
	public ResponseEntity<AirportDto> createAirport(@Valid AirportDto airportDto) {
		Airport airport = airportService.save(airportMapper.dtoToAirport(airportDto));
		return ResponseEntity.ok(airportMapper.airportToDto(airport));
	}

	@Override
	public ResponseEntity<Void> deleteAirport(Long id) {
		airportService.delete(id);
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<AirportDto> getById(Long id) {
		Airport airport = airportService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		
		return ResponseEntity.ok(airportMapper.airportSummaryToDto(airport));
	}

	@Override
	public ResponseEntity<List<HistoryDataAirportDto>> getHistoryById(Long id) {
		
		List<HistoryData<Airport>> airports = airportService.getAirportHistory(id);
		
		List<HistoryDataAirportDto> airportDtosWithHistory = new ArrayList<>();
		
		airports.forEach(hd ->{
			airportDtosWithHistory.add(historyDataMapper.airportHistoryDataToDto(hd));
		});
		
		return ResponseEntity.ok(airportDtosWithHistory);
	}

	@Override
	public ResponseEntity<AirportDto> modifyAirport(Long id, @Valid AirportDto airportDto) {
		Airport airport = airportMapper.dtoToAirport(airportDto);
		airport.setId(id);
		try {
			AirportDto savedAirportDto = airportMapper.airportToDto(airportService.update(airport));

			return ResponseEntity.ok(savedAirportDto);
		} catch (NoSuchElementException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
	}

	public void configPageable(@SortDefault("id") Pageable pageable) {}
	
	@Override
	public ResponseEntity<List<AirportDto>> getAll(@Valid Boolean full, @Valid Integer page, @Valid Integer size,
			@Valid List<String> sort) {
		
		boolean isFull = full == null ? false : full;
		
		Pageable pageable = createPageable("configPageable");
		
		List<Airport> airports = isFull 
				? airportService.findAllWithRelationships(pageable)
//				? airportRepository.findAllWithAddressAndDepartures() --> N*M sor jön vissza, ha N arrival és M departure van
				: airportRepository.findAll(pageable).getContent();
		
		List<AirportDto> resultList = isFull 
				? airportMapper.airportsToDtos(airports)
				: airportMapper.airportSummariesToDtos(airports);
		return ResponseEntity.ok(resultList);
	}

	private Pageable createPageable(String pageableConfigurerMethodName) {
		Method method;
		try {
			method = this.getClass().getMethod(pageableConfigurerMethodName, Pageable.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		MethodParameter methodParameter = new MethodParameter(method, 0);
		ModelAndViewContainer mavContainer = null;
		WebDataBinderFactory binderFactory = null;
		Pageable pageable = pageableResolver.resolveArgument(methodParameter, mavContainer, nativeWebRequest, binderFactory);
		return pageable;
	}

	@Override
	public ResponseEntity<String> uploadImageForAirport(Long id, @Valid String fileName,
			@Valid MultipartFile content) {
		Image image;
		try {
			image = airportService.saveImageForAirport(id, fileName, content.getBytes());
			return ResponseEntity.ok("/api/images/" + image.getId());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	
	
	
}
