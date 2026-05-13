package bioCanteenApp.waste.repository;

import bioCanteenApp.waste.domain.Waste;
import bioCanteenApp.waste.dto.WasteDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Repository
@Transactional
public class WasteRepo implements IWasteRepo {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Waste findByDate(LocalDate date) {
        TypedQuery<Waste> query = entityManager.createQuery(
        "SELECT w FROM Waste w WHERE w.date = :date", Waste.class);

        query.setParameter("date", date);
        List<Waste> result = query.getResultList();

        double totalMealsReserved = 0.0;
        double mealsNotServed = 0.0;
        double servedWaste = 0.0;
        double servedTotal = 0.0;

        for (Waste w : result) {
            totalMealsReserved += w.getTotalMealsReserved();
            mealsNotServed += w.getMealsNotServed();
            servedWaste += w.getServedWaste();
            servedTotal += w.getServedTotal();
        }

        return new Waste(totalMealsReserved, mealsNotServed, servedWaste, servedTotal);
    }

    @Override
    public Waste findByDateBetween(LocalDate start, LocalDate end) {
        TypedQuery<Waste> query = entityManager.createQuery(
        "SELECT w FROM Waste w WHERE w.date BETWEEN :start AND :end ORDER BY w.date ASC", Waste.class);

        query.setParameter("start", start);
        query.setParameter("end", end);

        List<Waste> result = query.getResultList();

        double totalMealsReserved = 0.0;
        double mealsNotServed = 0.0;
        double servedWaste = 0.0;
        double servedTotal = 0.0;

        for (Waste w : result) {
            totalMealsReserved += w.getTotalMealsReserved();
            mealsNotServed += w.getMealsNotServed();
            servedWaste += w.getServedWaste();
            servedTotal += w.getServedTotal();
        }

        return new Waste(totalMealsReserved, mealsNotServed, servedWaste, servedTotal);
    }

    @Override
    public Waste findAggregateAll() {
        TypedQuery<Waste> query = entityManager.createQuery(
                "SELECT w FROM Waste w ORDER BY w.date ASC", Waste.class
        );

        List<Waste> result = query.getResultList();

        double totalMealsReserved = 0.0;
        double mealsNotServed = 0.0;
        double servedWaste = 0.0;
        double servedTotal = 0.0;

        for (Waste w : result) {
            totalMealsReserved += w.getTotalMealsReserved();
            mealsNotServed += w.getMealsNotServed();
            servedWaste += w.getServedWaste();
            servedTotal += w.getServedTotal();
        }

        return new Waste(totalMealsReserved, mealsNotServed, servedWaste, servedTotal);
    }

    @Override
    public WasteDTO aggregateWaste(
            Long canteenId,
            Long diningHallId,
            Long supplierId,
            LocalDate start,
            LocalDate end
    ) {
        StringBuilder jpql = new StringBuilder("SELECT w FROM Waste w WHERE w.date BETWEEN :start AND :end");

        if (canteenId != null) {
            jpql.append(" AND w.canteen.id = :canteenId");
        }
        if (diningHallId != null) {
            jpql.append(" AND w.diningHall.id = :diningHallId");
        }
        if (supplierId != null) {
            jpql.append(" AND w.supplier.id = :supplierId");
        }

        TypedQuery<Waste> query = entityManager.createQuery(jpql.toString(), Waste.class);

        query.setParameter("start", start);
        query.setParameter("end", end);

        if (canteenId != null) query.setParameter("canteenId", canteenId);
        if (diningHallId != null) query.setParameter("diningHallId", diningHallId);
        if (supplierId != null) query.setParameter("supplierId", supplierId);

        List<Waste> result = query.getResultList();

        double totalMealsReserved = 0;
        double mealsNotServed = 0;
        double servedWaste = 0;
        double servedTotal = 0;

        for (Waste w : result) {
            totalMealsReserved += w.getTotalMealsReserved();
            mealsNotServed += w.getMealsNotServed();
            servedWaste += w.getServedWaste();
            servedTotal += w.getServedTotal();
        }

        return new WasteDTO(totalMealsReserved, mealsNotServed, servedWaste, servedTotal);
    }

    @Override
    public LocalDate[] getDateRange(String period) {
        LocalDate today = LocalDate.now();
        switch (period.toLowerCase()) {
            case "daily":
                return new LocalDate[]{today, today};
            case "weekly":
                return new LocalDate[]{today.minusDays(7), today};
            case "monthly":
                return new LocalDate[]{today.minusMonths(1), today};
            case "all":
            default:
                return new LocalDate[]{LocalDate.of(2000,1,1), today};
        }
    }

    @Override
    public Waste save(Waste waste) {
        if (waste.getId() == null) {
            entityManager.persist(waste);
        } else {
            waste = entityManager.merge(waste);
        }

        return waste;
    }

    @Override
    public Iterable<Waste> findAll() {
        TypedQuery<Waste> query = entityManager.createQuery("SELECT w FROM Waste w", Waste.class);

        return query.getResultList();
    }
}
