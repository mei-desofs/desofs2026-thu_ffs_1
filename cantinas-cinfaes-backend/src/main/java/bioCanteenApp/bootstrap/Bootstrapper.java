package bioCanteenApp.bootstrap;

import bioCanteenApp.address.Address;
import bioCanteenApp.address.Municipality;
import bioCanteenApp.address.Village;
import bioCanteenApp.canteens.domain.Canteen;
import bioCanteenApp.canteens.repository.ICanteenRepo;
import bioCanteenApp.diningHall.domain.DiningHall;
import bioCanteenApp.diningHall.repository.IDiningHallRepository;
import bioCanteenApp.dish.domain.Dish;
import bioCanteenApp.dish.domain.DishType;
import bioCanteenApp.dish.repository.IDishRepo;
import bioCanteenApp.ingredients.domain.Ingredient;
import bioCanteenApp.ingredients.repository.IIngredientRepo;
import bioCanteenApp.menu.domain.*;
import bioCanteenApp.menu.repository.IMenuRepo;
import bioCanteenApp.products.domain.Allergen;
import bioCanteenApp.products.domain.Product;
import bioCanteenApp.products.domain.ProductBatch;
import bioCanteenApp.products.domain.Season;
import bioCanteenApp.products.repository.IProductBatchRepo;
import bioCanteenApp.products.repository.IProductRepo;
import bioCanteenApp.recipes.domain.Recipe;
import bioCanteenApp.recipes.repository.IRecipeRepo;
import bioCanteenApp.reservation.domain.Reservation;
import bioCanteenApp.reservation.domain.ReservationStatus;
import bioCanteenApp.reservation.repository.IReservationRepo;
import bioCanteenApp.suppliers.domain.*;
import bioCanteenApp.suppliers.repository.ISupplierRepo;
import bioCanteenApp.users.domain.Role;
import bioCanteenApp.users.domain.User;
import bioCanteenApp.users.repository.IUserRepo;
import bioCanteenApp.waste.domain.Waste;
import bioCanteenApp.waste.repository.IWasteRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class Bootstrapper implements CommandLineRunner {

    private final IMenuRepo menuRepository;
    private final IProductRepo productRepository;
    private final ISupplierRepo supplierRepository;
    private final IUserRepo userRepository;
    private final IDishRepo dishRepository;
    private final IRecipeRepo recipeRepository;
    private final IIngredientRepo ingredientRepository;
    private final IReservationRepo reservationRepository;
    private final IProductBatchRepo productBatchRepository;
    private final ICanteenRepo canteenRepository;
    private final IDiningHallRepository diningHallRepository;
    private final IWasteRepo wasteRepository;

    @Override
    @Transactional
    public void run(final String... args) {
        createCanteensAndHalls();
        createUsers();
        createProducts();
        createRecipes();
        createDishes(); // Cria pratos + ingredientes
        createHistoryAndFutureMenus();
        createReservations();
        createSuppliers();
        createSupplierApplications();
        createProductBatches();
        createWaste();
    }

    private void createCanteensAndHalls() {
        if (canteenRepository.findAll().iterator().hasNext()) return;

        Address address1 = new Address("Rua da Cantina, 456", Municipality.AMARANTE, Village.GOUVEIA, "Portugal", "4690-100");
        Address address2 = new Address("Avenida dos Estudantes, 123", Municipality.BAIAO, Village.FRENDE, "Portugal", "4560-200");
        Canteen c1 = canteenRepository.save(new Canteen("Cantina Cinfaes", address1, 500, true));
        Canteen c2 = canteenRepository.save(new Canteen("Cantina ISEP", address2, 300, true));

        diningHallRepository.save(new DiningHall("Refeitório Principal", c1));
        diningHallRepository.save(new DiningHall("Refeitório Alunos", c1));
        diningHallRepository.save(new DiningHall("Refeitório Engenharia", c2));
    }

    private void createUsers() {
        if (userRepository.findAll().iterator().hasNext()) return;

        Canteen c1 = canteenRepository.findByName("Cantina Cinfaes").orElseThrow();
        DiningHall h1 = diningHallRepository.findByName("Refeitório Principal").orElseThrow();

        Canteen c2 = canteenRepository.findByName("Cantina ISEP").orElseThrow();
        DiningHall h2 = diningHallRepository.findByName("Refeitório Engenharia").orElseThrow();

        userRepository.save(new User("admin@biocanteens.com", "Admin", "Admin#123", Role.ADMIN));
        userRepository.save(new User("dietitian@biocanteens.com", "Dietitian", "Dietitian#123", Role.DIETITIAN));
        userRepository.save(new User("user@biocanteens.com", "User", "User#123", Role.USER));
        userRepository.save(new User("canteenmanager@cantinascinfaes.com", "Manager", "CanteenManager#123", Role.CANTEEN_MANAGER, c1));
        userRepository.save(new User("canteenmanager2@cantinascinfaes.com", "Manager 2", "CanteenManager#123", Role.CANTEEN_MANAGER, c2));
        userRepository.save(new User("hallmanager@cantinascinfaes.com", "Hall Manager", "HallManager#123", Role.DINING_HALL_MANAGER, h1));
        userRepository.save(new User("hallmanager2@cantinascinfaes.com", "Hall Manager 2", "HallManager#123", Role.DINING_HALL_MANAGER, h2));
        userRepository.save(new User("networkmanager@cantinascinfaes.com", "Network Manager", "NetworkManager#123", Role.NETWORK_MANAGER));

        for (int i = 1; i <= 15; i++) {
            userRepository.save(new User("tester" + i + "@test.com", "Tester " + i, "Pass#123", Role.USER));
        }
    }

    private void createProducts() {
        if (productRepository.findAll().iterator().hasNext()) return;

        String[] proteins = {"Chicken", "Beef", "Pork", "Salmon", "Codfish", "Tofu", "Turkey", "Hake"};
        for (String p : proteins) {
            List<Allergen> proteinAllergens = new ArrayList<>();
            if (p.equals("Salmon") || p.equals("Codfish") || p.equals("Hake")) {
                proteinAllergens.add(Allergen.FISH);
            } else if (p.equals("Tofu")) {
                proteinAllergens.add(Allergen.SOYBEANS); // Tofu é soja
            }
            productRepository.save(new Product(p, "kg", 50, List.of(Season.values()), proteinAllergens));
        }

        productRepository.save(new Product("Wheat", "kg", 180, List.of(Season.values()), List.of(Allergen.GLUTEN)));
        productRepository.save(new Product("Milk", "l", 7, List.of(Season.values()), List.of(Allergen.MILK)));
        productRepository.save(new Product("Pasta", "kg", 10, List.of(Season.values()), List.of(Allergen.GLUTEN)));

        productRepository.save(new Product("Golden Apple", "kg", 15, List.of(Season.AUTUMN, Season.WINTER), List.of()));
        productRepository.save(new Product("Orange", "kg", 12, List.of(Season.WINTER, Season.SPRING, Season.SUMMER), List.of()));
        productRepository.save(new Product("Strawberry", "kg", 25, List.of(Season.SPRING, Season.SUMMER), List.of()));
        productRepository.save(new Product("Chestnut", "kg", 10, List.of(Season.AUTUMN), List.of()));
        productRepository.save(new Product("Carrot", "kg", 10, List.of(Season.SPRING, Season.SUMMER, Season.AUTUMN), List.of()));
        productRepository.save(new Product("Green Beans", "kg", 7, List.of(Season.SUMMER, Season.AUTUMN), List.of()));
        productRepository.save(new Product("Raspberry", "kg", 3, List.of(Season.SUMMER, Season.AUTUMN), List.of()));
        productRepository.save(new Product("Black Grapes", "kg", 5, List.of(Season.AUTUMN), List.of()));
        productRepository.save(new Product("Persimmon", "kg", 10, List.of(Season.AUTUMN, Season.WINTER), List.of()));
        productRepository.save(new Product("Sprout", "kg", 45, List.of(Season.AUTUMN, Season.WINTER), List.of()));
        productRepository.save(new Product("Cabbage", "kg", 45, List.of(Season.AUTUMN, Season.WINTER), List.of()));

        String[] basicSides = {"Rice", "Potato", "Tomato", "Lettuce", "Broccoli", "Eggplant", "Onion", "Garlic"};
        for (String s : basicSides) {
            if (productRepository.findByName(s) == null) {
                productRepository.save(new Product(s, "kg", 10, List.of(Season.values()), List.of()));
            }
        }
    }

    private void createRecipes() {
        if (recipeRepository.findAll().iterator().hasNext()) return;
        String[] methods = {"Grelhado", "Estufado", "Assado", "Cozido", "Salteado", "Sopa", "Pasta"};
        for (String m : methods) {
            recipeRepository.save(new Recipe(m, "Preparar técnica de " + m.toLowerCase()));
        }
    }

    private void createDishes() {
        if (dishRepository.findAll().iterator().hasNext()) return;

        Object[][] dishData = {
                {"Roast Chicken", DishType.MEAT, "Chicken", 0.300, "Potato", 0.200, "Assado"},
                {"Beef Stew", DishType.MEAT, "Beef", 0.250, "Carrot", 0.150, "Estufado"},
                {"Pork Alentejana", DishType.MEAT, "Pork", 0.250, "Potato", 0.200, "Estufado"},
                {"Turkey Steak", DishType.MEAT, "Turkey", 0.200, "Rice", 0.150, "Grelhado"},
                {"Meatballs Pasta", DishType.MEAT, "Beef", 0.200, "Pasta", 0.150, "Pasta"},
                {"Grilled Salmon", DishType.FISH, "Salmon", 0.250, "Broccoli", 0.150, "Grelhado"},
                {"Baked Codfish", DishType.FISH, "Codfish", 0.300, "Potato", 0.200, "Assado"},
                {"Hake Fillets", DishType.FISH, "Hake", 0.200, "Rice", 0.150, "Cozido"},
                {"Fish Pasta", DishType.FISH, "Hake", 0.150, "Pasta", 0.150, "Pasta"},
                {"Salmon Rice", DishType.FISH, "Salmon", 0.150, "Rice", 0.200, "Estufado"},
                {"Tofu Stir-fry", DishType.VEGETARIAN, "Tofu", 0.200, "Broccoli", 0.200, "Salteado"},
                {"Veggie Pasta", DishType.VEGETARIAN, "Tomato", 0.300, "Pasta", 0.150, "Pasta"},
                {"Eggplant Stew", DishType.VEGETARIAN, "Eggplant", 0.250, "Tomato", 0.200, "Estufado"},
                {"Rice and Beans", DishType.VEGETARIAN, "Rice", 0.200, "Tomato", 0.150, "Estufado"},
                {"Roasted Veggies", DishType.VEGETARIAN, "Potato", 0.200, "Carrot", 0.200, "Assado"},
                {"Vegetable Soup", DishType.DIET, "Carrot", 0.150, "Onion", 0.100, "Sopa"},
                {"Boiled Chicken", DishType.DIET, "Chicken", 0.150, "Rice", 0.150, "Cozido"},
                {"Grilled Hake", DishType.DIET, "Hake", 0.200, "Potato", 0.150, "Grelhado"},
                {"Carrot Puree", DishType.DIET, "Carrot", 0.200, "Potato", 0.150, "Cozido"}
        };

        for (Object[] data : dishData) {
            String dishName = (String) data[0];
            DishType type = (DishType) data[1];
            String ing1Name = (String) data[2];
            Double qty1 = (Double) data[3];
            String ing2Name = (String) data[4];
            Double qty2 = (Double) data[5];
            String method = (String) data[6];

            Dish dish = new Dish(dishName, type);

            Recipe uniqueRecipe = new Recipe(
                    method + " de " + dishName,
                    "Preparar usando as medidas específicas."
            );
            recipeRepository.save(uniqueRecipe);
            dish.setRecipe(uniqueRecipe);

            Ingredient i1 = getOrCreateBaseIngredient(ing1Name);
            Ingredient i2 = getOrCreateBaseIngredient(ing2Name);

            dish.addIngredient(i1, qty1);
            dish.addIngredient(i2, qty2);

            dishRepository.save(dish);
        }
        log.info("20 pratos criados, cada um com sua própria receita individual.");
    }

    private Ingredient getOrCreateBaseIngredient(String productName) {
        List<Ingredient> existing = ingredientRepository.findByName(productName);
        if (!existing.isEmpty()) return existing.get(0);

        Product p = productRepository.findByName(productName);
        if (p == null) throw new IllegalStateException("Produto não encontrado: " + productName);

        double zFactor = 1.0 + (new Random().nextDouble() * 0.5);

        return ingredientRepository.save(new Ingredient(productName, zFactor, p));
    }

    private void createHistoryAndFutureMenus() {
        if (menuRepository.findAll().iterator().hasNext()) return;

        User dietitian = userRepository.findByEmail("dietitian@biocanteens.com").iterator().next();

        List<Dish> meatDishes = dishRepository.findAllByType(DishType.MEAT);
        List<Dish> fishDishes = dishRepository.findAllByType(DishType.FISH);
        List<Dish> vegDishes = dishRepository.findAllByType(DishType.VEGETARIAN);
        List<Dish> dietDishes = dishRepository.findAllByType(DishType.DIET);

        for (int weekOffset = -2; weekOffset <= 0; weekOffset++) {

            LocalDate monday = LocalDate.now().plusWeeks(weekOffset).with(java.time.DayOfWeek.MONDAY);
            Menu menu = new Menu(monday, monday.plusDays(4), MenuStatus.PUBLISHED);
            menu.setDietician(dietitian);

            for (int day = 0; day < 5; day++) {
                MenuEntry entry = new MenuEntry();
                entry.setDate(monday.plusDays(day));
                entry.setWeekDay(WeekDay.values()[day].name());
                entry.setMenu(menu);

                entry.getMenuEntryDishes().add(new MenuEntryDish(entry, meatDishes.get((day + Math.abs(weekOffset)) % meatDishes.size())));
                entry.getMenuEntryDishes().add(new MenuEntryDish(entry, fishDishes.get((day + Math.abs(weekOffset)) % fishDishes.size())));
                entry.getMenuEntryDishes().add(new MenuEntryDish(entry, vegDishes.get((day + Math.abs(weekOffset)) % vegDishes.size())));
                entry.getMenuEntryDishes().add(new MenuEntryDish(entry, dietDishes.get((day + Math.abs(weekOffset)) % dietDishes.size())));

                menu.getEntries().add(entry);
            }
            menuRepository.save(menu);
        }
    }

    private void createReservations() {
        if (reservationRepository.findAll().iterator().hasNext()) return;

        Random random = new Random();
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);

        Iterable<Menu> menus = menuRepository.findAll();

        for (Menu menu : menus) {
            for (MenuEntry entry : menu.getEntries()) {
                for (MenuEntryDish dish : entry.getMenuEntryDishes()) {

                    int numReservations = random.nextInt(users.size() - 5) + 5;

                    List<User> shuffledStudents = new ArrayList<>(users);
                    java.util.Collections.shuffle(shuffledStudents);

                    for (int i = 0; i < numReservations; i++) {
                        reservationRepository.save(new Reservation(
                                shuffledStudents.get(i),
                                dish,
                                entry.getDate().atTime(12, 0),
                                ReservationStatus.CONFIRMED
                        ));
                    }
                }
            }
        }
    }

    private void createSuppliers() {
        if (supplierRepository.findAll().iterator().hasNext()) return;

        String[][] supData = {
                {"Talho Cinfaes", "Chicken", "Beef", "Pork", "Turkey"},
                {"Peixaria Douro", "Salmon", "Codfish", "Hake"},
                {"Horta Local", "Tomato", "Carrot", "Potato", "Onion", "Broccoli", "Eggplant", "Lettuce", "Apple", "Orange"},
                {"Armazem Central", "Rice", "Pasta", "Tofu"},

                // BAIAO / GRILO
                {"Talho Grilo", "Beef", "Pork"},
                {"Horta Grilo", "Tomato", "Lettuce", "Onion"},
                {"Peixaria Grilo", "Salmon", "Hake"}
        };

        for (int i = 0; i < supData.length; i++) {

            User u = userRepository.save(
                    new User("sup" + i + "@test.pt", supData[i][0], "Pass#123", Role.USER)
            );

            List<SupplierCapacity> caps = new ArrayList<>();
            for (int j = 1; j < supData[i].length; j++) {
                caps.add(new SupplierCapacity(
                        supData[i][j],
                        LocalDate.now().minusYears(1),
                        LocalDate.now().plusYears(1),
                        500.0
                ));
            }

            byte[] dummyCertifiedOrganic = "Dummy PDF content".getBytes();

            Village village = (i >= 4) ? Village.GRILO : Village.FRENDE;

            Address address = new Address(
                    "Rua " + i,
                    Municipality.BAIAO,
                    village,
                    "P",
                    "4690"
            );

            SupplierApplication app = new SupplierApplication(
                    supData[i][0],
                    "sup" + i + "@test.pt",
                    "91" + i,
                    address,
                    dummyCertifiedOrganic,
                    1000L + i,
                    caps,
                    LocalDate.now()
            );
            app.setStatus(SupplierApplicationStatus.APPROVED);
            supplierRepository.save(app);

            Supplier s = new Supplier(
                    u,
                    "NIF" + i,
                    address,
                    "91" + i,
                    dummyCertifiedOrganic,
                    app
            );
            supplierRepository.save(s);
        }
    }


    private void createSupplierApplications() {
        if (supplierRepository.findAllApplications().iterator().hasNext()) return;

        List<SupplierCapacity> caps = List.of(new SupplierCapacity("Tomato", LocalDate.now(), LocalDate.now().plusMonths(1), 100.0));
        byte[] dummyCertifiedOrganic = "Dummy PDF content".getBytes();
        SupplierApplication app = new SupplierApplication("Novo Fornecedor", "novo@test.pt", "999",
                new Address("Rua X", Municipality.BAIAO, Village.FRENDE, "Portugal", "4610"), dummyCertifiedOrganic, 999L, caps, LocalDate.now());
        app.setStatus(SupplierApplicationStatus.APPROVED);
        supplierRepository.save(app);

        if (supplierRepository.findAllApplications().iterator().hasNext()) return;

        List<SupplierCapacity> caps2 = List.of(new SupplierCapacity("Tomato", LocalDate.now(), LocalDate.now().plusMonths(1), 100.0));
        byte[] dummyCertifiedOrganic2 = "Dummy PDF content".getBytes();
        SupplierApplication app2 = new SupplierApplication("Novo Fornecedor", "novo@test.pt", "999",
                new Address("Rua X", Municipality.BAIAO, Village.FRENDE, "Portugal", "4610"), dummyCertifiedOrganic2, 999L, caps2, LocalDate.now());
        app.setStatus(SupplierApplicationStatus.PENDING);
        supplierRepository.save(app2);

        if (supplierRepository.findAllApplications().iterator().hasNext()) return;

        List<SupplierCapacity> caps3 = List.of(new SupplierCapacity("Tomato", LocalDate.now(), LocalDate.now().plusMonths(1), 100.0));
        byte[] dummyCertifiedOrganic3 = "Dummy PDF content".getBytes();
        SupplierApplication app3 = new SupplierApplication("Novo Fornecedor", "novo@test.pt", "999",
                new Address("Rua X", Municipality.BAIAO, Village.FRENDE, "Portugal", "4610"), dummyCertifiedOrganic3, 999L, caps3, LocalDate.now());
        app.setStatus(SupplierApplicationStatus.PENDING);
        supplierRepository.save(app3);
    }

    private void createProductBatches() {
        if (productBatchRepository.findAll().iterator().hasNext()) return;
        List<Supplier> suppliers = supplierRepository.findAll();
        if (suppliers.isEmpty()) {return;}
        Supplier supplier = suppliers.get(0);

        List<Product> products = new ArrayList<>();
        productRepository.findAll().forEach(products::add);
        products.forEach(product -> {
            ProductBatch batch = new ProductBatch(product, 50.0, LocalDate.now(), true, supplier);
            productBatchRepository.save(batch);
        });

        Collections.shuffle(products);
        products.stream().limit(2).forEach(product -> {
            List<ProductBatch> batches = productBatchRepository.findByProduct(product);
            if (!batches.isEmpty()) {
                ProductBatch batch = batches.get(0);
                batch.setQuarantined(true);
                productBatchRepository.save(batch);
            }
        });
    }

    private void createWaste() {
        if (wasteRepository.findAll().iterator().hasNext()) return;

        DiningHall h1 = diningHallRepository.findByName("Refeitório Principal").get();
        DiningHall h2 = diningHallRepository.findByName("Refeitório Engenharia").get();
        Canteen c1 = canteenRepository.findByName("Cantina Cinfaes").get();
        Canteen c2 = canteenRepository.findByName("Cantina ISEP").get();
        Supplier s = supplierRepository.findBySupplierEmail("sup0@test.pt");

        Random rd = new Random();

        for (int i = 0; i < 30; i++) {
            LocalDate date = LocalDate.now().minusDays(i);

            // REFEITÓRIO PRINCIPAL
            wasteRepository.save(new Waste(date,
                    150.0 + rd.nextDouble(30),
                    5.0 + rd.nextDouble(8),
                    4.0 + rd.nextDouble(4),
                    141.0 + rd.nextDouble(18),
                    null, h1, null));

            // REFEITÓRIO ENGENHARIA
            wasteRepository.save(new Waste(date,
                    55.0 + rd.nextDouble(15),
                    10.0 + rd.nextDouble(10),
                    3.0 + rd.nextDouble(5),
                    42.0 + rd.nextDouble(10),
                    null, h2, null));

            // CANTINA CINFAES
            wasteRepository.save(new Waste(date,
                    200.0 + rd.nextDouble(50),
                    25.0 + rd.nextDouble(15),
                    15.0 + rd.nextDouble(10),
                    160.0 + rd.nextDouble(25),
                    c1, null, null));

            // CANTINA ISEP
            wasteRepository.save(new Waste(date,
                    400.0 + rd.nextDouble(100),
                    40.0 + rd.nextDouble(30),
                    50.0 + rd.nextDouble(40),
                    310.0 + rd.nextDouble(30),
                    c2, null, null));

            // SUPPLIER
            wasteRepository.save(new Waste(date,
                    800.0 + rd.nextDouble(200),
                    15.0 + rd.nextDouble(15),
                    110.0 + rd.nextDouble(60),
                    675.0 + rd.nextDouble(125),
                    null, null, s));
        }
    }
}