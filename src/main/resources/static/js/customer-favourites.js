
document.addEventListener("DOMContentLoaded", () => {
    const rows = document.getElementById("rows");
    const search = document.getElementById("search");

    let favourites = [];

    function displayFavourites() {
        const keyword = search.value.trim().toLowerCase();

        const filtered = favourites.filter(item => {
            const project = item.idp;

            return (
                (project.title || "").toLowerCase().includes(keyword) ||
                (project.categoryName || "").toLowerCase().includes(keyword) ||
                (project.propertyType || "").toLowerCase().includes(keyword)
            );
        });

        rows.replaceChildren();

        if (filtered.length === 0) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");

            cell.colSpan = 6;
            cell.className = "empty";
            cell.textContent = "No favourites found.";

            row.appendChild(cell);
            rows.appendChild(row);
            return;
        }

        filtered.forEach(item => {
            const project = item.idp;
            const row = document.createElement("tr");

            const values = [
                project.title || "Untitled Project",
                project.categoryName || "Uncategorised",
                project.propertyType || "-",
                `S$${Number(project.budget || 0).toLocaleString("en-SG")}`,
                item.savedAt ? new Date(item.savedAt).toLocaleDateString("en-SG") : "-"
            ];

            values.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });

            const actions = document.createElement("td");
            const link = document.createElement("a");

            link.href = `customer-idp.html?id=${encodeURIComponent(project.idpId)}`;
            link.textContent = "Open";

            actions.appendChild(link);
            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "btn danger small";
            remove.style.marginLeft = "16px";
            remove.textContent = "Remove";
            remove.addEventListener("click", async () => {
                if (!confirm(`Remove ${project.title} from favourites?`)) return;
                remove.disabled = true;
                try {
                    const response = await fetch(`/api/favourites/${encodeURIComponent(project.idpId)}`, {
                        method: "DELETE", credentials: "same-origin"
                    });
                    if (!response.ok) throw new Error(`Removal failed (${response.status})`);
                    favourites = favourites.filter(f => f.favouriteId !== item.favouriteId);
                    displayFavourites();
                } catch (error) {
                    alert(error.message);
                    remove.disabled = false;
                }
            });
            actions.appendChild(remove);
            row.appendChild(actions);
            rows.appendChild(row);
        });
    }

    async function loadFavourites() {
        rows.textContent = "Loading favourites...";

        try {
            const response = await fetch("/api/favourites", {
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error(`Failed to load favourites (${response.status})`);
            }

            favourites = await response.json();
            displayFavourites();

        } catch (error) {
            console.error(error);
            rows.replaceChildren();

            const row = document.createElement("tr");
            const cell = document.createElement("td");

            cell.colSpan = 6;
            cell.className = "empty";
            cell.textContent = "Unable to load favourites.";

            row.appendChild(cell);
            rows.appendChild(row);
        }
    }

    search.addEventListener("input", displayFavourites);
    loadFavourites();
});
