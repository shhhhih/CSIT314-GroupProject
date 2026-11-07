
document.addEventListener("DOMContentLoaded", () => {

    const topForm = document.getElementById("topForm");
    const registrationForm =
        document.getElementById("registrationForm");

    const today = new Date();

    const dateString = date => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");

        return `${year}-${month}-${day}`;
    };

    const firstDay = new Date(
        today.getFullYear(),
        today.getMonth(),
        1
    );

    // Default: current month to today
    document.getElementById("topFrom").value =
        dateString(firstDay);

    document.getElementById("topTo").value =
        dateString(today);

    document.getElementById("regFrom").value =
        dateString(firstDay);

    document.getElementById("regTo").value =
        dateString(today);

    async function getInsights(from, to) {

        if (!from || !to || from > to) {
            throw new Error("Please select a valid date range.");
        }

        const params = new URLSearchParams({ from, to });

        const response = await fetch(
            `/api/reports/insights?${params.toString()}`,
            { credentials: "same-origin" }
        );

        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.message ||
                `Unable to load insights (${response.status})`
            );
        }

        return data;
    }

    // Display Top 10 tables
    function displayTopProjects(rowsId, projects) {

        const rows = document.getElementById(rowsId);
        rows.replaceChildren();

        if (!projects.length) {
            const row = rows.insertRow();
            const cell = row.insertCell();

            cell.colSpan = 3;
            cell.className = "empty";
            cell.textContent = "No projects found for this period.";
            return;
        }

        projects.forEach((project, index) => {

            const row = rows.insertRow();

            row.insertCell().textContent = index + 1;
            row.insertCell().textContent = project.title;
            row.insertCell().textContent =
                Number(project.count).toLocaleString("en-SG");

        });
    }

    // Display bar chart rows
    function displayBars(containerId, items, labelKey) {

        const container = document.getElementById(containerId);
        container.replaceChildren();

        if (!items.length) {
            container.textContent = "No data found.";
            return;
        }

        const max = Math.max(
            1,
            ...items.map(item => Number(item.count))
        );

        items.forEach(item => {

            const row = document.createElement("div");
            row.className = "bar-row";

            const label = document.createElement("span");
            label.className = "label";
            label.textContent = item[labelKey];

            const track = document.createElement("div");
            track.className = "bar-track";

            const fill = document.createElement("div");
            fill.className = "bar-fill";

            const value = Number(item.count);

            fill.style.width =
                `${(value / max) * 100}%`;

            if (value === 0) {
                fill.style.minWidth = "0";
            }

            track.appendChild(fill);

            const count = document.createElement("span");
            count.className = "value";
            count.textContent = value.toLocaleString("en-SG");

            row.append(label, track, count);
            container.appendChild(row);
        });
    }

    // US26: Top 10 IDPs
    topForm.addEventListener("submit", async event => {
        event.preventDefault();

        const button = topForm.querySelector("button");
        button.disabled = true;

        try {
            const data = await getInsights(
                document.getElementById("topFrom").value,
                document.getElementById("topTo").value
            );

            displayTopProjects(
                "viewedRows",
                data.mostViewed
            );

            displayTopProjects(
                "shortlistedRows",
                data.mostShortlisted
            );

        } catch (error) {
            alert(error.message);
        } finally {
            button.disabled = false;
        }
    });

    // US28: New registrations
    registrationForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            const button =
                registrationForm.querySelector("button");

            button.disabled = true;

            try {
                const data = await getInsights(
                    document.getElementById("regFrom").value,
                    document.getElementById("regTo").value
                );

                displayBars(
                    "registrationBars",
                    data.registrations,
                    "role"
                );

                document.getElementById(
                    "registrationTotal"
                ).textContent =
                    `${data.totalRegistrations} new registration(s)`;

            } catch (error) {
                alert(error.message);
            } finally {
                button.disabled = false;
            }
        }
    );

    // US27: Category counts
    async function loadCategories() {
        try {
            const data = await getInsights(
                document.getElementById("topFrom").value,
                document.getElementById("topTo").value
            );

            displayBars(
                "categoryBars",
                data.categories,
                "categoryName"
            );

        } catch (error) {
            console.error(error);
        }
    }

    // Automatically display data on page load
    topForm.requestSubmit();
    registrationForm.requestSubmit();
    loadCategories();
});
