
document.addEventListener("DOMContentLoaded", async () => {

    const dashboardFields = [
        "totalUsers",
        "totalCustomers",
        "totalDesigners",
        "totalIdps",
        "totalViews",
        "totalShortlists"
    ];

    // Update the dashboard cards with database values
    function updateDashboard(data) {

        dashboardFields.forEach(field => {

            const element = document.getElementById(field);

            if (element) {

                const value = Number(data[field] ?? 0);

                element.textContent =
                    value.toLocaleString("en-SG");
            }

        });
    }

    // Display an error if statistics cannot be loaded
    function showError(message) {

        dashboardFields.forEach(field => {

            const element = document.getElementById(field);

            if (element) {
                element.textContent = "—";
            }

        });

        console.error("Dashboard error:", message);
    }

    // Retrieve statistics from Spring Boot
    try {

        const response = await fetch("/api/reports/overview", {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error(
                `Unable to load dashboard (${response.status})`
            );
        }

        const data = await response.json();

        updateDashboard(data);

    } catch (error) {

        showError(error.message);

    }

});
