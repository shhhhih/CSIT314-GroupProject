
package com.team.idp.config;

import com.team.idp.entity.IDP;
import com.team.idp.repository.IDPRepository;
import java.math.BigDecimal;
import java.util.List;
import com.team.idp.entity.Company;
import com.team.idp.repository.CompanyRepository;
import com.team.idp.entity.Category;
import com.team.idp.repository.CategoryRepository;
import com.team.idp.entity.AccountStatus;
import com.team.idp.entity.ProfileStatus;
import com.team.idp.entity.UserAccount;
import com.team.idp.entity.UserProfile;
import com.team.idp.repository.UserAccountRepository;
import com.team.idp.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("demo")
public class DemoDataSeeder {

    @Bean
    CommandLineRunner seedDemoData(
        IDPRepository idpRepository,
        CategoryRepository categoryRepository,
        UserProfileRepository profileRepository,
        UserAccountRepository accountRepository,
        CompanyRepository companyRepository,
        PasswordEncoder passwordEncoder) {
        return args -> {

            addCategory(categoryRepository, "Japandi",
                    "Japanese and Scandinavian inspired interiors");

            addCategory(categoryRepository, "Scandinavian",
                    "Bright interiors with natural materials");

            addCategory(categoryRepository, "Minimalist",
                    "Clean and simple interior designs");

            addCategory(categoryRepository, "Modern",
                    "Contemporary interiors with modern finishes");

            addCategory(categoryRepository, "Industrial",
                    "Raw textures, metal finishes and exposed elements");

            addCategory(categoryRepository, "Contemporary",
                    "Modern and current interior design styles");

            addCategory(categoryRepository, "Wabi-Sabi",
                    "Natural textures and imperfect beauty");

            addCategory(categoryRepository, "Luxury",
                    "Premium materials and elegant interiors");

            
UserProfile designerProfile = profileRepository
        .findByProfileName("Interior Designer")
        .orElseGet(() -> profileRepository.save(
                new UserProfile(
                        "Interior Designer",
                        "Interior designers who manage renovation projects",
                        ProfileStatus.ACTIVE)));

UserProfile customerProfile = profileRepository
        .findByProfileName("Customer")
        .orElseGet(() -> profileRepository.save(
                new UserProfile(
                        "Customer",
                        "Customers browsing renovation projects",
                        ProfileStatus.ACTIVE)));

for (int i = 1; i <= 5; i++) {
    addDemoAccount(
            accountRepository,
            passwordEncoder,
            "demo_designer" + i,
            "demo_designer" + i + "@example.com",
            designerProfile.getProfileID());

    addDemoAccount(
            accountRepository,
            passwordEncoder,
            "demo_customer" + i,
            "demo_customer" + i + "@example.com",
            customerProfile.getProfileID());
}

System.out.println("Demo accounts checked successfully!");


String[] companyNames = {
        "Haven Interior Studio",
        "Nordic Living Design",
        "Atelier Home Concepts",
        "Urban Nest Interiors",
        "The Design Collective"
};

String[] companyDescriptions = {
        "Specialising in Japandi and warm minimalist interiors.",
        "Creating Scandinavian-inspired residential spaces.",
        "Designing modern and contemporary homes.",
        "Transforming HDB flats into functional living spaces.",
        "Delivering luxury and bespoke interior designs."
};

for (int i = 1; i <= 5; i++) {

    String username = "demo_designer" + i;

    UserAccount designer = accountRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException(
                    "Demo designer account missing: " + username));

    if (companyRepository.findByUserID(designer.getUserID()).isPresent()) {
        continue;
    }

    Company company = new Company();

    company.setUserID(designer.getUserID());
    company.setCompanyName(companyNames[i - 1]);
    company.setDescription(companyDescriptions[i - 1]);
    company.setEmail(designer.getEmail());
    company.setPhone("6000" + String.format("%04d", i));
    company.setAddress("Demo Address " + i + ", Singapore");
    company.setWebsite(null);

    companyRepository.save(company);

    System.out.println("Added demo company: " + company.getCompanyName());
}

System.out.println("Demo companies checked successfully!");


String[][] demoProjects = {
        {"1", "Japandi Kitchen Renovation", "Warm wood finishes, neutral colours and a practical kitchen layout.", "45000", "HDB", "Japandi"},
        {"1", "Minimalist HDB Living Room", "A clean and functional HDB living room with concealed storage.", "38000", "HDB", "Minimalist"},
        {"1", "Wabi-Sabi Family Home", "Natural textures, earthy colours and calming living spaces.", "72000", "Condo", "Wabi-Sabi"},

        {"2", "Scandinavian HDB Makeover", "Bright interiors, timber furniture and an open-concept layout.", "52000", "HDB", "Scandinavian"},
        {"2", "Nordic Kitchen Transformation", "A Scandinavian kitchen with light cabinetry and efficient storage.", "43000", "Condo", "Scandinavian"},
        {"2", "Minimalist Condo Bedroom", "A peaceful bedroom with built-in wardrobes and soft lighting.", "26000", "Condo", "Minimalist"},

        {"3", "Modern Luxury Condominium", "A contemporary condominium with elegant finishes and spacious interiors.", "120000", "Condo", "Luxury"},
        {"3", "Contemporary Kitchen Design", "A stylish kitchen with a central island and modern appliances.", "65000", "Condo", "Contemporary"},
        {"3", "Modern Landed Home", "A complete redesign of a landed property with modern furnishings.", "210000", "Landed", "Modern"},

        {"4", "Industrial HDB Apartment", "Exposed textures, black metal accents and practical furniture.", "58000", "HDB", "Industrial"},
        {"4", "Compact HDB Kitchen", "Space-saving cabinets and smart storage for a smaller kitchen.", "28000", "HDB", "Modern"},
        {"4", "Contemporary Family Living", "An inviting living and dining area with flexible layouts.", "76000", "HDB", "Contemporary"},

        {"5", "Luxury Penthouse Interior", "Premium materials, statement lighting and bespoke carpentry.", "250000", "Condo", "Luxury"},
        {"5", "Japandi Condo Retreat", "A relaxing home featuring light wood and Japanese-inspired detailing.", "88000", "Condo", "Japandi"},
        {"5", "Modern Industrial Loft", "Industrial details combined with modern furniture and open spaces.", "135000", "Condo", "Industrial"},

        {"1", "Scandinavian Study Room", "A bright home office designed for productivity and comfort.", "18000", "HDB", "Scandinavian"},
        {"2", "Wabi-Sabi Bedroom Retreat", "A restful bedroom with natural materials and muted tones.", "32000", "Condo", "Wabi-Sabi"},
        {"3", "Luxury Landed Kitchen", "A premium kitchen featuring customised cabinetry and spacious counters.", "180000", "Landed", "Luxury"}
};

for (String[] projectData : demoProjects) {

    int designerNumber = Integer.parseInt(projectData[0]);
    String title = projectData[1];
    String description = projectData[2];
    BigDecimal budget = new BigDecimal(projectData[3]);
    String propertyType = projectData[4];
    String categoryName = projectData[5];

    UserAccount designer = accountRepository
            .findByUsername("demo_designer" + designerNumber)
            .orElseThrow();

    Company company = companyRepository
            .findByUserID(designer.getUserID())
            .orElseThrow();

    Category category = categoryRepository.findAll().stream()
            .filter(c -> c.getCategoryName().equalsIgnoreCase(categoryName))
            .findFirst()
            .orElseThrow();

    List<IDP> existingProjects = idpRepository.findByCompanyID(company.getCompanyID());

    boolean alreadyExists = existingProjects.stream()
            .anyMatch(project -> project.getTitle().equals(title));

    if (alreadyExists) {
        continue;
    }

    IDP project = new IDP(
            company.getCompanyID(),
            category.getCategoryID(),
            title,
            description,
            budget,
            propertyType,
            "Published"
    );

    idpRepository.save(project);

    System.out.println("Added demo IDP: " + title);
}

System.out.println("Demo renovation projects checked successfully!");



            System.out.println("Demo categories checked successfully!");
        };
    }

    private void addCategory(CategoryRepository repository,
                             String name,
                             String description) {

        if (!repository.existsByCategoryNameIgnoreCase(name)) {
            repository.save(new Category(name, description));
            System.out.println("Added category: " + name);
        }
    }

    
private void addDemoAccount(
        UserAccountRepository accountRepository,
        PasswordEncoder passwordEncoder,
        String username,
        String email,
        Long profileID) {

    if (accountRepository.findByUsername(username).isPresent()) {
        return;
    }

    if (accountRepository.findByEmail(email).isPresent()) {
        return;
    }

    String demoPassword = System.getenv("IDP_DEMO_PASSWORD");

    if (demoPassword == null || demoPassword.isBlank()) {
        throw new IllegalStateException(
                "Set IDP_DEMO_PASSWORD before running the demo profile.");
    }

    UserAccount account = new UserAccount(
            username,
            email,
            passwordEncoder.encode(demoPassword),
            profileID,
            AccountStatus.ACTIVE
    );

    accountRepository.save(account);

    System.out.println("Added demo account: " + username);
}

}



