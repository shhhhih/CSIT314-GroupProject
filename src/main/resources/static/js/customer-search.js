
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("searchForm");
    const results = document.getElementById("results");
    const count = document.getElementById("count");
    
    
async function loadCategories() {
    const categorySelect = document.getElementById("category");

    if (!categorySelect) return;

    try {
        const response = await fetch("/api/categories");

        if (!response.ok) {
            throw new Error("Failed to load categories");
        }

        const categories = await response.json();

        categorySelect.replaceChildren(new Option("All categories", ""));

        categories.forEach(category => {
            const option = new Option(
                category.categoryName,
                category.categoryID
            );

            categorySelect.add(option);
        });

    } catch (error) {
        console.error("Unable to load categories:", error);
    }
}

    const keywordFromHome = new URLSearchParams(location.search).get('keyword');
    if (keywordFromHome && form) form.elements.namedItem('keyword').value = keywordFromHome;

    if (!form || !results) {
        console.error("Search form or results container not found.");
        return;
    }

    async function searchIDPs() {
        const params = new URLSearchParams();

        for (const field of ["keyword", "categoryId", "propertyType", "minBudget", "maxBudget"]) {
            const input = form.elements.namedItem(field);
            const value = input?.value?.trim();

            if (value) {
                params.set(field, value);
            }
        }

        results.textContent = "Searching projects...";

        try {
            const response = await fetch(`/api/idps?${params.toString()}`, {
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error(`Search failed (${response.status})`);
            }

            const projects = await response.json();
            results.replaceChildren();

            if (count) {
                count.textContent = `${projects.length} project(s) found`;
}

            if (!projects.length) {
                results.textContent = "No projects found.";
                return;
            }

            projects.forEach(project => {
                const card = document.createElement("article");
                card.className = "card";

                const title = document.createElement("h3");
                title.textContent = project.title || "Untitled Project";

                const details = document.createElement("p");
                details.textContent =
                    `${project.propertyType || "Property type unavailable"} · ` +
                    `S$${Number(project.budget || 0).toLocaleString("en-SG")}`;

                const link = document.createElement("a");
                link.href = `customer-idp.html?id=${encodeURIComponent(project.idpId)}`;
                link.textContent = "View Details";
                link.className = "btn";
                
const favouriteButton = document.createElement("button");
favouriteButton.type = "button";
favouriteButton.className = "btn";
favouriteButton.textContent = "♡ Save to Favourites";

favouriteButton.addEventListener("click", async () => {
    favouriteButton.disabled = true;

    try {
        const response = await fetch("/api/favourites", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "same-origin",
            body: JSON.stringify({
                idpId: project.idpId
            })
        });

        if (response.status === 409) {
            favouriteButton.textContent = "♥ Already Saved";
            return;
        }

        if (!response.ok) {
            throw new Error(`Unable to save favourite (${response.status})`);
        }

        favouriteButton.textContent = "♥ Saved!";
    } catch (error) {
        console.error(error);
        favouriteButton.textContent = "Try Again";
        favouriteButton.disabled = false;
    }
});


                

                card.append(title, details, link, favouriteButton);
                results.appendChild(card);
            });
        } catch (error) {
            console.error(error);
            results.textContent = "Unable to load projects. Please try again.";
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
