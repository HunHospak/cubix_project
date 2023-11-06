package hu.webuni.airport.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.webuni.airport.model.Address;
import hu.webuni.airport.model.Airport;
import hu.webuni.airport.model.HistoryData;
import hu.webuni.airport.model.Image;
import hu.webuni.airport.repository.AirportRepository;
import hu.webuni.airport.repository.ImageRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AirportService {

	private final AirportRepository airportRepository;
	private final ImageRepository imageRepository;
	
	@PersistenceContext
	private EntityManager em;

	@Transactional
	public Airport save(Airport airport) {
		checkUniqueIata(airport.getIata(), null);
		return airportRepository.save(airport);
	}
	
	@Transactional
	public Airport update(Airport airport) {
		checkUniqueIata(airport.getIata(), airport.getId());
		if(airportRepository.existsById(airport.getId())) {
			return airportRepository.save(airport);
		}
		else
			throw new NoSuchElementException();
	}
	
	private void checkUniqueIata(String iata, Long id) {
		
		boolean forUpdate = id != null;
		Long count = forUpdate ?
				airportRepository.countByIataAndIdNot(iata, id)
				:airportRepository.countByIata(iata);
		
		if(count > 0)
			throw new NonUniqueIataException(iata);
	}
	
	public List<Airport> findAll(){
		return airportRepository.findAll();
	}
	
	public Optional<Airport> findById(long id){
		return airportRepository.findById(id);
	}
	
	@Transactional
	public void delete(long id) {
		airportRepository.deleteById(id);
	}

	@Transactional
	@Cacheable("pagedAirportsWithRelationships")
	public List<Airport> findAllWithRelationships(Pageable pageable) {
//		List<Airport> airports = airportRepository.findAllWithAddressAndDepartures(pageable); --> in memory lapozás, minden sor bejön a DB-ből
//		airports = airportRepository.findAllWithArrivals(pageable);
		
		List<Airport> airports = airportRepository.findAllWithAddress(pageable);
		List<Long> airportIds = airports.stream().map(Airport::getId).toList();
		
		airports = airportRepository.findByIdWithArrivals(airportIds);
		
		//a videóban ez a sor még a pageable.getSort() nélkül szerepel, hibásan
		//ha nem adnánk át a pageable-ben meghatározott rendezést, a kért helyett nem-definiált sorrendben
		//adnánk vissza a lapra eső entitásokat
		airports = airportRepository.findByIdWithDepartures(airportIds, pageable.getSort());
		return airports;
	}
	
	@Transactional
	@SuppressWarnings({"rawtypes", "unchecked"})
	public List<HistoryData<Airport>> getAirportHistory(long id){
		
		List resultList = AuditReaderFactory.get(em)
		.createQuery()
		.forRevisionsOfEntity(Airport.class, false, true)
		.add(AuditEntity.property("id").eq(id))
		.getResultList()
		.stream()
		.map(o ->{
			Object[] objArray = (Object[])o;
			DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity)objArray[1];
			Airport airport = (Airport)objArray[0];
			Address address = airport.getAddress();
			if(address != null) 
				address.getCity();
			airport.getArrivals().size();
			airport.getDepartures().size();
			
			return new HistoryData<Airport>(
				airport,
				(RevisionType)objArray[2],
				revisionEntity.getId(),
				revisionEntity.getRevisionDate()
			);
		}).toList();
		
		return resultList;
	}
	
	@Transactional
	public Image saveImageForAirport(long airportId, String fileName, byte[] bytes) {
		Airport airport = airportRepository.findById(airportId).get();
		Image image = Image.builder()
			.data(bytes)
			.fileName(fileName)
			.build();
		image = imageRepository.save(image);
		airport.getImages().add(image);
		return image;
	}
	
}
