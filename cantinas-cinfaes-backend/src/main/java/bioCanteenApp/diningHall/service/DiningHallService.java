package bioCanteenApp.diningHall.service;

import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.diningHall.dto.DiningHallDTO;
import bioCanteenApp.diningHall.mappers.IDiningHallMapper;
import bioCanteenApp.diningHall.repository.IDiningHallRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiningHallService implements IDiningHallService{

    private final IDiningHallRepository repo;

    private final IDiningHallMapper mapper;

    public DiningHallService(IDiningHallRepository repo, IDiningHallMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<DiningHallDTO> getAllDiningHall() {
        return repo.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }
}
