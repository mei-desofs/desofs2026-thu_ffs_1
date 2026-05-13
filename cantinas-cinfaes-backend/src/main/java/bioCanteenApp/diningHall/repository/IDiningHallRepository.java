package bioCanteenApp.diningHall.repository;

import bioCanteenApp.diningHall.domain.DiningHall;

import java.util.List;
import java.util.Optional;

public interface IDiningHallRepository {
    DiningHall save(DiningHall diningHall);
    List<DiningHall> findAll();
    Optional<DiningHall> findByName(String name);

}
