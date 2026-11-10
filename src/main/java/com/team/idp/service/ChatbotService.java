
package com.team.idp.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final NamedParameterJdbcTemplate jdbc;

    public ChatbotService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private record Alias(String phrase, String value) {}

    // =========================================================
    // RECOGNIZED ROOMS
    // =========================================================

    private static final List<Alias> ROOMS = List.of(
        new Alias("guest bedroom", "Guest Room"),
        new Alias("guest room", "Guest Room"),
        new Alias("guestroom", "Guest Room"),
        new Alias("master bedroom", "Bedroom"),
        new Alias("kids bedroom", "Kids Room"),
        new Alias("kids room", "Kids Room"),
        new Alias("children room", "Kids Room"),
        new Alias("nursery", "Kids Room"),
        new Alias("living room", "Living Room"),
        new Alias("livingroom", "Living Room"),
        new Alias("dining room", "Dining Room"),
        new Alias("home office", "Office"),
        new Alias("study room", "Office"),
        new Alias("work from home", "Office"),
        new Alias("walk in wardrobe", "Wardrobe"),
        new Alias("walk in closet", "Wardrobe"),
        new Alias("laundry room", "Laundry Room"),
        new Alias("utility room", "Laundry Room"),
        new Alias("gaming room", "Gaming Room"),
        new Alias("game room", "Gaming Room"),
        new Alias("home theater", "Home Theater"),
        new Alias("home theatre", "Home Theater"),
        new Alias("full home", "Full Home"),
        new Alias("whole house", "Full Home"),
        new Alias("whole home", "Full Home"),
        new Alias("restroom", "Bathroom"),
        new Alias("washroom", "Bathroom"),
        new Alias("toilet", "Bathroom"),
        new Alias("bathroom", "Bathroom"),
        new Alias("kitchen", "Kitchen"),
        new Alias("bedroom", "Bedroom"),
        new Alias("office", "Office"),
        new Alias("wardrobe", "Wardrobe"),
        new Alias("balcony", "Balcony"),
        new Alias("patio", "Balcony"),
        new Alias("backyard", "Garden"),
        new Alias("back yard", "Garden"),
        new Alias("front yard", "Garden"),
        new Alias("courtyard", "Garden"),
        new Alias("outdoor space", "Garden"),
        new Alias("garden", "Garden"),
        new Alias("garage", "Garage")
    );

    // =========================================================
    // RECOGNIZED PROPERTY TYPES
    // =========================================================

    private static final List<Alias> PROPERTIES = List.of(
        new Alias("condominium", "Condo"),
        new Alias("condo", "Condo"),
        new Alias("public housing", "HDB"),
        new Alias("government flat", "HDB"),
        new Alias("hdb", "HDB"),
        new Alias("semi detached", "Landed"),
        new Alias("terrace house", "Landed"),
        new Alias("landed house", "Landed"),
        new Alias("bungalow", "Landed"),
        new Alias("landed", "Landed"),
        new Alias("commercial space", "Commercial"),
        new Alias("commercial", "Commercial"),
        new Alias("shop lot", "Commercial")
    );

    // =========================================================
    // RECOGNIZED DESIGN STYLES
    // =========================================================

    private static final List<Alias> STYLES = List.of(
        new Alias("mid century modern", "Mid-Century Modern"),
        new Alias("scandinavian", "Scandinavian"),
        new Alias("minimalist", "Minimalist"),
        new Alias("industrial", "Industrial"),
        new Alias("contemporary", "Contemporary"),
        new Alias("japandi", "Japandi"),
        new Alias("bohemian", "Bohemian"),
        new Alias("traditional", "Traditional"),
        new Alias("farmhouse", "Farmhouse"),
        new Alias("coastal", "Coastal"),
        new Alias("luxury", "Luxury"),
        new Alias("modern", "Modern"),
        new Alias("rustic", "Rustic"),
        new Alias("zen", "Zen")
    );

    private static final Pattern MONEY = Pattern.compile(
        "(?i)(?<![a-z0-9])" +
        "(?:s\\$\\s*|sgd\\s*|\\$\\s*)?" +
        "(\\d{1,3}(?:,\\d{3})+|" +
        "\\d{4,8}|" +
        "\\d{1,5}(?:\\.\\d+)?\\s*k)" +
        "(?![a-z0-9])"
    );

    // Uses the live IDP database.
    // Never hardcodes project IDs, counts or budgets.
    private static final String BASE_QUERY = """
        FROM idps i
        LEFT JOIN categories c
            ON i.categoryid = c.categoryid
        WHERE LOWER(i.project_status) = 'published'
        """;

    // =========================================================
    // MAIN CHATBOT CONVERSATION
    // =========================================================

    public Map<String, Object> reply(
        String message,
        Map<String, String> previous
    ) {
        Map<String, String> c = clean(previous);
        String input = message == null ? "" : message.trim();

        if (input.isEmpty() || input.length() > 500) {
            return say(
                "Could you send me a message of up to 500 characters? 💚",
                c, List.of(), List.of("Start over")
            );
        }

        String q = norm(input);

        // 1. Conversation controls

        if (has(q, "start over", "reset chat", "restart chat",
                   "clear chat", "new conversation")) {

            c.clear();

            return say(
                "Of course! 🌷 Let's start fresh. Which part of your " +
                "home would you love to transform?",
                c, List.of(), roomOptions()
            );
        }

        if (has(q, "thank you", "thankyou", "thanks",
                   "thank u", "thx")) {

            return say(
                "You're so welcome! 💚 Thank you for exploring IDP " +
                "with me. I hope you find a design you absolutely love! 🏡✨",
                c, List.of(),
                List.of("Show projects", "Start over")
            );
        }

        if (has(q, "goodbye", "bye", "see you")) {
            return say(
                "Thank you for chatting with IDP! 🌷 Wishing you a " +
                "wonderful renovation journey. See you again! 👋💚",
                c, List.of(), List.of("Start over")
            );
        }

        if (q.matches(
            "(hi+|hey+|hello+|good morning|" +
            "good afternoon|good evening)"
        )) {
            return say(
                "Hey there! 👋💚 I'm your IDP renovation assistant. " +
                "What part of your home are we dreaming of transforming?",
                c, List.of(), roomOptions()
            );
        }

        // 2. Website help

        if (has(q, "what is hdb", "what does hdb mean",
                   "what is landed", "what does landed mean",
                   "property types", "explain property types")) {
            return propertyHelp(c);
        }

        if (has(q, "favourites", "favorites", "favourite",
                   "favorite", "shortlist", "saved projects")) {

            return say(
                "You can open a design project and use its Save or " +
                "Favourite option if it's available for your account. 💚",
                c, List.of(),
                List.of("Find projects", "Start over")
            );
        }

        if (has(q, "log in", "login", "sign up", "register")) {
            return say(
                "You can use the IDP website's login or registration " +
                "page to access customer features. 😊",
                c, List.of(), List.of("Find projects")
            );
        }

        // 3. Clearly unrelated requests

        if (isOutOfScope(q)) {
            return outOfScope(c);
        }

        // 4. Change existing preferences

        if (has(q, "change room", "different room")) {
            c.remove("room");
            clearSearchProgress(c);

            return say(
                "Of course! 🌷 Which room would you like to explore?",
                c, List.of(), roomOptions()
            );
        }

        if (has(q, "change property", "different property")) {
            c.remove("property");
            c.remove("propertyPending");
            c.remove("propertySkipped");
            clearSearchProgress(c);

            return askProperty(c);
        }

        if (has(q, "change budget", "different budget")) {
            clearBudget(c);
            clearSearchProgress(c);
            return askBudget(c);
        }

        // NEW: Broaden search without deleting preferences.
        if (has(q, "broaden search", "show more options")) {
            return broadenSearch(c);
        }

        // NEW: Only show other rooms when explicitly requested.
        if (has(q, "explore all projects",
                   "browse all projects",
                   "all project types")) {
            return exploreAllProjects(c);
        }

        // 5. Recognize direct property types

        String directProperty = matchAlias(q, PROPERTIES);

        // Explicit corrections:
        // "Actually, it's Condo, not Landed."

        if (has(q, "not landed") &&
            has(q, "condo", "hdb")) {
            directProperty = has(q, "condo")
                ? "Condo" : "HDB";
        }

        if (has(q, "not condo") &&
            has(q, "landed", "hdb")) {
            directProperty = has(q, "landed")
                ? "Landed" : "HDB";
        }

        if (has(q, "not hdb") &&
            has(q, "condo", "landed")) {
            directProperty = has(q, "condo")
                ? "Condo" : "Landed";
        }

        if (directProperty != null) {
            if (!directProperty.equals(c.get("property"))) {
                clearSearchProgress(c);
            }

            c.put("property", directProperty);
            c.remove("propertyPending");
            c.remove("propertySkipped");
            c.remove("stage");
        }

        // 6. Confirm inferred housing types

        if (directProperty == null &&
            "propertyConfirm".equals(c.get("stage"))) {

            if (has(q, "yes", "correct", "thats right",
                       "that is right", "yep", "yeah")) {

                String pending = c.get("propertyPending");

                if (pending != null) {
                    c.put("property", pending);
                }

                c.remove("propertyPending");
                c.remove("propertySkipped");
                c.remove("stage");
                clearSearchProgress(c);

                return continueConversation(c);
            }

            if (has(q, "no", "nope", "not really", "wrong")) {
                c.remove("propertyPending");
                return askProperty(c);
            }
        }

        if (has(q, "continue current room", "keep current room",
                   "continue with bathroom", "continue with kitchen")) {
            return continueConversation(c);
        }

        // 7. Property skip and uncertainty

        if (has(q, "skip property", "skip home type",
                   "any property type", "skip property type")) {

            c.remove("property");
            c.remove("propertyPending");
            c.put("propertySkipped", "true");
            c.remove("stage");
            clearSearchProgress(c);

            return continueConversation(c);
        }

        if (has(q, "help identify my home", "help identify")) {
            return propertyHelp(c);
        }

        boolean unsure = has(
            q, "im not sure", "i am not sure",
            "not sure", "i dont know",
            "i do not know", "no idea"
        );

        if (unsure && isPropertyStage(c)) {
            c.put("stage", "propertyUnknown");

            return say(
                "That's absolutely fine! 😊💚 I can explain the " +
                "different home types, or we can skip this question " +
                "and explore designs for all properties.",
                c, List.of(),
                List.of("Help identify my home", "Skip property")
            );
        }

        // 8. Room and garden understanding

        String room = matchAlias(q, ROOMS);

        boolean describesGarden = has(
            q, "garden", "backyard", "back yard",
            "courtyard", "yard"
        );

        boolean gardenDesignIntent =
            has(
                q, "redesign my garden", "design my garden",
                "renovate my garden", "garden makeover",
                "landscape my garden", "redo my garden",
                "decorate my garden", "garden renovation",
                "garden design", "outdoor makeover"
            )
            || q.equals("garden")
            || q.equals("backyard");

        boolean houseWithGarden = has(
            q, "house with a garden",
            "house with garden",
            "house and garden",
            "house has a garden",
            "i have a garden",
            "my home has a garden"
        );

        // A home with a garden is not automatically
        // a request to renovate the garden.

        if ("Garden".equals(room) &&
            describesGarden &&
            !gardenDesignIntent &&
            houseWithGarden) {
            room = null;
        }

        if (room != null) {
            if (!room.equals(c.get("room"))) {
                clearSearchProgress(c);
            }

            c.put("room", room);
        }

        String style = matchAlias(q, STYLES);

        if (style != null) {
            if (!style.equals(c.get("style"))) {
                clearSearchProgress(c);
            }

            c.put("style", style);
        }

        // 9. Indirect housing descriptions

        if (directProperty == null &&
            !c.containsKey("property") &&
            !c.containsKey("propertySkipped") &&
            houseWithGarden &&
            !gardenDesignIntent) {

            c.put("propertyPending", "Landed");
            c.put("stage", "propertyConfirm");

            return say(
                "A home with a garden sounds lovely! 🌿🏡 " +
                "It might be a landed house, meaning a house " +
                "with its own land. Does that sound right?",
                c, List.of(),
                List.of("Yes, that's right", "No", "I'm not sure")
            );
        }

        if (directProperty == null &&
            !c.containsKey("property") &&
            !c.containsKey("propertySkipped") &&
            has(q, "apartment", "high rise", "high floor",
                   "live in a flat", "15th floor", "12th floor")) {

            c.put("stage", "property");

            return say(
                "Got it! 🏢 Is it an HDB flat (Singapore public " +
                "housing) or a private Condo? It's okay if you " +
                "aren't sure. 💚",
                c, List.of(),
                List.of(
                    "HDB", "Condo",
                    "I'm not sure", "Skip property"
                )
            );
        }

        if (room == null &&
            describesGarden &&
            !gardenDesignIntent &&
            !houseWithGarden) {

            String current = c.get("room");

            if (current != null) {
                return say(
                    "A garden sounds wonderful! 🌿💚 " +
                    "Would you like to switch to garden design, " +
                    "or continue planning your " +
                    current.toLowerCase(Locale.ROOT) + "?",
                    c, List.of(),
                    List.of("Garden", "Continue current room")
                );
            }

            return say(
                "Oh, you have a garden! 🌸 Are you hoping to " +
                "redesign it or another part of your home?",
                c, List.of(),
                List.of("Garden", "Kitchen", "Bathroom")
            );
        }

        // 10. Budget recognition

        List<Long> amounts = money(input);

        if (!amounts.isEmpty()) {
            clearBudget(c);
            clearSearchProgress(c);

            long first = amounts.get(0);

            if (amounts.size() >= 2) {
                long second = amounts.get(1);

                c.put(
                    "budgetMin",
                    String.valueOf(Math.min(first, second))
                );

                c.put(
                    "budgetMax",
                    String.valueOf(Math.max(first, second))
                );

                c.put("budgetMode", "range");

            } else if (has(
                q, "around", "about", "roughly",
                "approximately", "approx"
            )) {

                c.put(
                    "budgetMin",
                    String.valueOf(Math.round(first * 0.8))
                );

                c.put(
                    "budgetMax",
                    String.valueOf(Math.round(first * 1.2))
                );

                c.put("budgetMode", "approximate");

            } else if (has(
                q, "above", "over", "more than",
                "at least", "minimum"
            )) {

                c.put("budgetMin", String.valueOf(first));
                c.put("budgetMode", "minimum");

            } else {
                // A single figure such as "50k"
                // is treated as a maximum budget.
                c.put("budgetMax", String.valueOf(first));
                c.put("budgetMode", "maximum");
            }

            c.remove("stage");
        }

        if (has(
            q, "affordable", "cheap", "cheaper",
            "budget friendly", "low cost",
            "dont have much money", "limited budget",
            "show affordable projects"
        )) {

            clearBudget(c);
            clearSearchProgress(c);
            c.put("budgetMode", "affordable");
            c.remove("stage");
        }

        if (has(
            q, "skip budget", "any budget",
            "no budget limit", "unlimited budget",
            "money isnt an issue"
        )) {

            clearBudget(c);
            clearSearchProgress(c);
            c.put("budgetSkipped", "true");
            c.remove("stage");
        }

        if (has(
            q, "help me understand costs",
            "show project costs",
            "how much does it cost",
            "how much will it cost",
            "renovation cost guidance"
        )) {
            return costGuidance(c);
        }

        if (unsure && isBudgetStage(c)) {
            c.put("stage", "budgetUnknown");

            return say(
                "No worries at all! 💚 Would you prefer to see " +
                "affordable projects, learn about project costs, " +
                "or skip the budget for now?",
                c, List.of(),
                List.of(
                    "Show affordable projects",
                    "Help me understand costs",
                    "Skip budget"
                )
            );
        }

        // 11. Detect requests for results

        boolean wantResults = has(
            q, "show projects", "find projects",
            "browse projects", "recommend",
            "show me", "see projects",
            "find designs", "search projects",
            "show affordable projects"
        );

        if (has(
            q, "skip budget", "show affordable projects"
        )) {
            wantResults = true;
        }

        // 12. Unknown and off-topic questions

        if (isUnknownTopic(
            q, c, room, directProperty,
            style, amounts, wantResults
        )) {
            return outOfScope(c);
        }

        // 13. Continue naturally

        boolean hasPreferences =
            c.containsKey("room") ||
            c.containsKey("property") ||
            c.containsKey("style") ||
            hasBudget(c);

        if (!hasPreferences && !wantResults) {
            return say(
                "I'd love to help! 🌷 Tell me about the room you'd " +
                "like to renovate, or select Find projects to browse.",
                c, List.of(), roomOptions()
            );
        }

        if (wantResults) {
            return search(c);
        }

        return continueConversation(c);
    }

    // =========================================================
    // EXISTING CONVERSATION FLOW
    // =========================================================

    private Map<String, Object> continueConversation(
        Map<String, String> c
    ) {
        if (!c.containsKey("property") &&
            !c.containsKey("propertySkipped")) {
            return askProperty(c);
        }

        if (!hasBudget(c)) {
            return askBudget(c);
        }

        return search(c);
    }

    private Map<String, Object> askProperty(
        Map<String, String> c
    ) {
        c.put("stage", "property");

        return say(
            "That sounds exciting! 🏡✨ What kind of home is it?\n\n" +
            "• HDB: a public-housing flat in Singapore\n" +
            "• Condo: a private apartment in a condominium\n" +
            "• Landed: a house with its own land\n\n" +
            "You can also choose 'I'm not sure'. 💚",
            c, List.of(),
            List.of(
                "HDB", "Condo", "Landed",
                "I'm not sure", "Skip property"
            )
        );
    }

    private Map<String, Object> propertyHelp(
        Map<String, String> c
    ) {
        c.put("stage", "property");

        return say(
            "Here's a simple guide! 🏡💚\n\n" +
            "HDB: Public housing apartments in Singapore.\n" +
            "Condo: Private apartment units in a condominium.\n" +
            "Landed: Terrace houses, semi-detached houses and " +
            "bungalows with their own land.\n\n" +
            "Which sounds closest to your home? You can also skip!",
            c, List.of(),
            List.of(
                "HDB", "Condo", "Landed", "Skip property"
            )
        );
    }

    private Map<String, Object> askBudget(
        Map<String, String> c
    ) {
        c.put("stage", "budget");

        return say(
            "Lovely! 💚 Do you have a budget in mind?\n\n" +
            "You could say 30k, 'between 20k and 40k', " +
            "or simply 'affordable'. No pressure if " +
            "you're still figuring things out! 😊",
            c, List.of(),
            List.of(
                "Under 20k",
                "20k to 50k",
                "Affordable",
                "I'm not sure",
                "Skip budget"
            )
        );
    }

    // =========================================================
    // COST GUIDANCE
    // =========================================================

    private Map<String, Object> costGuidance(
        Map<String, String> c
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                MIN(i.budget) AS minBudget,
                MAX(i.budget) AS maxBudget,
                COUNT(*) AS projectCount
            """ + BASE_QUERY);

        MapSqlParameterSource params =
            new MapSqlParameterSource();

        // Ignore the customer's budget when explaining
        // existing project budget ranges.
        appendFilters(sql, params, c, false);

        try {
            Map<String, Object> stats =
                jdbc.queryForMap(sql.toString(), params);

            Number count =
                (Number) stats.get("projectCount");

            if (count == null || count.longValue() == 0) {
                return say(
                    "I couldn't find comparable published project " +
                    "budgets yet. 💚 We can still browse designs " +
                    "without setting a budget.",
                    c, List.of(),
                    List.of("Skip budget", "Find projects")
                );
            }

            Object minValue = stats.get("minBudget");
            Object maxValue = stats.get("maxBudget");

            if (minValue == null || maxValue == null) {
                return say(
                    "These projects don't have enough budget " +
                    "information for a comparison yet. 💚",
                    c, List.of(),
                    List.of("Skip budget", "Find projects")
                );
            }

            BigDecimal min =
                new BigDecimal(String.valueOf(minValue));

            BigDecimal max =
                new BigDecimal(String.valueOf(maxValue));

            return say(
                "I checked " + count.longValue() +
                " matching published IDP projects! 🏡✨\n\n" +
                "Their listed budgets range from S$" +
                moneyText(min) + " to S$" +
                moneyText(max) +
                ".\n\nThese are portfolio project budgets, not " +
                "guaranteed renovation quotations. Would you " +
                "like to explore projects without setting a limit? 💚",
                c, List.of(),
                List.of(
                    "Skip budget",
                    "Show affordable projects",
                    "Change budget"
                )
            );

        } catch (Exception exception) {
            return say(
                "I'm sorry, I couldn't check project budgets " +
                "right now. 💚 You can still browse projects.",
                c, List.of(),
                List.of("Skip budget", "Find projects")
            );
        }
    }

    // =========================================================
    // NEW: EXACT-MATCH-FIRST RECOMMENDATION
    // =========================================================

    private Map<String, Object> search(
        Map<String, String> c
    ) {
        return searchFromTier(c, 0);
    }

    // =========================================================
    // NEW: FLEXIBLE BROADEN SEARCH
    // =========================================================

    private Map<String, Object> broadenSearch(
        Map<String, String> c
    ) {
        int previousTier = -1;

        try {
            previousTier = Integer.parseInt(
                c.getOrDefault("resultTier", "-1")
            );
        } catch (NumberFormatException ignored) {
            previousTier = -1;
        }

        int nextTier = Math.max(1, previousTier + 1);

        if (nextTier > 3) {
            return say(
                "We've already explored the broader options " +
                "for your selected room. 💚\n\n" +
                "Would you like to change your preferences " +
                "or explore projects from other rooms?",
                c, List.of(),
                List.of(
                    "Change room",
                    "Change budget",
                    "Explore all projects",
                    "Start over"
                )
            );
        }

        return searchFromTier(c, nextTier);
    }

    // =========================================================
    // NEW: PROGRESSIVE FILTER RELAXATION
    // =========================================================

    private Map<String, Object> searchFromTier(
        Map<String, String> c,
        int startingTier
    ) {
        String selectedRoom = c.get("room");

        try {
            // Check room availability independently of
            // budget, style and property restrictions.
            //
            // This prevents an empty room category from
            // accidentally showing unrelated recommendations.

            if (selectedRoom != null &&
                !roomHasPublishedProjects(selectedRoom)) {

                c.put("resultTier", "3");

                return say(
                    "I'm sorry! 💚 There aren't any published " +
                    selectedRoom.toLowerCase(Locale.ROOT) +
                    " projects available right now.\n\n" +
                    "Would you like to explore another room " +
                    "or browse all available renovation projects? 🏡✨",
                    c, List.of(),
                    List.of(
                        "Change room",
                        "Explore all projects",
                        "Start over"
                    )
                );
            }

            Set<String> attempted =
                new HashSet<>();

            for (int tier = startingTier; tier <= 3; tier++) {

                // Temporary copy:
                // Customer's original preferences remain unchanged.
                Map<String, String> filters =
                    new LinkedHashMap<>(c);

                List<String> relaxed =
                    new ArrayList<>();

                // TIER 1: Ignore budget limits.
                if (tier >= 1 && hasNumericBudget(filters)) {
                    clearBudget(filters);
                    relaxed.add("budget");
                }

                // TIER 2: Ignore design style.
                if (tier >= 2 &&
                    filters.containsKey("style")) {

                    filters.remove("style");
                    relaxed.add("design style");
                }

                // TIER 3: Ignore property type.
                if (tier >= 3 &&
                    filters.containsKey("property")) {

                    filters.remove("property");
                    relaxed.add("property type");
                }

                // IMPORTANT:
                // The requested room is NEVER removed here.

                String signature =
                    filterSignature(filters);

                // Avoid repeating identical database queries.
                if (!attempted.add(signature)) {
                    continue;
                }

                List<Map<String, Object>> projects =
                    fetchProjects(filters);

                if (projects.isEmpty()) {
                    continue;
                }

                c.put(
                    "resultTier",
                    String.valueOf(tier)
                );

                if (relaxed.isEmpty()) {
                    return say(
                        "Yay! 🏡✨ I found " +
                        projects.size() +
                        " matching IDP project" +
                        (projects.size() == 1 ? "" : "s") +
                        " for you!\n\n" +
                        "These match the preferences you've " +
                        "shared with me. Take a look below! 💚",
                        c, projects,
                        List.of(
                            "Change room",
                            "Change budget",
                            "Broaden search",
                            "Start over"
                        )
                    );
                }

                String relaxedDescription =
                    String.join(", ", relaxed);

                return say(
                    "I found some related designs for you! 🏡✨\n\n" +
                    "I couldn't find an exact match for all " +
                    "your preferences, so I broadened the search " +
                    "by relaxing: " +
                    relaxedDescription + ".\n\n" +
                    (selectedRoom == null
                        ? "These are broader inspiration projects.\n\n"
                        : "I kept your selected " +
                          selectedRoom.toLowerCase(Locale.ROOT) +
                          " requirement.\n\n") +
                    "Your original preferences are still saved. " +
                    "Please check the listed project budgets " +
                    "and property types before deciding. 💚",
                    c, projects,
                    List.of(
                        "Change budget",
                        "Change room",
                        "Broaden search",
                        "Start over"
                    )
                );
            }

            c.put("resultTier", "3");

            if (selectedRoom != null) {
                return say(
                    "I found published projects for your selected " +
                    selectedRoom.toLowerCase(Locale.ROOT) +
                    ", but couldn't find suitable results under " +
                    "the current search options. 💚\n\n" +
                    "Would you like to change your preferences " +
                    "or explore other rooms?",
                    c, List.of(),
                    List.of(
                        "Change property",
                        "Change budget",
                        "Change room",
                        "Explore all projects"
                    )
                );
            }

            return say(
                "I couldn't find any suitable published projects " +
                "right now. 💚\n\n" +
                "You can change your preferences or try again later.",
                c, List.of(),
                List.of("Change room", "Start over")
            );

        } catch (Exception exception) {
            return say(
                "I'm sorry, I couldn't search the project " +
                "database right now. 💚 Please try again.",
                c, List.of(),
                List.of("Find projects", "Start over")
            );
        }
    }

    // =========================================================
    // NEW: CHECK WHETHER THE REQUESTED ROOM EXISTS
    // =========================================================

    private boolean roomHasPublishedProjects(
        String room
    ) {
        Map<String, String> roomOnly =
            new LinkedHashMap<>();

        roomOnly.put("room", room);

        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) " + BASE_QUERY
        );

        MapSqlParameterSource params =
            new MapSqlParameterSource();

        appendFilters(
            sql, params, roomOnly, false
        );

        Long count = jdbc.queryForObject(
            sql.toString(),
            params,
            Long.class
        );

        return count != null && count > 0;
    }

    // =========================================================
    // NEW: EXPLORE ALL PROJECTS
    // =========================================================

    private Map<String, Object> exploreAllProjects(
        Map<String, String> c
    ) {
        try {
            List<Map<String, Object>> projects =
                fetchProjects(Map.of());

            if (projects.isEmpty()) {
                return say(
                    "There aren't any published projects " +
                    "available right now. 💚",
                    c, List.of(),
                    List.of("Start over")
                );
            }

            return say(
                "Of course! 🏡✨ Here are some published " +
                "IDP projects for inspiration.\n\n" +
                "These may include other rooms, styles, " +
                "property types and budgets.\n\n" +
                "Your original preferences are still saved. 💚",
                c, projects,
                List.of(
                    "Change room",
                    "Change budget",
                    "Start over"
                )
            );

        } catch (Exception exception) {
            return say(
                "I'm sorry, I couldn't load the published " +
                "projects right now. 💚 Please try again.",
                c, List.of(),
                List.of(
                    "Explore all projects",
                    "Start over"
                )
            );
        }
    }

    // =========================================================
    // NEW: LIVE DATABASE SEARCH
    // =========================================================

    private List<Map<String, Object>> fetchProjects(
        Map<String, String> filters
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                i.idpid AS idpId,
                i.title AS title,
                i.budget AS budget,
                i.property_type AS propertyType,
                c.category_name AS categoryName
            """ + BASE_QUERY);

        MapSqlParameterSource params =
            new MapSqlParameterSource();

        appendFilters(sql, params, filters, true);

        // For approximate budgets, prioritize projects
        // closest to the customer's target amount.
        if ("approximate".equals(filters.get("budgetMode")) &&
            filters.containsKey("budgetMin") &&
            filters.containsKey("budgetMax")) {

            long low =
                Long.parseLong(filters.get("budgetMin"));

            long high =
                Long.parseLong(filters.get("budgetMax"));

            params.addValue(
                "budgetMid",
                BigDecimal.valueOf(
                    low + (high - low) / 2
                )
            );

            sql.append(
                " ORDER BY ABS(i.budget - :budgetMid) ASC, " +
                "i.idpid ASC LIMIT 5"
            );

        } else {
            sql.append(
                " ORDER BY i.budget ASC, i.idpid ASC LIMIT 5"
            );
        }

        return jdbc.queryForList(
            sql.toString(),
            params
        );
    }

    // =========================================================
    // DATABASE FILTERS - SUPPORT ALL RECOGNIZED ROOMS
    // =========================================================

    private void appendFilters(
        StringBuilder sql,
        MapSqlParameterSource params,
        Map<String, String> c,
        boolean includeBudget
    ) {
        // Property type

        if (c.containsKey("property")) {
            sql.append(
                " AND LOWER(i.property_type) = LOWER(:property)"
            );

            params.addValue(
                "property",
                c.get("property")
            );
        }

        // Room type

        if (c.containsKey("room")) {
            List<String> terms = switch (c.get("room")) {

                case "Kitchen" ->
                    List.of("kitchen");

                case "Bathroom" ->
                    List.of("bathroom", "restroom", "toilet",
                            "washroom");

                case "Bedroom" ->
                    List.of("bedroom", "master bedroom",
                            "bedroom retreat");

                case "Guest Room" ->
                    List.of("guest room", "guest bedroom",
                            "guestroom");

                case "Kids Room" ->
                    List.of("kids room", "kids bedroom",
                            "children room", "nursery");

                case "Living Room" ->
                    List.of("living room", "living area",
                            "lounge");

                case "Dining Room" ->
                    List.of("dining room", "dining area");

                case "Office" ->
                    List.of("home office", "office",
                            "study room", "study");

                case "Wardrobe" ->
                    List.of("wardrobe", "walk in closet",
                            "dressing room");

                case "Laundry Room" ->
                    List.of("laundry room", "utility room",
                            "laundry area");

                case "Gaming Room" ->
                    List.of("gaming room", "game room",
                            "gaming space");

                case "Home Theater" ->
                    List.of("home theater", "home theatre",
                            "cinema room");

                case "Balcony" ->
                    List.of("balcony", "patio",
                            "terrace balcony");

                case "Garden" ->
                    List.of("garden", "backyard",
                            "landscape", "outdoor");

                case "Garage" ->
                    List.of("garage", "carport");

                case "Full Home" ->
                    List.of("full home", "whole house",
                            "whole home", "entire home");

                default ->
                    List.of(
                        c.get("room")
                            .toLowerCase(Locale.ROOT)
                    );
            };

            sql.append(" AND (");

            for (int i = 0; i < terms.size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }

                sql.append(
                    "(LOWER(i.title) LIKE :room" + i +
                    " OR LOWER(COALESCE(i.description,'')) " +
                    "LIKE :room" + i + ")"
                );

                params.addValue(
                    "room" + i,
                    "%" + escapeLike(terms.get(i)) + "%"
                );
            }

            sql.append(")");
        }

        // Design style

        if (c.containsKey("style")) {
            sql.append("""
                AND (
                    LOWER(i.title) LIKE :style
                    OR LOWER(COALESCE(i.description,'')) LIKE :style
                    OR LOWER(COALESCE(c.category_name,'')) LIKE :style
                )
                """);

            params.addValue(
                "style",
                "%" + escapeLike(
                    c.get("style")
                        .toLowerCase(Locale.ROOT)
                ) + "%"
            );
        }

        // Budget filters

        if (includeBudget) {

            if (c.containsKey("budgetMin")) {
                sql.append(
                    " AND i.budget >= :minBudget"
                );

                params.addValue(
                    "minBudget",
                    new BigDecimal(c.get("budgetMin"))
                );
            }

            if (c.containsKey("budgetMax")) {
                sql.append(
                    " AND i.budget <= :maxBudget"
                );

                params.addValue(
                    "maxBudget",
                    new BigDecimal(c.get("budgetMax"))
                );
            }
        }
    }

    // =========================================================
    // NEW: SEARCH STATE HELPERS
    // =========================================================

    private static void clearSearchProgress(
        Map<String, String> c
    ) {
        c.remove("resultTier");
    }

    private static boolean hasNumericBudget(
        Map<String, String> c
    ) {
        return c.containsKey("budgetMin") ||
               c.containsKey("budgetMax");
    }

    private static String filterSignature(
        Map<String, String> c
    ) {
        return String.join("|",
            c.getOrDefault("room", ""),
            c.getOrDefault("property", ""),
            c.getOrDefault("style", ""),
            c.getOrDefault("budgetMin", ""),
            c.getOrDefault("budgetMax", "")
        );
    }

    // =========================================================
    // OFF-TOPIC HANDLING
    // =========================================================

    private static Map<String, Object> outOfScope(
        Map<String, String> c
    ) {
        return say(
            "That's a little outside my specialty! 😊💚\n\n" +
            "I'm the IDP Renovation Assistant, so I can help " +
            "with interior design, room makeovers, renovation " +
            "budgets, home types and finding projects on IDP. 🏡✨\n\n" +
            "What would you like to explore?",
            c, List.of(),
            List.of(
                "Kitchen",
                "Bathroom",
                "Find projects",
                "Start over"
            )
        );
    }

    private static boolean isOutOfScope(String q) {
        return has(
            q,
            "weather today",
            "weather tomorrow",
            "weather forecast",
            "what is the weather",
            "tell me a joke",
            "who is taylor swift",
            "football score",
            "soccer score",
            "basketball score",
            "stock market",
            "bitcoin price",
            "crypto price",
            "election result",
            "who is the president",
            "capital of france",
            "capital of japan",
            "math homework",
            "help with my homework",
            "solve this equation",
            "write python code",
            "write a python program",
            "write java code",
            "write my assignment",
            "write an essay",
            "recommend a movie",
            "what time is it",
            "play some music"
        );
    }

    private static boolean isUnknownTopic(
        String q,
        Map<String, String> c,
        String room,
        String property,
        String style,
        List<Long> amounts,
        boolean wantResults
    ) {
        if (room != null || property != null ||
            style != null || !amounts.isEmpty() ||
            wantResults) {
            return false;
        }

        if (has(
            q, "renovate", "renovation", "reno",
            "remodel", "remodeling", "makeover",
            "decorate", "design", "interior",
            "furniture", "flooring", "lighting",
            "wall", "paint", "tiles", "cabinet",
            "countertop", "layout", "garden",
            "backyard", "landscape", "outdoor",
            "house", "home", "apartment", "flat",
            "property", "budget", "budjet",
            "affordable", "cheap", "cheaper",
            "cost", "price", "estimate", "designer",
            "idp", "project", "projects",
            "change", "skip", "continue",
            "not sure", "unsure", "dont know"
        )) {
            return false;
        }

        if (has(
            q, "yes", "no", "maybe", "correct",
            "thats right", "yep", "yeah", "nope"
        )) {
            return false;
        }

        if (c.containsKey("stage") &&
            q.split(" ").length <= 4) {
            return false;
        }

        return q.split(" ").length >= 3;
    }

    // =========================================================
    // CONVERSATION STAGE HELPERS
    // =========================================================

    private static boolean isPropertyStage(
        Map<String, String> c
    ) {
        return List.of(
            "property",
            "propertyConfirm",
            "propertyUnknown"
        ).contains(c.getOrDefault("stage", ""));
    }

    private static boolean isBudgetStage(
        Map<String, String> c
    ) {
        return List.of(
            "budget",
            "budgetUnknown"
        ).contains(c.getOrDefault("stage", ""));
    }

    private static boolean hasBudget(
        Map<String, String> c
    ) {
        return c.containsKey("budgetMin") ||
               c.containsKey("budgetMax") ||
               c.containsKey("budgetSkipped") ||
               "affordable".equals(c.get("budgetMode"));
    }

    private static void clearBudget(
        Map<String, String> c
    ) {
        c.remove("budgetMin");
        c.remove("budgetMax");
        c.remove("budgetMode");
        c.remove("budgetSkipped");
    }

    // =========================================================
    // BUDGET PARSING
    // =========================================================

    private static List<Long> money(String input) {
        List<Long> result = new ArrayList<>();
        Matcher matcher = MONEY.matcher(input);

        while (matcher.find() && result.size() < 2) {
            try {
                String raw = matcher.group(1)
                    .replace(",", "")
                    .replace(" ", "")
                    .toLowerCase(Locale.ROOT);

                boolean thousands =
                    raw.endsWith("k");

                if (thousands) {
                    raw = raw.substring(
                        0, raw.length() - 1
                    );
                }

                double value =
                    Double.parseDouble(raw) *
                    (thousands ? 1000 : 1);

                long amount = Math.round(value);

                if (amount >= 1000 &&
                    amount <= 50_000_000) {
                    result.add(amount);
                }

            } catch (NumberFormatException ignored) {
                // Ignore invalid money expressions.
            }
        }

        return result;
    }

    // =========================================================
    // ROOM / PROPERTY / STYLE RECOGNITION
    // =========================================================

    private static String matchAlias(
        String q,
        List<Alias> aliases
    ) {
        // Exact match before fuzzy matching.
        for (Alias alias : aliases) {
            if (phrase(q, norm(alias.phrase()))) {
                return alias.value();
            }
        }

        String[] words = q.split(" ");

        for (Alias alias : aliases) {
            String[] expected =
                norm(alias.phrase()).split(" ");

            for (
                int i = 0;
                i <= words.length - expected.length;
                i++
            ) {
                boolean matches = true;

                for (int j = 0; j < expected.length; j++) {
                    if (!close(words[i + j], expected[j])) {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    return alias.value();
                }
            }
        }

        return null;
    }

    private static boolean close(
        String word,
        String target
    ) {
        if (word.equals(target)) {
            return true;
        }

        if (word.length() < 4 ||
            target.length() < 5 ||
            word.charAt(0) != target.charAt(0)) {
            return false;
        }

        int limit = target.length() >= 9 ? 2 : 1;

        if (Math.abs(
            word.length() - target.length()
        ) > limit) {
            return false;
        }

        int[] old =
            new int[target.length() + 1];

        int[] now =
            new int[target.length() + 1];

        for (int j = 0; j <= target.length(); j++) {
            old[j] = j;
        }

        for (int i = 1; i <= word.length(); i++) {
            now[0] = i;

            for (int j = 1; j <= target.length(); j++) {
                now[j] = Math.min(
                    Math.min(
                        now[j - 1] + 1,
                        old[j] + 1
                    ),
                    old[j - 1] +
                    (
                        word.charAt(i - 1) ==
                        target.charAt(j - 1)
                            ? 0 : 1
                    )
                );
            }

            int[] temporary = old;
            old = now;
            now = temporary;
        }

        return old[target.length()] <= limit;
    }

    private static String norm(String text) {
        return text.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ")
            .trim()
            .replaceAll("\\s+", " ");
    }

    private static boolean phrase(
        String q,
        String p
    ) {
        return Pattern.compile(
            "(?<![a-z0-9])" +
            Pattern.quote(p) +
            "(?![a-z0-9])"
        ).matcher(q).find();
    }

    private static boolean has(
        String q,
        String... values
    ) {
        for (String value : values) {
            if (phrase(q, norm(value))) {
                return true;
            }
        }

        return false;
    }

    private static String escapeLike(
        String value
    ) {
        return value.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }

    // =========================================================
    // CONTEXT VALIDATION AND MEMORY
    // =========================================================

    private static Map<String, String> clean(
        Map<String, String> previous
    ) {
        Map<String, String> c =
            new LinkedHashMap<>();

        if (previous == null) {
            return c;
        }

        for (String key : List.of(
            "room",
            "property",
            "style",
            "budgetMin",
            "budgetMax",
            "budgetMode",
            "propertyPending",
            "propertySkipped",
            "budgetSkipped",
            "stage",
            "resultTier"
        )) {
            String value = previous.get(key);

            if (value != null && value.length() <= 100) {
                c.put(key, value);
            }
        }

        if (c.containsKey("property") &&
            !List.of(
                "HDB", "Condo", "Landed", "Commercial"
            ).contains(c.get("property"))) {
            c.remove("property");
        }

        if (c.containsKey("propertyPending") &&
            !"Landed".equals(c.get("propertyPending"))) {
            c.remove("propertyPending");
        }

        if (c.containsKey("room") &&
            ROOMS.stream().noneMatch(
                a -> a.value().equals(c.get("room"))
            )) {
            c.remove("room");
        }

        if (c.containsKey("style") &&
            STYLES.stream().noneMatch(
                a -> a.value().equals(c.get("style"))
            )) {
            c.remove("style");
        }

        for (String key : List.of(
            "budgetMin", "budgetMax"
        )) {
            try {
                long value = Long.parseLong(
                    c.getOrDefault(key, "0")
                );

                if (value < 1000 ||
                    value > 50_000_000) {
                    c.remove(key);
                }

            } catch (NumberFormatException exception) {
                c.remove(key);
            }
        }

        if (c.containsKey("resultTier")) {
            try {
                int tier =
                    Integer.parseInt(c.get("resultTier"));

                if (tier < 0 || tier > 3) {
                    c.remove("resultTier");
                }

            } catch (NumberFormatException exception) {
                c.remove("resultTier");
            }
        }

        return c;
    }

    // =========================================================
    // DISPLAY HELPERS
    // =========================================================

    private static String moneyText(
        BigDecimal amount
    ) {
        return String.format(
            Locale.US, "%,.0f", amount
        );
    }

    private static List<String> roomOptions() {
        return List.of(
            "Kitchen",
            "Bathroom",
            "Guest room",
            "Garden",
            "Living room"
        );
    }

    private static Map<String, Object> say(
        String reply,
        Map<String, String> c,
        List<Map<String, Object>> projects,
        List<String> quickReplies
    ) {
        Map<String, Object> data =
            new LinkedHashMap<>();

        data.put("reply", reply);
        data.put(
            "context",
            new LinkedHashMap<>(c)
        );
        data.put("projects", projects);
        data.put("quickReplies", quickReplies);

        return data;
    }
}





