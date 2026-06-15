package bioCanteenApp.menu.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.DishIngredientDto;
import bioCanteenApp.dish.repository.DishRepo;
import bioCanteenApp.dish.service.DishService;
import bioCanteenApp.menu.domain.*;
import bioCanteenApp.menu.dto.MenuDto;
import bioCanteenApp.menu.mapper.MenuEntryMapper;
import bioCanteenApp.menu.mapper.MenuMapper;
import bioCanteenApp.menu.repository.MenuRepo;
import bioCanteenApp.products.dto.ProductDTO;
import bioCanteenApp.products.repository.ProductBatchRepo;
import bioCanteenApp.products.service.ProductService;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.dto.GetUserDTO;
import bioCanteenApp.users.mapper.UserMapper;
import bioCanteenApp.users.repository.UserRepo;
import bioCanteenApp.users.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService implements IMenuService {

    private final MenuRepo menuRepo;
    private final MenuMapper menuMapper;
    private final IUserService userService;
    private final ProductService productService;
    private final DishService dishService;
    private final DishRepo dishRepo;
    private final MenuEntryMapper menuEntryMapper;
    private final UserMapper userMapper;
    private final UserRepo userRepository;
    private final ProductBatchRepo productBatchRepo;

    @Override
    public List<MenuDto> getAllMenus() {
        List<Menu> menus = (List<Menu>) menuRepo.findAll();
        return menus.stream()
                .map(menuMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuDto> getMenusByWeek(LocalDate startDate, LocalDate endDate) {
        List<Menu> menus = menuRepo.findByMenuDates(startDate, endDate, startDate, endDate);
        return menus.stream()
                .map(menuMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MenuDto createMenu(MenuDto dto) {
        GetUserDTO dietician = userService.findUserById(dto.getDieticianId().getId());

        if (dietician == null || !dietician.getRole().contains(Role.DIETITIAN.name())) {
            throw new IllegalArgumentException("Dietician not found");
        }

        Menu menu = menuMapper.toDomain(dto);
        menu = menuRepo.save(menu);

        return menuMapper.toDTO(menu);
    }

    @Override
    public MenuDto generateMenu(LocalDate startDate, LocalDate endDate) {

        Menu menu = new Menu(startDate, endDate, MenuStatus.GENERATED);

        //1. Ver todos os produtos sazonais disponiveis
        //2. Ver todos os pratos
        //3. Para cada dia da semana, escolher 3 pratos que usem produtos sazonais

        //Lista de produtos sazonais disponiveis
        List<ProductDTO> seasonalProducts = productService.getAvailableSeasonalProducts();

        List<String> seasonalProductsNames = seasonalProducts.stream()
                .map(ProductDTO::getName)
                .collect(Collectors.toList());

        List<DishDto> dishesWithSeasonalIngredients =
                dishService.getDishesWithSeasonalIngredients(seasonalProductsNames);

        List<DishDto> dishesWithSeasonalAndStock = dishesWithSeasonalIngredients.stream()
                .filter(this::hasStockForDish)
                .collect(Collectors.toList());

        if (dishesWithSeasonalAndStock.isEmpty()) {
            throw new IllegalArgumentException("No dishes available with seasonal ingredients and sufficient stock");
        }

        // 4. Distribuir pratos por tipo
        Map<DishType, List<DishDto>> dishesByType = new HashMap<>();

        for (DishType type : DishType.values()) {
            dishesByType.put(type, new ArrayList<>());
        }

        for (DishDto dish : dishesWithSeasonalAndStock) {
            DishType type = DishType.valueOf(dish.getDishType());
            dishesByType.get(type).add(dish);
        }

        for (DishType type : DishType.values()) {
            if (dishesByType.get(type).isEmpty()) {
                throw new IllegalStateException(
                        "Cannot generate menu. Missing available dishes for type: " + type
                );
            }
        }

        List<MenuEntry> entries = new ArrayList<>();
        Random random = new Random();
        WeekDay[] weekDays = WeekDay.values();

        for (int i = 0; i < weekDays.length; i++) {
            LocalDate date = startDate.plusDays(i);

            MenuEntry entry = new MenuEntry();
            entry.setWeekDay(weekDays[i].name());
            entry.setDate(date);
            entry.setMenu(menu);

            List<MenuEntryDish> entryDishes = new ArrayList<>();

            for (DishType type : DishType.values()) {
                List<DishDto> list = dishesByType.get(type);

                DishDto selectedDto = list.get(random.nextInt(list.size()));

                Dish dishEntity = dishRepo.findById(selectedDto.getId())
                        .orElseThrow(() -> new RuntimeException("Dish not found: " + selectedDto.getId()));

                entryDishes.add(new MenuEntryDish(entry, dishEntity));
            }

            entry.setMenuEntryDishes(entryDishes);
            entries.add(entry);
        }

        menu.setEntries(entries);
        menu = menuRepo.save(menu);

        return menuMapper.toDTO(menu);
    }

    @Override
    public void publishMenu(LocalDate start, LocalDate end, Long dietitianId) {
        User dietician = userRepository.findById(dietitianId);

        if (dietician == null) {
            throw new IllegalArgumentException("Dietician not found");
        }

        if (LocalDate.now().isAfter(start.minusWeeks(1))) {
            throw new IllegalArgumentException(
                    "Menu must be published at least one week before the target week starts."
            );
        }

        menuRepo.findByMenuDates(start, end, start, end).forEach(menu -> {
            menu.setStatus(MenuStatus.PUBLISHED);
            menu.setDietician(dietician);
            menuRepo.save(menu);
        });
    }

    @Override
    public void closeMenu(LocalDate start, LocalDate end) {
        menuRepo.findByMenuDates(start, end, start, end).forEach(menu -> {
            menu.setStatus(MenuStatus.CLOSED);
            menuRepo.save(menu);
        });
    }

    @Override
    public Map<String, Object> getPlanningStats() {
        long totalMenus = menuRepo.count();
        long totalDishes = dishRepo.count();

        long approvedMenus = menuRepo.countByStatus(MenuStatus.PUBLISHED);
        String approvalRate = totalMenus > 0
                ? String.format("%.0f%%", (approvedMenus * 100.0 / totalMenus))
                : "0%";

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMenus", totalMenus);
        stats.put("totalDishes", totalDishes);
        stats.put("approvalRate", approvalRate);

        return stats;
    }

    private boolean hasStockForDish(DishDto dishDto) {
        if (dishDto == null || dishDto.getIngredients() == null || dishDto.getIngredients().isEmpty()) {
            return false;
        }

        for (DishIngredientDto ingredient : dishDto.getIngredients()) {
            Long productId = ingredient.getProductId();
            double requiredQuantity = ingredient.getQuantity();

            double availableQuantity = productBatchRepo.sumValidStockByProduct(productId);

            if (availableQuantity < requiredQuantity) {
                return false;
            }
        }

        return true;
    }
}