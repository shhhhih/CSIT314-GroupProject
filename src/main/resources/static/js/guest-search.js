
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("searchForm");
    const results = document.getElementById("results");
    const count = document.getElementById("count");
    const categorySelect = document.getElementById("category");

    if (!form || !results) return;

    async function loadCategories() {
        if (!categorySelect) return;

        try {
            const response = await fetch("/api/categories");

            if (!response.ok) {
                throw new Error("Failed to load categories");
            }

            const categories = await response.json();

            categorySelect.replaceChildren(new Option("All categories", ""));

            categories.forEach(category => {
                categorySelect.add(
                    new Option(category.categoryName, category.categoryID)
                );
            });
        } catch (error) {
            console.error("Unable to load categories:", error);
        }
    }

    async function searchIDPs() {
        const params = new URLSearchParams();

        for (const field of [
            "keyword",
            "categoryId",
            "propertyType",
            "minBudget",
            "maxBudget"
        ]) {
            const input = form.elements.namedItem(field);
            const value = input?.value?.trim();

            if (value) {
                params.set(field, value);
            }
        }

        results.textContent = "Searching projects...";

        try {
            const response = await fetch(
                `/api/guest/idps?${params.toString()}`
            );

            if (!response.ok) {
                throw new Error(`Search failed (${response.status})`);
            }

            const projects = await response.json();

            results.replaceChildren();
            count.textContent = `${projects.length} project(s) found`;

            if (projects.length === 0) {
                results.textContent = "No projects found.";
                return;
            }

            projects.forEach(project => {
                const card = document.createElement("article");
                card.className = "card idp-card";

                const body = document.createElement("div");
                body.className = "idp-card-body";

                const title = document.createElement("h3");
                title.textContent = project.title || "Untitled Project";

                const details = document.createElement("p");
                details.textContent =
                    `${project.propertyType || "Unknown"} · S$${Number(
                        project.budget || 0
                    ).toLocaleString("en-SG")}`;

                const link = document.createElement("a");
                link.href = "login.html";
                link.className = "btn";
                link.textContent = "Log in to View Details";

                body.append(title, details, link);
                card.appendChild(body);
                results.appendChild(card);
            });
        } catch (error) {
            console.error(error);
            results.textContent =
                "Unable to load projects. Please try again.";
            count.textContent = "";
        }
    }

    form.addEventListener("submit", event => {
        event.preventDefault();
        searchIDPs();
    });

    form.addEventListener("reset", () => {
        setTimeout(searchIDPs, 0);
    });

    loadCategories();
    searchIDPs();
});
