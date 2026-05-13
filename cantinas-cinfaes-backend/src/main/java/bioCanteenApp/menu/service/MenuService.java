package bioCanteenApp.menu.service;

import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishIngredient;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.dto.DishDto;
import bioCanteenApp.dish.dto.DishIngredientDto;
import bioCanteenApp.dish.repository.DishRepo;
import bioCanteenApp.dish.service.DishService;
import bioCanteenApp.ingredients.dto.IngredientDto;
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
import bioCanteenApp.users.dto.UserDTO;
import bioCanteenApp.users.mapper.UserMapper;
import bioCanteenApp.users.repository.UserRepo;
import bioCanteenApp.users.service.UserService;
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
    private final UserService userService;
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
        GetUserDTO dietician = userService.getUserById(String.valueOf(dto.getDieticianId()));

        if (dietician == null || !dietician.getRole().contains(Role.DIETITIAN.name())) {
            throw new IllegalArgumentException("Dietician not found");
        }

        Menu menu = menuMapper.toDomain(dto);
        menu = menuRepo.save(menu);

        return menuMapper.toDTO(menu);
    }

    @Override
    public MenuDto generateMenu(LocalDate startDate, LocalDate endDate) {

        System.out.println("=== Generating menu from " + startDate + " to " + endDate + " ===");

        Menu menu = new Menu(startDate, endDate, MenuStatus.GENERATED);

        //1. Ver todos os produtos sazonais disponiveis
        //2. Ver todos os pratos
        //3. Para cada dia da semana, escolher 3 pratos que usem produtos sazonais

        //Lista de produtos sazonais disponiveis
        List<ProductDTO> seasonalProducts = productService.getAvailableSeasonalProducts();
        List<String> seasonalProductsNames = seasonalProducts.stream()
                .map(ProductDTO::getName)
                .collect(Collectors.toList());
        System.out.println("=== Generating menu from " + startDate + " to " + endDate + " ===");


        // 2. Pratos que usam apenas produtos sazonais
        List<DishDto> dishesWithSeasonalIngredients = dishService.getDishesWithSeasonalIngredients(seasonalProductsNames);
        System.out.println("Seasonal product names: " + seasonalProductsNames);

        List<DishDto> dishesWithSeasonalAndStock = dishesWithSeasonalIngredients.stream()
                .filter(this::hasStockForDish)
                .collect(Collectors.toList());

        System.out.println("Dishes with seasonal ingredients before stock check: " + dishesWithSeasonalAndStock);

        if (dishesWithSeasonalAndStock.isEmpty()) {
            throw new IllegalArgumentException("No dishes available with seasonal ingredients AND sufficient stock");
        }

        // 4. Distribuir pratos por tipo
        Map<DishType, List<DishDto>> dishesByType = new HashMap<>();
        for (DishType type : DishType.values()) {
            dishesByType.put(type, new ArrayList<>());
        }

        for (DishDto d : dishesWithSeasonalAndStock) {
            DishType type = DishType.valueOf(d.getDishType());
            dishesByType.get(type).add(d);
        }
        System.out.println("Dishes with seasonal ingredients: " + dishesWithSeasonalIngredients);

        // 5. Criar MenuEntries para cada dia da semana
        List<MenuEntry> entries = new ArrayList<>();
        Random random = new Random();
        WeekDay[] weekDays = WeekDay.values();
        System.out.println("Week days: " + Arrays.toString(weekDays));

        for (int i = 0; i < weekDays.length; i++) {
            LocalDate date = startDate.plusDays(i);

            MenuEntry entry = new MenuEntry();
            entry.setWeekDay(weekDays[i].name());
            entry.setDate(date);
            entry.setMenu(menu);

            List<MenuEntryDish> entryDishes = new ArrayList<>();

            for (DishType type : DishType.values()) {
                List<DishDto> list = dishesByType.get(type);
                System.out.println("Dishes for type " + type + ": " + list);
                if (list != null && !list.isEmpty()) {
                    DishDto selectedDto = list.get(random.nextInt(list.size()));
                    System.out.println("Selected dish for " + type + ": " + selectedDto.getDishName());
                    Dish dishEntity = dishRepo.findById(selectedDto.getId())
                            .orElseThrow(() -> new RuntimeException("Dish not found: " + selectedDto.getId()));
                    entryDishes.add(new MenuEntryDish(entry, dishEntity));
                }
            }

            entry.setMenuEntryDishes(entryDishes);
            System.out.println("MenuEntry created for " + weekDays[i] + " with dishes: " + entryDishes);
            entries.add(entry);
        }

        menu.setEntries(entries);
        menu = menuRepo.save(menu);
        System.out.println("Menu saved with entries: " + menu.getEntries());

        MenuDto dto = menuMapper.toDTO(menu);
        System.out.println("MenuDto mapped: " + dto);

        return dto;
    }

    @Override
    public void publishMenu(LocalDate start, LocalDate end, Long dietitianId) {
        User dietician = userRepository.findById(dietitianId);

        if (dietician == null) {
            throw new IllegalArgumentException("Dietician not found");
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

        System.out.println("Total" + totalMenus);

        long approvedMenus = menuRepo.countByStatus(MenuStatus.PUBLISHED);
        String approvalRate = totalMenus > 0 ? String.format("%.0f%%", (approvedMenus * 100.0 / totalMenus)) : "0%";

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